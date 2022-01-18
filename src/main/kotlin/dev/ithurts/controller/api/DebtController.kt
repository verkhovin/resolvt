package dev.ithurts.controller.api

import dev.ithurts.application.dto.debt.DebtDTO
import dev.ithurts.application.dto.TechDebtReport
import dev.ithurts.application.query.DebtQueryRepository
import dev.ithurts.application.service.DebtApplicationService
import dev.ithurts.application.service.RepositoryInfoService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.net.URI

@RestController
@RequestMapping("api")
class DebtController(
    private val debtApplicationService: DebtApplicationService,
    private val debtQueryRepository: DebtQueryRepository,
    private val repositoryInfoService: RepositoryInfoService
) {
    @PostMapping("/debts")
    fun reportDebt(@RequestBody techDebtReport: TechDebtReport): ResponseEntity<Any> {
        val debtId = debtApplicationService.createDebt(techDebtReport)
        return ResponseEntity.created(URI.create("https://ithurts.dev/api/debt/$debtId"))
            .build()
    }

    @GetMapping("/debts")
    fun getDebts(@RequestParam remoteUrl: String): List<DebtDTO> {
        val repositoryInfo = repositoryInfoService.parseRemoteUrl(remoteUrl)
        return debtQueryRepository.queryRepositoryActiveDebts(repositoryInfo)
    }
}