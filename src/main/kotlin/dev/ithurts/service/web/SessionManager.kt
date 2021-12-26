package dev.ithurts.service.web

import dev.ithurts.model.organisation.Organisation
import dev.ithurts.model.organisation.OrganisationMemberRole
import org.springframework.stereotype.Service
import javax.servlet.http.HttpSession

@Service
class SessionManager {
    fun setCurrentOrganisation(session: HttpSession, organisationId: Long, organisationMemberRole: OrganisationMemberRole) {
        session.setAttribute("currentOrganisation.id", organisationId)
        session.setAttribute("currentOrganisation.role", organisationMemberRole.toString())
    }

    fun setOrganisations(session: HttpSession, organisations: List<Organisation>) {
        session.setAttribute("organisations", organisations)
    }
}