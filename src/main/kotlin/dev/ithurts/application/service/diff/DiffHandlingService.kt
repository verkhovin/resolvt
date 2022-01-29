package dev.ithurts.application.service.diff

import dev.ithurts.application.dto.PushInfo
import dev.ithurts.application.security.IntegrationAuthenticationFacade
import dev.ithurts.domain.debt.Debt
import dev.ithurts.domain.debt.DebtRepository
import io.reflectoring.diffparser.api.DiffParser
import io.reflectoring.diffparser.api.model.Diff
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class DiffHandlingService(
    private val debtDiffApplicator: DebtDiffApplicator,
    private val diffParser: DiffParser,
    private val debtRepository: DebtRepository,
    private val integrationAuthenticationFacade: IntegrationAuthenticationFacade
) {
    fun handleDiff(diff: String, pushInfo: PushInfo) {
        if (diff.isBlank()) {
            return
        }
        val parsedDiffs: List<Diff> = diffParser.parse(diff.toByteArray())
        val debtsForChangedFiles = debtsForChangedFiles(parsedDiffs)
        val filePathToDiffs: Map<String, List<Diff>> = parsedDiffs.groupBy { trimDiffFilepath(it.fromFileName) }
        debtsForChangedFiles.forEach { debt ->
            debtDiffApplicator.applyDiffs(debt, filePathToDiffs, pushInfo)
            debtRepository.save(debt)
        }
    }

//    private fun applyDiffsToDebt(
//        debt: Debt,
//        filePathToDiffs: Map<String, List<Diff>>
//    ) {
//        val debtChanged = basicDiffProcessor.processDiff(debt, filePathToDiffs)
//        if (debtChanged) {
//            if (debt.startLine > debt.endLine) {
//                log.error("Debt start line is greater than end line: $debt")
//                debt.startLine = debt.endLine
//            }
//            debtRepository.save(debt)
//        }
//    }

    private fun debtsForChangedFiles(parsedDiffs: List<Diff>): List<Debt> {
        val changedFilePaths = parsedDiffs.map { trimDiffFilepath(it.fromFileName) }
        return debtRepository.findByWorkspaceIdAndFilePaths(
            integrationAuthenticationFacade.workspace.identity,
            changedFilePaths
        )
    }

    companion object {
        private val log = LoggerFactory.getLogger(DiffHandlingService::class.java)
    }
}