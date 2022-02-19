package dev.ithurts.application.internal.sourceprovider.bitbucket

import dev.ithurts.domain.workspace.SourceProviderApplicationCredentials
import dev.ithurts.domain.workspace.Workspace
import dev.ithurts.application.security.oauth2.AuthenticatedOAuth2User
import dev.ithurts.application.internal.sourceprovider.bitbucket.dto.Token
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.*
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestTemplate
import java.time.Clock
import java.util.*

@Service
class BitbucketAuthorizationProvider(
    private val clientService: OAuth2AuthorizedClientService,
    @Qualifier("commonRestTemplate")
    private val restTemplate: RestTemplate,
    private val clock: Clock,
) {
    fun getAuthorization(): String {
        val authentication = SecurityContextHolder
            .getContext()
            .authentication
        return when (authentication.principal) {
            is AuthenticatedOAuth2User -> {
                getAuthenticationOnBehalfOfAccount(authentication)
            }
            is Workspace -> {
                getAuthenticationOnBehalfOfWorkspace(authentication.principal as Workspace)
            }
            else -> throw IllegalStateException("Unknown principal type")
        }
    }

    @PreAuthorize("hasPermission(#workspace.id, 'Workspace', 'MEMBER')")
    fun getAuthorization(workspace: Workspace): String {
        return getAuthenticationOnBehalfOfWorkspace(workspace)
    }

    private fun getAuthenticationOnBehalfOfAccount(authentication: Authentication?): String {
        authentication as OAuth2AuthenticationToken
        val client: OAuth2AuthorizedClient = clientService.loadAuthorizedClient(
            authentication.authorizedClientRegistrationId,
            authentication.name
        )

        return client.accessToken.tokenValue
    }

    private fun getAuthenticationOnBehalfOfWorkspace(
        workspace: Workspace,
    ): String {
        val token = buildBitbucketAppJWT(workspace.sourceProviderApplicationCredentials)
        val body = LinkedMultiValueMap<String, String>()
        body.add("grant_type", "urn:bitbucket:oauth2:jwt")
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_FORM_URLENCODED
        headers.add("Authorization", "JWT $token")
        val response: ResponseEntity<Token> = restTemplate.exchange(
            "https://bitbucket.org/site/oauth2/access_token", HttpMethod.POST, HttpEntity(body, headers),
            Token::class.java
        )
        return response.body!!.accessToken
    }

    private fun buildBitbucketAppJWT(applicationCredentials: SourceProviderApplicationCredentials): String? {
        val clientKey = applicationCredentials.clientKey
        val secret = applicationCredentials.secret

        return Jwts.builder()
            .setIssuedAt(Date(clock.millis()))
            .setExpiration(Date(clock.instant().plusSeconds(18000).toEpochMilli()))
            .setIssuer("it-hurts-app")
            .setSubject(clientKey)
            .addClaims(mapOf("qsh" to qsh))
            .signWith(Keys.hmacShaKeyFor(secret.toByteArray()))
            .compact()
    }

    companion object {
        private val qsh = buildQueryStringHash("POST&/site/oauth2/access_token$")
    }
}