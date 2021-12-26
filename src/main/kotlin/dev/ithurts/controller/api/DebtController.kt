package dev.ithurts.controller.api

import dev.ithurts.controller.api.dto.DebtDTO
import dev.ithurts.controller.api.dto.TechDebtReport
import dev.ithurts.service.core.DebtApiService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.net.URI

@RestController
@RequestMapping("api")
class DebtController(
    private val debtApiService: DebtApiService
) {
    @PostMapping("/debts")
    fun reportDebt(@RequestBody techDebtReport: TechDebtReport): ResponseEntity<Any> {
        val debtId = debtApiService.createDebt(techDebtReport)
        return ResponseEntity.created(URI.create("https://ithurts.dev/api/debt/$debtId"))
            .build()
    }

    @GetMapping("/debts")
    fun getDebts(@RequestParam remoteUrl: String): List<DebtDTO> {
        return debtApiService.getDebts(remoteUrl)
    }
}