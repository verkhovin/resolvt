package dev.ithurts.controller

import dev.ithurts.controller.dto.MemberInvitationRequest
import dev.ithurts.controller.dto.OrganisationCreationRequest
import dev.ithurts.security.AuthenticatedOAuth2User
import dev.ithurts.service.OrganisationService
import dev.ithurts.sourceprovider.SourceProviderCommunicationService
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*

@Controller
@RequestMapping("organisations")
@SessionAttributes("currentOrganisation.id", "currentOrganisation.role")
class OrganisationController(
    private val organisationService: OrganisationService,
    private val sourceProviderCommunicationService: SourceProviderCommunicationService
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

    @PostMapping("/invite")
    fun inviteMember(
        @ModelAttribute("memberInvitationRequest") memberInvitationRequest: MemberInvitationRequest,
        @ModelAttribute("currentOrganisation.id") currentOrganisationId: Long
    ): String {
        organisationService.addMemberByEmail(currentOrganisationId, memberInvitationRequest.email)
        return "redirect:/dashboard"
    }
}