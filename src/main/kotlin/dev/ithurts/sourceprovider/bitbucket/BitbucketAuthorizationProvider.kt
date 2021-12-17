package dev.ithurts.sourceprovider.bitbucket

import dev.ithurts.model.organisation.Organisation
import dev.ithurts.security.AuthenticatedOAuth2User
import dev.ithurts.sourceprovider.bitbucket.dto.Token
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
    private val clock: Clock
) {
    fun getAuthorization(): String {
        val authentication = SecurityContextHolder
            .getContext()
            .authentication
        return when (authentication.principal) {
            is AuthenticatedOAuth2User -> {
                getAuthenticationOnBehalfOfAccount(authentication)
            }
            is Organisation -> {
                getAuthenticationOnBehalfOfOrganisation(authentication)
            }
            else -> throw IllegalStateException("Unknown principal type")
        }
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
        authentication: Authentication,
    ): String {
         val token = buildAddonJWT(authentication)
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

    private fun buildAddonJWT(authentication: Authentication): String? {
        val subject = authentication.principal as Organisation
        val clientKey = subject.clientKey
        val secret = subject.secret

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