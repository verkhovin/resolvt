package dev.ithurts.controller.web

import dev.ithurts.application.query.DebtQueryRepository
import dev.ithurts.application.service.DebtApplicationService
import dev.ithurts.controller.web.page.DebtEditForm
import dev.ithurts.controller.web.page.DebtEditPage
import dev.ithurts.domain.debt.DebtStatus
import dev.ithurts.domain.workspace.WorkspaceRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpSession

@Controller
@RequestMapping("/debts")
class DebtWebController( //TODO typo
    private val debtApplicationService: DebtApplicationService,
    private val debtQueryRepository: DebtQueryRepository,
    private val workspaceRepository: WorkspaceRepository
) {
    @PostMapping("/{debtId}/vote")
    fun vote(@PathVariable debtId: Long): ResponseEntity<Any> {
        debtApplicationService.vote(debtId)
        return ResponseEntity.ok().build()
    }

    @PostMapping("/{debtId}/downVote")
    fun downVote(@PathVariable debtId: Long): ResponseEntity<Any> {
        debtApplicationService.downVote(debtId)
        return ResponseEntity.ok().build()
    }

    @GetMapping("/{debtId}/edit")
    fun editPage(@PathVariable debtId: Long, model: Model, httpSession: HttpSession): String {
        val debt = debtQueryRepository.queryDebt(debtId)
        val page = DebtEditPage(debt)
        model.addAttribute("page", page)
        model.addAttribute("form", page.form)

        val workspaceId = httpSession.getAttribute("currentOrganisation.id") as Long
        val workspace = workspaceRepository.findByIdOrNull(workspaceId)!!
        model.addAttribute("org", workspace) // TODO <--- to parent class

        return "debts/edit"
    }

    @PostMapping("/{debtId}/edit")
    fun edit(
        @PathVariable debtId: Long, @ModelAttribute("form") form: DebtEditForm,
        @RequestParam(value = "action") action: String
    ): String {
        when(action) {
            "leave_open" -> form.status = DebtStatus.OPEN
            "resolve" -> form.status = DebtStatus.RESOLVED
        }
        debtApplicationService.edit(debtId, form)
        return "redirect:/dashboard"
    }
}