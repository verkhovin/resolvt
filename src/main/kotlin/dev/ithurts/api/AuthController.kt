package dev.ithurts.api

import dev.ithurts.model.api.PluginToken
import dev.ithurts.service.PluginAuthService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("api")
class AuthController(
    private val pluginAuthService: PluginAuthService
) {
    @PostMapping("/auth/access_token")
    fun getAccessToken(@RequestParam authCode: String, @RequestParam codeVerifier: String): PluginToken {
        return pluginAuthService.issuePluginToken(authCode, codeVerifier)
    }
}