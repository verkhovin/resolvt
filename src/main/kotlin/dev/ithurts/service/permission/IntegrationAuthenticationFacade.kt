package dev.ithurts.service.permission

import dev.ithurts.service.workspace.Workspace
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component

@Component
class IntegrationAuthenticationFacade {
    val workspace: Workspace
        get() = SecurityContextHolder.getContext().authentication.principal as Workspace
}