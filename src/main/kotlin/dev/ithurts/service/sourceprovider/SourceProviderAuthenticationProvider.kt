package dev.ithurts.service.sourceprovider

import dev.ithurts.api.web.oauth2.AuthenticatedOAuth2User
import dev.ithurts.service.workspace.Workspace
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken

abstract class SourceProviderAuthenticationProvider(
    private val clientService: OAuth2AuthorizedClientService
) {
    fun getAuthentication(): String {
        val authentication = SecurityContextHolder
            .getContext()
            .authentication
        return when (authentication.principal) {
            is AuthenticatedOAuth2User -> {
                getAuthenticationOnBehalfOfAccount(authentication)
            }
            is Workspace -> {
                getAuthentication(authentication.principal as Workspace)
            }
            else -> throw IllegalStateException("Unknown principal type")
        }
    }

    abstract fun getAuthentication(workspace: Workspace): String

    protected fun getAuthenticationOnBehalfOfAccount(authentication: Authentication?): String {
        authentication as OAuth2AuthenticationToken
        val client: OAuth2AuthorizedClient = clientService.loadAuthorizedClient(
            authentication.authorizedClientRegistrationId,
            authentication.name
        )

        return client.accessToken.tokenValue
    }
}