package dev.ithurts.controller

import dev.ithurts.controller.dto.OrganisationCreationRequest
import dev.ithurts.security.AuthenticatedOAuth2User
import dev.ithurts.service.OrganisationService
import dev.ithurts.sourceprovider.SourceProviderCommunicationService
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("organisations")
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
        @ModelAttribute("creationRequest") creationRequest: OrganisationCreationRequest
    ): String {
        val organisationId: Long = organisationService.createOrganisationFromExternalOne(
            creationRequest.externalOrganisationId,
            authentication.account
        )
        return "redirect:/dashboard"
    }
}