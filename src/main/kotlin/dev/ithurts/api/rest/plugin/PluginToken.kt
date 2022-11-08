package dev.ithurts.application.model

data class PluginToken(
    val accessToken: String,
    val refreshToken: String,
)

enum class TokenType {
    ACCESS,
    REFRESH
}