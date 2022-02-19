package dev.ithurts.controller.web

import dev.ithurts.domain.workspace.WorkspaceRepository
import dev.ithurts.application.security.oauth2.AuthenticatedOAuth2User
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
class SessionEnrichingHandlerInterceptor(
    private val workspaceRepository: WorkspaceRepository,
    private val sessionManager: SessionManager
) : HandlerInterceptor {
    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
    ): Boolean {
        request.getSession(false)?.let { session ->
            if ((request.parameterMap["sync"]?.firstOrNull()
                    ?.toBoolean() == true) || session.getAttribute("currentOrganisation.id") == null
            ) {
                SecurityContextHolder.getContext()?.authentication?.principal?.let { user ->
                    if (user is AuthenticatedOAuth2User) {
                        val workspaces = workspaceRepository.getByMembers_accountId(user.accountId)
                        workspaces.minByOrNull { it.id }?.let { firstWorkspace ->
                            val member = firstWorkspace.getMember(user.accountId)!!
                            sessionManager.setCurrentOrganisation(session, firstWorkspace.id, member.role)
                            sessionManager.setOrganisations(session, workspaces) // TODO workspace to dto
                        }
                    }
                }
            }
        }
        return true
    }
}