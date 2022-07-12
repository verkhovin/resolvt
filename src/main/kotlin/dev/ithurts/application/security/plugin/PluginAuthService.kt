package dev.ithurts.application.security.plugin

import dev.ithurts.application.exception.PluginAuthFailedException
import dev.ithurts.domain.account.Account
import dev.ithurts.domain.authcode.AuthCode
import dev.ithurts.application.model.PluginToken
import dev.ithurts.application.model.TokenType
import dev.ithurts.domain.authcode.AuthCodeRepository
import org.apache.tomcat.util.buf.HexUtils
import org.springframework.stereotype.Service
import java.security.MessageDigest
import java.time.Clock
import java.time.Instant
import java.time.temporal.ChronoUnit.MINUTES
import java.util.*
import kotlin.random.Random

@Service
class PluginAuthService(
    private val authCodeRepository: AuthCodeRepository,
    private val pluginTokenManager: PluginTokenManager,
    private val clock: Clock
) {
    fun generateAuthCode(account: Account, codeChallenge: String): String {
        val authCodeValue = Random.nextInt(1000, 10000).toString()
        val expiresAt = clock.instant().plus(5, MINUTES)

        val authCode = createOrUpdateActualAuthCode(account, authCodeValue, codeChallenge, expiresAt)

        authCodeRepository.save(authCode)
        return authCode.authCode
    }

    fun issuePluginToken(authCodeValue: String, codeVerifier: String): PluginToken {
        val codeChallenge = buildCodeChallenge(codeVerifier)
        val authCode = authCodeRepository.getByAuthCodeAndCodeChallenge(authCodeValue, codeChallenge)
            ?: throw PluginAuthFailedException("Invalid auth code")

        if (authCode.expiresAt.isBefore(clock.instant())) {
            throw PluginAuthFailedException("Auth code expired")
        }

        authCode.used = true
        authCodeRepository.save(authCode)
        return pluginTokenManager.issuePluginToken(authCode.accountId)
    }

    fun refreshPluginToken(refreshToken: String): PluginToken {
        val accountId = pluginTokenManager.validateToken(TokenType.REFRESH, refreshToken)
        return pluginTokenManager.issuePluginToken(accountId)
    }

    private fun buildCodeChallenge(codeVerifier: String): String =
        HexUtils.toHexString(MessageDigest.getInstance("SHA-256").digest(codeVerifier.toByteArray()))
            .let { hashedVerifier -> Base64.getUrlEncoder().encodeToString(hashedVerifier.toByteArray()) }

    private fun createOrUpdateActualAuthCode(
        account: Account,
        authCodeValue: String,
        codeChallenge: String,
        expiresAt: Instant
    ) = authCodeRepository.getByAccountIdAndExpiresAtAfterAndUsedIsFalse(account.id, clock.instant())
        ?.copy(
            authCode = authCodeValue,
            codeChallenge = codeChallenge,
            expiresAt = expiresAt
        ) ?: AuthCode(
        authCodeValue,
        codeChallenge,
        account.id,
        expiresAt
    )
}