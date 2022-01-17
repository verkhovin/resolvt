package dev.ithurts.application.service.web

import dev.ithurts.domain.workspace.Workspace
import dev.ithurts.domain.workspace.WorkspaceMemberRole
import org.springframework.stereotype.Service
import javax.servlet.http.HttpSession

@Service
class SessionManager {
    fun setCurrentOrganisation(session: HttpSession, organisationId: Long, workspaceMemberRole: WorkspaceMemberRole) {
        session.setAttribute("currentOrganisation.id", organisationId)
        session.setAttribute("currentOrganisation.role", workspaceMemberRole.toString())
    }

    fun setOrganisations(session: HttpSession, workspaces: List<Workspace>) {
        session.setAttribute("organisations", workspaces)
    }
}