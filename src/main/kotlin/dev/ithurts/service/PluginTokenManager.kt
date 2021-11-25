package dev.ithurts.service

import dev.ithurts.model.api.PluginToken
import dev.ithurts.model.api.TokenType
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
    @Value("\${ithurts.security.jwt.key}")private val jwtKey: String
) {
    fun issuePluginToken(userId: Long): PluginToken {
        return PluginToken(
            generateJwtToken(userId, TokenType.ACCESS, 30),
            generateJwtToken(userId, TokenType.REFRESH, ONE_MONTH_IN_MINUTES),
        )
    }

    private fun generateJwtToken(userId: Long, type: TokenType, expirationMinutes: Long) = Jwts.builder()
        .setSubject(userId.toString())
        .setClaims(mapOf("type" to type))
        .setExpiration(Date.from(clock.instant().plus(expirationMinutes, ChronoUnit.MINUTES)))
        .signWith(Keys.hmacShaKeyFor(jwtKey.toByteArray()))
        .compact()

    companion object {
        private const val ONE_MONTH_IN_MINUTES = 43200L
    }
}