package dev.resolvt.api.web

import dev.resolvt.api.web.oauth2.AuthenticatedOAuth2User
import dev.resolvt.api.rest.plugin.PluginAuthService
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
@RequestMapping("plugins/auth")
class PluginAuthController(
    private val pluginAuthService: PluginAuthService
) {
    @GetMapping("/code")
    fun pluginAuthCode(
        @AuthenticationPrincipal authentication: AuthenticatedOAuth2User,
        @RequestParam("code_challenge") codeChallenge: String,
        model: Model
    ): String {
        val authCode = pluginAuthService.generateAuthCode(authentication.account, codeChallenge)
        model.addAttribute("authCode", authCode)
        return "plugin-auth-code"
    }

}