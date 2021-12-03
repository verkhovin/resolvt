package dev.ithurts.api

import dev.ithurts.model.api.TechDebtReport
import dev.ithurts.service.DebtService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.net.URI

@RestController
@RequestMapping("api")
class DebtController(
    private val debtService: DebtService
) {
    @PostMapping("/debts")
    fun reportDebt(@RequestBody techDebtReport: TechDebtReport): ResponseEntity<Any> {
        val debtId = debtService.createDebt(techDebtReport)
        return ResponseEntity.created(URI.create("https://ithurts.dev/api/debt/$debtId"))
            .build()
    }
}