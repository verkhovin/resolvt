package dev.ithurts.api.web

import dev.ithurts.query.DebtQueryRepository
import dev.ithurts.api.web.oauth2.AuthenticatedOAuth2User
import dev.ithurts.service.workspace.Workspace
import dev.ithurts.service.workspace.WorkspaceRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import javax.servlet.http.HttpSession

@Controller
class IndexController(
    private val workspaceRepository: WorkspaceRepository,
    private val debtQueryRepository: DebtQueryRepository
) {
    @GetMapping("/")
    fun index() = "static/index"

    @GetMapping("/dashboard")
    fun dashboard(
        @AuthenticationPrincipal authentication: AuthenticatedOAuth2User,
        model: Model,
        httpSession: HttpSession,
        @RequestParam("sync", required = false) sync: Boolean = false,
        @RequestParam("status", required = false) status: String? = null
    ): String {
        // if sync, then new organisation was created and request can contain jwt or other sensitive info
        if (sync) {
            return "redirect:/dashboard"
        }
        val workspaces = httpSession.getAttribute("organisations") as List<Workspace>?
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
}