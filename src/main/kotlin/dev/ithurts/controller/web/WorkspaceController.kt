package dev.ithurts.controller.web

import dev.ithurts.controller.web.dto.MemberInvitationRequest
import dev.ithurts.application.security.oauth2.AuthenticatedOAuth2User
import dev.ithurts.application.service.WorkspaceApplicationService
import dev.ithurts.application.service.web.SessionManager
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpSession

@Controller
@RequestMapping("organisations")
class WorkspaceController(
    private val workspaceApplicationService: WorkspaceApplicationService,
    private val sessionManager: SessionManager
) {
    @GetMapping("/invite")
    fun memberInvitePage(model: Model, httpSession: HttpSession) = "organisation/invite".apply {
        val organisationId = httpSession.getAttribute("currentOrganisation.id") as Long
        val org = workspaceApplicationService.getById(organisationId)
        model.addAttribute("org", org)
        model.addAttribute("memberInvitationRequest", MemberInvitationRequest(""))
    }

    @GetMapping("{id}/select")
    fun selectCurrentOrganisation(
        @AuthenticationPrincipal authentication: AuthenticatedOAuth2User,
        @PathVariable id: Long,
        session: HttpSession
    ): String {
        val membership = workspaceApplicationService.getMembership(id, authentication.accountId)
        sessionManager.setCurrentOrganisation(session, membership.organisation.id!!, membership.role)
        return "redirect:/dashboard"
    }

    @PostMapping("/invite")
    fun inviteMember(
        @ModelAttribute("memberInvitationRequest") memberInvitationRequest: MemberInvitationRequest,
        httpSession: HttpSession
    ): String {
        workspaceApplicationService.addMemberByEmail(
            httpSession.getAttribute("currentOrganisation.id") as Long,
            memberInvitationRequest.email
        )
        return "redirect:/dashboard"
    }
}