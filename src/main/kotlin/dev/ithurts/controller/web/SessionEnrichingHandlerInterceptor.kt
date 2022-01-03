package dev.ithurts.controller.web

import dev.ithurts.repository.OrganisationMembershipRepository
import dev.ithurts.security.oauth2.AuthenticatedOAuth2User
import dev.ithurts.service.web.SessionManager
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
class SessionEnrichingHandlerInterceptor(
    private val sessionManager: SessionManager,
    private val organisationMembershipRepository: OrganisationMembershipRepository
) : HandlerInterceptor {
    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
    ): Boolean {
        request.getSession(false)?.let { session ->
            if ((request.parameterMap["sync"]?.firstOrNull()?.toBoolean() == true) || session.getAttribute("currentOrganisation.id") == null) {
                SecurityContextHolder.getContext()?.authentication?.principal?.let { user ->
                    if (user is AuthenticatedOAuth2User) {
                        val memberships = organisationMembershipRepository.getByAccountId(user.accountId)
                        if (memberships.isNotEmpty()) {
                            sessionManager.setCurrentOrganisation(session, memberships[0].organisation.id!!, memberships[0].role)
                            sessionManager.setOrganisations(session, memberships.map { it.organisation })
                        }
                    }
                }
            }
        }
        return true
    }
}