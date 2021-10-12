package dev.ithurts.controller

import dev.ithurts.security.AuthenticatedOAuth2User
import dev.ithurts.service.AccountService
import dev.ithurts.service.OrganisationService
import org.springframework.security.core.AuthenticatedPrincipal
import org.springframework.security.core.Authentication
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping

@Controller
class IndexController(
    private val organisationService: OrganisationService
) {
    @GetMapping("/")
    fun index() = "index"

    @GetMapping("/dashboard")
    fun dashboard(@AuthenticationPrincipal authentication: AuthenticatedOAuth2User, model: Model) = "dashboard".apply {
        val organisations = organisationService.getByMemberAccountId(authentication.id)
        model.addAttribute("organisations", organisations)
    }
}