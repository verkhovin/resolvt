package dev.ithurts.api

import dev.ithurts.model.api.PluginToken
import dev.ithurts.service.PluginAuthService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class AuthController(
    private val pluginAuthService: PluginAuthService
) {
    @PostMapping("/auth/access-token")
    fun getAccessToken(
        @RequestParam(required = false) authorizationCode: String?,
        @RequestParam(required = false) refreshToken: String?,
        @RequestParam codeVerifier: String,
        @RequestParam grantType: String
    ): PluginToken {
        return if (grantType == "authorization_code") {
            if (authorizationCode == null) {
                throw IllegalArgumentException("authorizationCode is required")
            }
            pluginAuthService.issuePluginToken(authorizationCode, codeVerifier)
        } else if (grantType == "refresh_token") {
            if (refreshToken == null) {
                throw IllegalArgumentException("refreshToken is required")
            }
            pluginAuthService.refreshPluginToken(refreshToken)
        } else {
            throw IllegalArgumentException("grantType must be either 'authorization_code' or 'refresh_token'")
        }
    }
}