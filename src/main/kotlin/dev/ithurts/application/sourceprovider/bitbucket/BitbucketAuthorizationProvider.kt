package dev.ithurts.application.sourceprovider.bitbucket

import dev.ithurts.domain.SourceProvider
import dev.ithurts.domain.workspace.SourceProviderApplicationCredentials
import dev.ithurts.domain.workspace.Workspace
import dev.ithurts.application.security.oauth2.AuthenticatedOAuth2User
import dev.ithurts.application.sourceprovider.bitbucket.dto.Token
import dev.ithurts.domain.workspace.WorkspaceRepository
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.apache.tomcat.util.buf.HexUtils
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.*
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestTemplate
import java.security.MessageDigest
import java.time.Clock
import java.util.*

@Service
class BitbucketAuthorizationProvider(
    private val clientService: OAuth2AuthorizedClientService,
    @Qualifier("commonRestTemplate")
    private val restTemplate: RestTemplate,
    private val clock: Clock,
    private val workspaceRepository: WorkspaceRepository
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
                getAuthenticationOnBehalfOfOrganisation(authentication.principal as Workspace)
            }
            else -> throw IllegalStateException("Unknown principal type")
        }
    }

    fun getAuthorizationUnsafe(organisationExternalId: String): String {
        val organisation = workspaceRepository.findBySourceProviderAndExternalId(SourceProvider.BITBUCKET, organisationExternalId)
            ?: throw IllegalArgumentException("Organisation not found")
        return getAuthenticationOnBehalfOfOrganisation(organisation)
    }

    private fun getAuthenticationOnBehalfOfAccount(authentication: Authentication?): String {
        authentication as OAuth2AuthenticationToken
        val client: OAuth2AuthorizedClient = clientService.loadAuthorizedClient(
            authentication.authorizedClientRegistrationId,
            authentication.name
        )

        return client.accessToken.tokenValue
    }

    private fun getAuthenticationOnBehalfOfOrganisation(
        workspace: Workspace,
    ): String {
        val token = buildAddonJWT(workspace.sourceProviderApplicationCredentials)
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

    private fun buildAddonJWT(applicationCredentials: SourceProviderApplicationCredentials): String? {
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
        private val qsh = getQueryStringHash("POST&/site/oauth2/access_token$")
        private fun getQueryStringHash(canonicalUrl: String): String? {
            val md: MessageDigest = MessageDigest.getInstance("SHA-256")
            md.update(canonicalUrl.toByteArray(charset("UTF-8")))
            val digest: ByteArray = md.digest()
            return HexUtils.toHexString(digest)
        }
    }
}