package dev.ithurts.security

import dev.ithurts.model.Account
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component

@Component
class AuthenticationFacade {
    val account: Account
        get() = SecurityContextHolder.getContext().authentication.principal as Account
}