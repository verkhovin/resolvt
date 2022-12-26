package dev.resolvt.service.sourceprovider.github

import dev.resolvt.configuration.ApplicationProperties
import dev.resolvt.configuration.Github
import dev.resolvt.service.sourceprovider.SourceProviderAuthenticationProvider
import dev.resolvt.service.sourceprovider.github.model.GithubInstallationAccessToken
import dev.resolvt.service.workspace.Workspace
import dev.resolvt.util.noBody
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.bouncycastle.openssl.PEMKeyPair
import org.bouncycastle.openssl.PEMParser
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpMethod
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.io.FileReader
import java.security.Key
import java.security.KeyPair
import java.security.PrivateKey
import java.time.Clock
import java.time.temporal.ChronoUnit

@Service
@Github
class GithubAuthenticationProvider(
    @Qualifier("githubRestTemplate") private val restTemplate: RestTemplate,
    private val applicationProperties: ApplicationProperties,
    clientService: OAuth2AuthorizedClientService,
    private val clock: Clock,
): SourceProviderAuthenticationProvider(clientService) {
    private val privateKey: Key = getPrivateKeyFromFile(applicationProperties.github!!.tokenSignaturePrivateKeyLocation)

    override fun getAuthentication(workspace: Workspace): String {
        val appToken = buildJWT()
        return restTemplate.exchange(
            "/app/installations/${workspace.sourceProviderApplicationCredentials.clientKey}/access_tokens",
            HttpMethod.POST,
            noBody(appToken, mapOf("Accept" to "application/vnd.github+json")),
            GithubInstallationAccessToken::class.java
        ).body!!.token
    }

    private fun buildJWT(): String {
        val now = clock.instant()
        return Jwts.builder()
            .setClaims(mapOf("iat" to now.minusSeconds(60).epochSecond,
                "exp" to now.plus(5, ChronoUnit.MINUTES).epochSecond))
            .setIssuer(applicationProperties.github!!.appId)
            .signWith(privateKey, SignatureAlgorithm.RS256)
            .compact()
    }

    private fun getPrivateKeyFromFile(filePath: String): PrivateKey {
        val pemParser = PEMParser(FileReader(filePath))
        val converter = JcaPEMKeyConverter()
        val `object`: Any = pemParser.readObject()
        val kp: KeyPair = converter.getKeyPair(`object` as PEMKeyPair)
        return kp.private
    }
}