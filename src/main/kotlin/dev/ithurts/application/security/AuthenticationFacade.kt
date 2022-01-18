package dev.ithurts.application.security

import dev.ithurts.domain.account.Account
import dev.ithurts.application.security.oauth2.AuthenticatedOAuth2User
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component

@Component
class AuthenticationFacade {
    val account: Account
        get() {
            return when (val principal = SecurityContextHolder.getContext().authentication.principal) {
                is Account -> principal
                is AuthenticatedOAuth2User -> principal.account
                else -> throw IllegalStateException("Unknown principal type: $principal")
            }
        }
}