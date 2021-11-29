package dev.ithurts.api

import dev.ithurts.model.Account
import dev.ithurts.model.api.Me
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.lang.Exception

@RestController
@RequestMapping("api")
class MeController {
    @GetMapping("/me")
    fun me(@AuthenticationPrincipal account: Account): Me {
        return Me(account.id!!, account.name)
    }

    @ExceptionHandler(Exception::class)
    fun handleException(e: Exception): ResponseEntity<*> {
        return ResponseEntity.badRequest().build<Any>()
    }

}