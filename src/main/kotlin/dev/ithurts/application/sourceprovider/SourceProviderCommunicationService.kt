package dev.ithurts.application.sourceprovider

import dev.ithurts.domain.account.Account
import dev.ithurts.domain.SourceProvider
import dev.ithurts.domain.SourceProvider.*
import dev.ithurts.domain.workspace.Workspace
import dev.ithurts.application.sourceprovider.bitbucket.BitbucketAuthorizationProvider
import dev.ithurts.application.sourceprovider.bitbucket.BitbucketClient
import dev.ithurts.application.sourceprovider.model.SourceProviderRepository
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.stereotype.Service

@Service
class SourceProviderCommunicationService(
    private val bitbucketClient: BitbucketClient,
    private val bitbucketAuthorizationProvider: BitbucketAuthorizationProvider
) {

    fun getDiff(organisation: String, repository: String, spec: String): String {
        return client.getDiff(getAccessToken(), organisation, repository, spec)
    }

    fun getCurrentSourceProvider(): SourceProvider {
        val authentication = SecurityContextHolder.getContext().authentication
        return when {
            authentication is OAuth2AuthenticationToken -> SourceProvider.valueOf(authentication.authorizedClientRegistrationId.uppercase())
            authentication.principal is Workspace -> (authentication.principal as Workspace).sourceProvider
            authentication.principal is Account -> (authentication.principal as Account).sourceProvider
            else -> throw IllegalStateException("Unknown authentication type")
        }
    }

    fun getRepository(organisationExternalId: String, repository: String): SourceProviderRepository {
        return client.getRepository(getAccessTokenUnsafe(organisationExternalId), organisationExternalId, repository)
    }

    private val client: SourceProviderClient
        get() = when (getCurrentSourceProvider()) {
            BITBUCKET -> bitbucketClient
        }

    private fun getAccessToken(): String = when (getCurrentSourceProvider()) {
        BITBUCKET -> bitbucketAuthorizationProvider.getAuthorization()
    }

    private fun getAccessTokenUnsafe(organisationExternalId: String) = when (getCurrentSourceProvider()) {
        BITBUCKET -> bitbucketAuthorizationProvider.getAuthorizationUnsafe(organisationExternalId)
    }
}
