package dev.ithurts.controller.web

import dev.ithurts.exception.EntityNotFoundException
import dev.ithurts.model.organisation.Organisation
import dev.ithurts.security.oauth2.AuthenticatedOAuth2User
import dev.ithurts.service.DebtApiService
import dev.ithurts.service.core.OrganisationService
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import javax.servlet.http.HttpSession

@Controller
class IndexController(
    private val organisationService: OrganisationService,
    private val debtApiService: DebtApiService
) {
    @GetMapping("/")
    fun index() = "index"

    @GetMapping("/dashboard")
    fun dashboard(
        @AuthenticationPrincipal authentication: AuthenticatedOAuth2User,
        model: Model,
        httpSession: HttpSession
    ):String {
        val organisations = httpSession.getAttribute("organisations") as List<Organisation>?
        if (organisations == null || organisations.isEmpty()) {
            return "init_dashboard"
        }
        val organisationId = httpSession.getAttribute("currentOrganisation.id") as Long
        val org = organisationService.getById(organisationId)
            ?: throw EntityNotFoundException("organisation", "id", organisationId.toString())
        val debts = debtApiService.getActiveDebtsForOrganisation(org.id!!)
        model.addAttribute("debts", debts)
        model.addAttribute("org", org)
        return "dashboard"
    }
}