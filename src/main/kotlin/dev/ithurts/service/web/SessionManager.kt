package dev.ithurts.service.web

import dev.ithurts.model.organisation.Organisation
import dev.ithurts.security.AuthenticatedOAuth2User
import dev.ithurts.service.OrganisationService
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import javax.servlet.http.HttpSession

@Service
class SessionManager(
    private val organisationService: OrganisationService
) {
    fun setAccountSessionProperties(session: HttpSession) {
        if (session.getAttribute("currentOrganisation.id") == null) {
            SecurityContextHolder.getContext()?.authentication?.principal?.let { user ->
                if (user is AuthenticatedOAuth2User) {
                    val userOrganisations = organisationService.getByMemberAccountId(user.accountId)
                    if (userOrganisations.isNotEmpty()) {
                        val organisation = userOrganisations[0]
                        setOrganisationRelatedSessionAttributes(session, organisation)
                        session.setAttribute("organisations", userOrganisations)
                    }
                }
            }
        }
    }

    private fun setOrganisationRelatedSessionAttributes(
        session: HttpSession,
        organisation: Organisation
    ) {
        session.setAttribute("currentOrganisation.id", organisation.id)
        session.setAttribute("currentOrganisation.role", organisation.members[0].role.toString())
    }
}