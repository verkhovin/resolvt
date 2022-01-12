package dev.ithurts.controller.web

import dev.ithurts.model.organisation.Organisation
import dev.ithurts.security.oauth2.AuthenticatedOAuth2User
import dev.ithurts.service.DebtApiService
import dev.ithurts.service.OrganisationApiService
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import javax.servlet.http.HttpSession

@Controller
class IndexController(
    private val organisationApiService: OrganisationApiService,
    private val debtApiService: DebtApiService
) {
    @GetMapping("/")
    fun index() = "static/index"

    @GetMapping("/dashboard")
    fun dashboard(
        @AuthenticationPrincipal authentication: AuthenticatedOAuth2User,
        model: Model,
        httpSession: HttpSession,
        @RequestParam("sync", required = false) sync: Boolean = false
    ):String {
        // if sync, then new organisation was created and request can contain jwt or other sensitive info
        if (sync) {
            return "redirect:/dashboard"
        }
        val organisations = httpSession.getAttribute("organisations") as List<Organisation>?
        if (organisations == null || organisations.isEmpty()) {
            return "init_dashboard"
        }
        val organisationId = httpSession.getAttribute("currentOrganisation.id") as Long
        val org = organisationApiService.getById(organisationId)
        val debts = debtApiService.getActiveDebtsForOrganisation(org.id!!)
            .sortedByDescending { it.votes }
        model.addAttribute("debts", debts)
        model.addAttribute("org", org)
        return "dashboard"
    }
}