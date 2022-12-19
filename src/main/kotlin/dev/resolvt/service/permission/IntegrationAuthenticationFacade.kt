package dev.resolvt.service.permission

import dev.resolvt.service.workspace.Workspace
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component

@Component
class IntegrationAuthenticationFacade {
    val workspace: Workspace
        get() = SecurityContextHolder.getContext().authentication.principal as Workspace
}