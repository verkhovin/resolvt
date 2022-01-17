package dev.ithurts.controller.api

import dev.ithurts.application.dto.Me
import dev.ithurts.application.security.AuthenticationFacade
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("api")
class MeController(
    private val authenticationFacade: AuthenticationFacade
) {
    @GetMapping("/me")
    fun me(): Me {
        val account = authenticationFacade.account
        return Me(account.id!!, account.name)
    }

//    @ExceptionHandler(Exception::class)
//    fun handleException(e: Exception): ResponseEntity<*> {
//        return ResponseEntity.badRequest().build<Any>()
//    }

}