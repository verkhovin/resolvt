package dev.ithurts.api.web

import dev.ithurts.service.workspace.Workspace
import dev.ithurts.service.workspace.WorkspaceMemberRole
import org.springframework.stereotype.Service
import javax.servlet.http.HttpSession

@Service
class SessionManager {
    fun setCurrentOrganisation(session: HttpSession, workspaceId: String, workspaceMemberRole: WorkspaceMemberRole) {
        session.setAttribute("currentOrganisation.id", workspaceId)
        session.setAttribute("currentOrganisation.role", workspaceMemberRole.toString())
    }

    fun setOrganisations(session: HttpSession, workspaces: List<Workspace>) {
        session.setAttribute("organisations", workspaces)
    }
}