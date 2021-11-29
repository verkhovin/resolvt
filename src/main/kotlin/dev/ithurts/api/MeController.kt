package dev.ithurts.api

import dev.ithurts.model.Account
import dev.ithurts.model.api.Me
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("api")
class MeController {
    @GetMapping("/me")
    fun me(@AuthenticationPrincipal account: Account): Me {
        return Me(account.id!!, account.name)
    }
}