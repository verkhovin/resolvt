package dev.resolvt.api.rest.plugin

import dev.resolvt.application.model.PluginToken
import dev.resolvt.application.model.TokenType
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.Clock
import java.time.temporal.ChronoUnit
import java.util.*

@Service
class PluginTokenManager(
    private val clock: Clock,
    @Value("\${resolvt.security.jwt.key}")private val jwtKey: String
) {
    fun issuePluginToken(accountId: String): PluginToken {
        return PluginToken(
            generateJwtToken(accountId, TokenType.ACCESS, 30)   ,
            generateJwtToken(accountId, TokenType.REFRESH, ONE_MONTH_IN_MINUTES),
        )
    }

    fun validateToken(expectedType: TokenType, token: String): String {
        val claims = Jwts.parserBuilder()
            .setSigningKey(Keys.hmacShaKeyFor(jwtKey.toByteArray()))
            .build()
            .parseClaimsJws(token)
            .body

        val accountId = claims.subject
        val tokenType = claims.get("type", String::class.java)

        if (tokenType != expectedType.name) {
            throw PluginAuthFailedException("Invalid token type")
        }

        return accountId
    }

    private fun generateJwtToken(userId: String, type: TokenType, expirationMinutes: Long) = Jwts.builder()
        .setSubject(userId)
        .addClaims(mapOf("type" to type))
        .setExpiration(Date.from(clock.instant().plus(expirationMinutes, ChronoUnit.MINUTES)))
        .signWith(Keys.hmacShaKeyFor(jwtKey.toByteArray()))
        .compact()

    companion object {
        private const val ONE_MONTH_IN_MINUTES = 43200L
    }
}