package dev.resolvt.api.rest

import dev.resolvt.service.debt.model.DebtReport
import dev.resolvt.query.model.DebtDto
import dev.resolvt.query.DebtQueryRepository
import dev.resolvt.service.debt.DebtService
import dev.resolvt.service.repository.RepositoryInfo
import dev.resolvt.service.repository.RepositoryInfoService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.net.URI

@RestController
@RequestMapping("api/debts")
class DebtController(
    private val debtService: DebtService,
    private val debtQueryRepository: DebtQueryRepository,
    private val repositoryInfoService: RepositoryInfoService,
) {
    @PostMapping
    fun reportDebt(@RequestBody techDebtReport: DebtReport): ResponseEntity<Any> {
        val repositoryInfo: RepositoryInfo = repositoryInfoService.parseRemoteUrl(techDebtReport.remoteUrl)
        val debtId = debtService.createDebt(techDebtReport, repositoryInfo)
        return ResponseEntity.created(URI.create("https://resolvt.dev/api/debt/$debtId"))
            .build()
    }
    @PutMapping("/{id}")
    fun updateDebt(@PathVariable id: String, @RequestBody debtDto: DebtReport): ResponseEntity<Any> {
        debtService.update(id, debtDto)
        return ResponseEntity.noContent().build()
    }

    @GetMapping
    fun getDebts(@RequestParam remoteUrl: String): List<DebtDto> {
        val repositoryInfo = repositoryInfoService.parseRemoteUrl(remoteUrl)
        return debtQueryRepository.queryRepositoryActiveDebts(repositoryInfo)
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
}