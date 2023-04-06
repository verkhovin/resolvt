package dev.resolvt.api.web

import dev.resolvt.query.DebtQueryRepository
import dev.resolvt.api.web.oauth2.AuthenticatedOAuth2User
import dev.resolvt.configuration.ApplicationProperties
import dev.resolvt.service.beta.PrivateBetaService
import dev.resolvt.service.workspace.Workspace
import dev.resolvt.service.workspace.WorkspaceCreatedWaitingService
import dev.resolvt.service.workspace.WorkspaceRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import javax.servlet.http.HttpSession

@Controller
class IndexController(
    private val workspaceRepository: WorkspaceRepository,
    private val debtQueryRepository: DebtQueryRepository,
    private val applicationProperties: ApplicationProperties,
    private val privateBetaService: PrivateBetaService,
    private val workspaceCreatedWaitingService: WorkspaceCreatedWaitingService
) {
    @GetMapping("/")
    fun index(@AuthenticationPrincipal authentication: AuthenticatedOAuth2User?): String {
        if (authentication != null) {
            return "redirect:/dashboard"
        }
        if (!applicationProperties.showMainPage) {
            return "redirect:/login"
        }
        return "static/index"
    }

    @GetMapping("/dashboard")
    fun dashboard(
        @AuthenticationPrincipal authentication: AuthenticatedOAuth2User,
        model: Model,
        httpSession: HttpSession,
        @RequestParam("sync", required = false) sync: Boolean = false,
        @RequestParam("status", required = false) status: String? = null,
        @RequestParam("source", required = false) source: String? = null
    ): String {
        // if sync, then new organisation was created and request can contain jwt or other sensitive info
        if (sync) {
            return "redirect:/dashboard"
        }
        val workspaces = httpSession.getAttribute("organisations") as List<Workspace>?
        if (source == "github_app") {
            val acquaintedWorkspaceIds = workspaces?.map { it.id }?.toSet() ?: emptySet()
            workspaceCreatedWaitingService.waitUntilWorkspaceIsCreated(authentication.accountId,
                acquaintedWorkspaceIds, applicationProperties.github!!.workspaceCreationWaiterSeconds)
            return "redirect:/dashboard"
        }
        if (workspaces == null || workspaces.isEmpty()) {
            return "init_dashboard"
        }
        val workspaceId = httpSession.getAttribute("currentOrganisation.id") as String
        val workspace = workspaceRepository.findByIdOrNull(workspaceId)!!
        val debts = debtQueryRepository.queryWorkspaceDebts(workspaceId, status == "resolved")
            .sortedByDescending {it.cost}
        model.addAttribute("debts", debts)
        model.addAttribute("org", workspace)
        model.addAttribute("resolved", status == "resolved")
        return "dashboard"
    }

    @PostMapping("/beta/request")
    fun requestBeta(@RequestParam("email") email: String) {
        privateBetaService.createBetaAccessRequest(email)
    }
}