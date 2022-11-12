package dev.ithurts.service.sourceprovider.bitbucket

import dev.ithurts.configuration.Bitbucket
import dev.ithurts.service.sourceprovider.SourceProviderAuthenticationProvider
import dev.ithurts.service.sourceprovider.bitbucket.model.Token
import dev.ithurts.service.workspace.SourceProviderApplicationCredentials
import dev.ithurts.service.workspace.Workspace
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.*
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestTemplate
import java.time.Clock
import java.util.*

@Service
@Bitbucket
class BitbucketAuthenticationProvider(
    clientService: OAuth2AuthorizedClientService,
    @Qualifier("commonRestTemplate")
    private val restTemplate: RestTemplate,
    private val clock: Clock,
) : SourceProviderAuthenticationProvider(clientService) {

    override fun getAuthentication(workspace: Workspace): String {
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
            .setSubject(clientKey.toString())
            .addClaims(mapOf("qsh" to qsh))
            .signWith(Keys.hmacShaKeyFor(secret!!.toByteArray()))
            .compact()
    }

    companion object {
        private val qsh = buildQueryStringHash("POST&/site/oauth2/access_token$")
    }
}