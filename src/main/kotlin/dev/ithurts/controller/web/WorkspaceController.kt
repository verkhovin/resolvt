package dev.ithurts.controller.web

import dev.ithurts.controller.web.dto.MemberInvitationRequest
import dev.ithurts.application.security.oauth2.AuthenticatedOAuth2User
import dev.ithurts.application.service.WorkspaceApplicationService
import dev.ithurts.application.service.web.SessionManager
import dev.ithurts.domain.workspace.WorkspaceRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpSession

@Controller
@RequestMapping("organisations")
class WorkspaceController(
    private val workspaceApplicationService: WorkspaceApplicationService,
    private val workspaceRepository: WorkspaceRepository,
    private val sessionManager: SessionManager
) {
    @GetMapping("/invite")
    fun memberInvitePage(model: Model, httpSession: HttpSession) = "organisation/invite".apply {
        val workspaceId = httpSession.getAttribute("currentOrganisation.id") as Long
        val org = workspaceRepository.findByIdOrNull(workspaceId)
        model.addAttribute("org", org)
        model.addAttribute("memberInvitationRequest", MemberInvitationRequest(""))
    }

    @GetMapping("{id}/select")
    fun selectCurrentOrganisation(
        @AuthenticationPrincipal authentication: AuthenticatedOAuth2User,
        @PathVariable id: Long,
        session: HttpSession
    ): String {
        val workspace = workspaceRepository.findByIdOrNull(id)!!
        workspace.getMember(authentication.accountId)?.let {member ->
            sessionManager.setCurrentOrganisation(session, id, member.role)
        }
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