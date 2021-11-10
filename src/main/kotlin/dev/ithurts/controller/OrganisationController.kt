package dev.ithurts.controller

import dev.ithurts.controller.dto.MemberInvitationRequest
import dev.ithurts.controller.dto.OrganisationCreationRequest
import dev.ithurts.security.AuthenticatedOAuth2User
import dev.ithurts.service.OrganisationService
import dev.ithurts.service.web.SessionManager
import dev.ithurts.sourceprovider.SourceProviderCommunicationService
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpSession

@Controller
@RequestMapping("organisations")
class OrganisationController(
    private val organisationService: OrganisationService,
    private val sourceProviderCommunicationService: SourceProviderCommunicationService,
    private val sessionManager: SessionManager
) {
    @GetMapping("/new")
    fun newPage(@AuthenticationPrincipal authentication: AuthenticatedOAuth2User, model: Model) =
        "organisation/new".apply {
            val ownedExternalOrganisations = sourceProviderCommunicationService.getOwnedExternalOrganisations()
            model.addAllAttributes(
                mapOf(
                    "externalOrganisations" to ownedExternalOrganisations,
                    "creationRequest" to OrganisationCreationRequest(
                        ownedExternalOrganisations[0].id,
                        sourceProviderCommunicationService.getCurrentSourceProvider()
                    )
                )
            )
        }

    @PostMapping
    fun createOrganisationFromExternalOne(
        @AuthenticationPrincipal authentication: AuthenticatedOAuth2User,
        @ModelAttribute("creationRequest") creationRequest: OrganisationCreationRequest,
        model: Model
    ): String {
        organisationService.createOrganisationFromExternalOne(
            creationRequest.externalOrganisationId,
            authentication.account
        )
        return "redirect:/dashboard"
    }

    @GetMapping("/invite")
    fun memberInvitePage(model: Model) = "organisation/invite".apply {
        model.addAttribute("memberInvitationRequest", MemberInvitationRequest(""))
    }

    @GetMapping("{id}/select")
    fun selectCurrentOrganisation(
        @AuthenticationPrincipal authentication: AuthenticatedOAuth2User,
        @PathVariable id: Long,
        session: HttpSession
    ): String {
        val membership = organisationService.getMembership(id, authentication.accountId)
        sessionManager.setCurrentOrganisation(session, membership.organisation.id!!, membership.role)
        return "redirect:/dashboard"
    }

    @PostMapping("/invite")
    fun inviteMember(
        @ModelAttribute("memberInvitationRequest") memberInvitationRequest: MemberInvitationRequest,
        httpSession: HttpSession
    ): String {
        organisationService.addMemberByEmail(
            httpSession.getAttribute("currentOrganisation.id") as Long,
            memberInvitationRequest.email
        )
        return "redirect:/dashboard"
    }
}