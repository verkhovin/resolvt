package dev.ithurts.application.dto

data class PluginToken(
    val accessToken: String,
    val refreshToken: String,
)

enum class TokenType {
    ACCESS,
    REFRESH
}