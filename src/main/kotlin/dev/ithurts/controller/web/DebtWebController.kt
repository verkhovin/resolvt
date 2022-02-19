package dev.ithurts.controller.web

import dev.ithurts.application.query.DebtQueryRepository
import dev.ithurts.application.DebtService
import dev.ithurts.controller.web.page.*
import dev.ithurts.domain.workspace.WorkspaceRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpSession

@Controller
@RequestMapping("/debts")
class DebtWebController(
    private val debtService: DebtService,
    private val debtQueryRepository: DebtQueryRepository,
    private val workspaceRepository: WorkspaceRepository
) {

    @GetMapping("/{id}")
    fun debtPage(@PathVariable id: String, model: Model, httpSession: HttpSession): String {
        val debt = debtQueryRepository.queryDebtDetails(id)
        val page = DebtPage(debt)
        model.addAttribute("page", page)

        val workspaceId = httpSession.getAttribute("currentOrganisation.id") as String
        val workspace = workspaceRepository.findByIdOrNull(workspaceId)!!
        model.addAttribute("org", workspace) // TODO <--- to parent class

        return "debts/debt"
    }


    @GetMapping("/{debtId}/edit")
    fun editPage(@PathVariable debtId: String, model: Model, httpSession: HttpSession): String {
        val debt = debtQueryRepository.queryDebt(debtId)
        val page = DebtEditPage(debt)
        model.addAttribute("page", page)
        model.addAttribute("form", page.form)

        val workspaceId = httpSession.getAttribute("currentOrganisation.id") as String
        val workspace = workspaceRepository.findByIdOrNull(workspaceId)!!
        model.addAttribute("org", workspace) // TODO <--- to parent class

        return "debts/edit"
    }

    @GetMapping("/{debtId}/{bindingId}/edit")
    fun bindingEditPage(
        @PathVariable debtId: String,
        @PathVariable bindingId: String,
        model: Model,
        httpSession: HttpSession
    ): String {
        val details = debtQueryRepository.queryDebtDetails(debtId)
        val page = BindingEditPage(details.debt, details.debt.bindings.first { it.id == bindingId })
        model.addAttribute("page", page)
        model.addAttribute("form", page.bindingForm)
        model.addAttribute("advancedForm", page.advancedBindingEditForm)

        val workspaceId = httpSession.getAttribute("currentOrganisation.id") as String
        val workspace = workspaceRepository.findByIdOrNull(workspaceId)!!
        model.addAttribute("org", workspace) // TODO <--- to parent class

        return "debts/edit-binding"
    }

    @GetMapping("/{debtId}/delete")
    fun deleteDebt(@PathVariable debtId: String): String {
        debtService.deleteDebt(debtId)
        return "redirect:/dashboard"
    }

    @PostMapping("/{debtId}/vote")
    fun vote(@PathVariable debtId: String): ResponseEntity<Any> {
        debtService.vote(debtId)
        return ResponseEntity.ok().build()
    }

    @PostMapping("/{debtId}/downVote")
    fun downVote(@PathVariable debtId: String): ResponseEntity<Any> {
        debtService.downVote(debtId)
        return ResponseEntity.ok().build()
    }

    @PostMapping("/{debtId}/edit")
    fun edit(
        @PathVariable debtId: String, @ModelAttribute("form") form: DebtEditForm,
        @RequestParam(value = "action") action: String
    ): String {
        debtService.edit(debtId, form)
        return "redirect:/dashboard"
    }

    @PostMapping("/{debtId}/{bindingId}/edit")
    fun editBinding(
        @PathVariable debtId: String,
        @PathVariable bindingId: String,
        @RequestParam() type: String,
        @ModelAttribute("form") form: BindingEditForm,
        @ModelAttribute("advancedForm") advancedForm: AdvancedBindingEditForm
    ): String {
        when (type) {
            "basic" -> debtService.editBinding(debtId, bindingId, form)
            "advanced" -> debtService.editAdvancedBinding(debtId, bindingId, advancedForm)
            else -> throw IllegalArgumentException("Unknown type: $type")
        }
        return "redirect:/debts/${debtId}"
    }

}