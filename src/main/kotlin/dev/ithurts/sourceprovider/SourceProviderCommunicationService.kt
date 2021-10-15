package dev.ithurts.sourceprovider

import dev.ithurts.model.SourceProvider
import dev.ithurts.sourceprovider.bitbucket.BitbucketClient
import dev.ithurts.sourceprovider.model.SourceProviderOrganisation
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.stereotype.Service

@Service
class SourceProviderCommunicationService(
    private val bitbucketClient: BitbucketClient,
    private val clientService: OAuth2AuthorizedClientService
) {
    fun getOwnedExternalOrganisations(): List<SourceProviderOrganisation> =
        client.getUserOrganisations(accessToken, client.organisationOwnerRole)

    fun getCurrentSourceProvider(): SourceProvider =
        SourceProvider.valueOf(authentication().authorizedClientRegistrationId.uppercase())

    fun getOrganisation(externalOrganisationId: String): SourceProviderOrganisation {
        return client.getOrganisation(accessToken, externalOrganisationId)
    }

    private val client: SourceProviderClient
        get() = when (SourceProvider.valueOf(authentication().authorizedClientRegistrationId.uppercase())) {
            SourceProvider.BITBUCKET -> bitbucketClient
        }

    private val accessToken: String
        get() {
            val authentication = authentication()
            val client: OAuth2AuthorizedClient = clientService.loadAuthorizedClient(
                authentication.authorizedClientRegistrationId,
                authentication.name
            )

            return client.accessToken.tokenValue
        }

    private fun authentication() = SecurityContextHolder
        .getContext()
        .authentication as OAuth2AuthenticationToken
}
