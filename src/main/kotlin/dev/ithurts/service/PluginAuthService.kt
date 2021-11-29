package dev.ithurts.service

import dev.ithurts.exception.PluginAuthFailedException
import dev.ithurts.model.Account
import dev.ithurts.model.AuthCode
import dev.ithurts.model.api.PluginToken
import dev.ithurts.model.api.TokenType
import dev.ithurts.repository.AuthCodeRepository
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
    ) = authCodeRepository.getByAccountIdAndExpiresAtAfterAndUsedIsFalse(account.id!!, clock.instant())
        ?.also { authCode ->
            authCode.authCode = authCodeValue
            authCode.codeChallenge = codeChallenge
            authCode.expiresAt = expiresAt
        } ?: AuthCode(
        authCodeValue,
        codeChallenge,
        account.id,
        expiresAt
    )
}