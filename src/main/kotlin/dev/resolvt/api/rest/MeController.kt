package dev.resolvt.api.rest

import dev.resolvt.service.permission.AuthenticationFacade
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("api")
class MeController(
    private val authenticationFacade: AuthenticationFacade
) {
    @GetMapping("/me")
    fun me(): dev.resolvt.api.rest.Me {
        val account = authenticationFacade.account
        return dev.resolvt.api.rest.Me(account.id, account.name)
    }

//    @ExceptionHandler(Exception::class)
//    fun handleException(e: Exception): ResponseEntity<*> {
//        return ResponseEntity.badRequest().build<Any>()
//    }

}

data class Me(
    val id: String,
    val name: String,
)
