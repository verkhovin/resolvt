package dev.ithurts.sourceprovider

import dev.ithurts.model.SourceProvider
import dev.ithurts.model.SourceProvider.*
import dev.ithurts.model.organisation.Organisation
import dev.ithurts.sourceprovider.bitbucket.BitbucketAuthorizationProvider
import dev.ithurts.sourceprovider.bitbucket.BitbucketClient
import dev.ithurts.sourceprovider.model.SourceProviderOrganisation
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.stereotype.Service

@Service
class SourceProviderCommunicationService(
    private val bitbucketClient: BitbucketClient,
    private val bitbucketAuthorizationProvider: BitbucketAuthorizationProvider
) {
    fun getOwnedExternalOrganisations(): List<SourceProviderOrganisation> =
        client.getUserOrganisations(getAccessToken(), client.organisationOwnerRole)

    fun getDiff(organisation: String, repository: String, spec: String): String {
        return client.getDiff(getAccessToken(), organisation, repository, spec)
    }

    fun getCurrentSourceProvider(): SourceProvider {
        val authentication = SecurityContextHolder.getContext().authentication
        return when {
            authentication is OAuth2AuthenticationToken -> SourceProvider.valueOf(authentication.authorizedClientRegistrationId.uppercase())
            authentication.principal is Organisation -> (authentication.principal as Organisation).sourceProvider
            else -> throw IllegalStateException("Unknown authentication type")
        }
    }

    private val client: SourceProviderClient
        get() = when (getCurrentSourceProvider()) {
            BITBUCKET -> bitbucketClient
        }

    private fun getAccessToken(): String = when (getCurrentSourceProvider()) {
        BITBUCKET -> bitbucketAuthorizationProvider.getAuthorization()
    }
}
