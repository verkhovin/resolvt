package dev.ithurts.security

import dev.ithurts.model.Account
import dev.ithurts.model.organisation.Organisation
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component

@Component
class IntegrationAuthenticationFacade {
    val organisation: Organisation
        get() = SecurityContextHolder.getContext().authentication.principal as Organisation
}