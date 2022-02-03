package dev.ithurts.controller.api

import dev.ithurts.application.dto.TechDebtReport
import dev.ithurts.application.dto.debt.DebtDto
import dev.ithurts.application.query.DebtQueryRepository
import dev.ithurts.application.service.DebtApplicationService
import dev.ithurts.application.service.RepositoryInfo
import dev.ithurts.application.service.RepositoryInfoService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.net.URI

@RestController
@RequestMapping("api/debts")
class DebtController(
    private val debtApplicationService: DebtApplicationService,
    private val debtQueryRepository: DebtQueryRepository,
    private val repositoryInfoService: RepositoryInfoService,
) {
    @PostMapping
    fun reportDebt(@RequestBody techDebtReport: TechDebtReport): ResponseEntity<Any> {
        val repositoryInfo: RepositoryInfo = repositoryInfoService.parseRemoteUrl(techDebtReport.remoteUrl)
        val debtId = debtApplicationService.createDebt(techDebtReport, repositoryInfo)
        return ResponseEntity.created(URI.create("https://ithurts.dev/api/debt/$debtId"))
            .build()
    }

    @GetMapping
    fun getDebts(@RequestParam remoteUrl: String): List<DebtDto> {
        val repositoryInfo = repositoryInfoService.parseRemoteUrl(remoteUrl)
        return debtQueryRepository.queryRepositoryActiveDebts(repositoryInfo)
    }

    @PostMapping("/{debtId}/vote")
    fun vote(@PathVariable debtId: String): ResponseEntity<Any> {
        debtApplicationService.vote(debtId)
        return ResponseEntity.ok().build()
    }

    @PostMapping("/{debtId}/downVote")
    fun downVote(@PathVariable debtId: String): ResponseEntity<Any> {
        debtApplicationService.downVote(debtId)
        return ResponseEntity.ok().build()
    }

//    @ExceptionHandler(NoResultException::class)
//    fun handleNoResult(): ResponseEntity<ItHurtsError> {
//        return ResponseEntity(
//            ItHurtsError(
//                "not_accessible_entity", "We failed to find what you are looking for. " +
//                        "The entity either does not exist or you have lack of access"
//            ), HttpStatus.NOT_FOUND
//        )
//    }
}