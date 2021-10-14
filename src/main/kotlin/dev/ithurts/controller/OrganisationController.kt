package dev.ithurts.controller

import dev.ithurts.security.AuthenticatedOAuth2User
import dev.ithurts.sourceprovider.SourceProviderCommunicationService
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("organisations")
class OrganisationController(private val sourceProviderCommunicationService: SourceProviderCommunicationService) {
    @GetMapping("/new")
    fun newPage(@AuthenticationPrincipal authentication: AuthenticatedOAuth2User, model: Model) =
        "organisation/new".apply {
            model.addAttribute(
                "externalOrganisations",
                sourceProviderCommunicationService.getOwnedExternalOrganisations()
            )
        }
}