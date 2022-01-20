package dev.ithurts.controller.web

import dev.ithurts.application.query.DebtQueryRepository
import dev.ithurts.application.security.oauth2.AuthenticatedOAuth2User
import dev.ithurts.domain.debt.DebtStatus
import dev.ithurts.domain.workspace.Workspace
import dev.ithurts.domain.workspace.WorkspaceRepository
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
        @RequestParam("sync", required = false) sync: Boolean = false
    ): String {
        // if sync, then new organisation was created and request can contain jwt or other sensitive info
        if (sync) {
            return "redirect:/dashboard"
        }
        val workspaces = httpSession.getAttribute("organisations") as List<Workspace>?
        if (workspaces == null || workspaces.isEmpty()) {
            return "init_dashboard"
        }
        val workspaceId = httpSession.getAttribute("currentOrganisation.id") as Long
        val workspace = workspaceRepository.findByIdOrNull(workspaceId)!!
        val debts = debtQueryRepository.queryWorkspaceDebts(workspaceId)
            .sortedWith { d1, d2 ->
                when {
                    d1.status == d2.status -> d2.votes - d1.votes
                    d1.status == DebtStatus.PROBABLY_RESOLVED -> -1
                    d1.status == DebtStatus.OPEN && d2.status == DebtStatus.RESOLVED -> -1
                    d1.status == DebtStatus.OPEN && d2.status == DebtStatus.PROBABLY_RESOLVED -> 1
                    d1.status == DebtStatus.RESOLVED  -> 1
                    else -> 0
                }
            }
        model.addAttribute("debts", debts)
        model.addAttribute("org", workspace)
        return "dashboard"
    }
}