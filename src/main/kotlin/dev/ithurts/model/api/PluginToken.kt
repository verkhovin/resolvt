package dev.ithurts.model.api

data class PluginToken(
    val accessToken: String,
    val refreshToken: String,
)

enum class TokenType {
    ACCESS,
    REFRESH
}