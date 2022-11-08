package dev.ithurts.api.web

import dev.ithurts.api.web.dto.MemberInvitationRequest
import dev.ithurts.api.web.oauth2.AuthenticatedOAuth2User
import dev.ithurts.service.workspace.WorkspaceService
import dev.ithurts.service.workspace.WorkspaceRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpSession

@Controller
@RequestMapping("organisations")
class WorkspaceController(
    private val workspaceService: WorkspaceService,
    private val workspaceRepository: WorkspaceRepository,
    private val sessionManager: SessionManager
) {
    @GetMapping("/invite")
    fun memberInvitePage(model: Model, httpSession: HttpSession) = "organisation/invite".apply {
        val workspaceId = httpSession.getAttribute("currentOrganisation.id") as String
        val org = workspaceRepository.findByIdOrNull(workspaceId)
        model.addAttribute("org", org)
        model.addAttribute("memberInvitationRequest", MemberInvitationRequest(""))
    }

    @GetMapping("{id}/select")
    fun selectCurrentOrganisation(
        @AuthenticationPrincipal authentication: AuthenticatedOAuth2User,
        @PathVariable id: String,
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
        workspaceService.addMemberByEmail(
            httpSession.getAttribute("currentOrganisation.id") as String,
            memberInvitationRequest.email
        )
        return "redirect:/dashboard"
    }
}