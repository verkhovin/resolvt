package dev.ithurts.application.service.diff

import dev.ithurts.domain.debt.Debt
import dev.ithurts.application.service.OrganisationAwareDebtService
import io.reflectoring.diffparser.api.DiffParser
import io.reflectoring.diffparser.api.model.Diff
import io.reflectoring.diffparser.api.model.Range
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class DiffHandlingService(
    private val hunkResolvingStrategy: HunkResolvingStrategy,
    private val diffParser: DiffParser,
    private val debtService: OrganisationAwareDebtService
) {
    fun handleDiff(diff: String) {
        if (diff.isBlank()) {
            return
        }
        val parsedDiffs: List<Diff> = diffParser.parse(diff.toByteArray())

        val probablyAffectedDebts = debtService.getDebtsForFiles(parsedDiffs.map { trimDiffFilepath(it.fromFileName) })
        val diffsNeedToProcess = parsedDiffs.groupBy { trimDiffFilepath(it.fromFileName) }

        probablyAffectedDebts.forEach { debt ->
            val debtChanged = processDiff(debt, diffsNeedToProcess[debt.filePath] ?: emptyList())
            if (debtChanged) {
                if (debt.startLine > debt.endLine) {
                    log.error("Debt start line is greater than end line: $debt")
                    debt.startLine = debt.endLine
                }
                debtService.saveDebt(debt)
            }
        }

    }

    private fun processDiff(debt: Debt, relatedDiffs: List<Diff>): Boolean {
        return relatedDiffs.any { diff ->
            diff.hunks.any hunk@{ hunk ->
                if (debt.endLine < hunk.fromFileRange.lineStart) {
                    log.info("Debt is before hunk")
                    return@hunk false
                }
                if (debt.startLine > hunk.fromFileRange.lineEnd) {
                    log.info("Debt is after hunk")
                    val offsetChange = hunk.toFileRange.lineCount - hunk.fromFileRange.lineCount
                    debt.startLine += offsetChange
                    debt.endLine += offsetChange
                    return@hunk true
                }
                if (debt.startLine < hunk.fromFileRange.lineStart && debt.endLine > hunk.fromFileRange.lineEnd) {
                    log.info("Debt is around hunk")
                    val offsetChange = hunk.toFileRange.lineCount - hunk.fromFileRange.lineCount
                    debt.endLine += offsetChange
                    debt.partlyChanged()
                    return@hunk true
                }
                hunkResolvingStrategy.processHunk(debt, hunk)
            }
        }
    }

    private fun trimDiffFilepath(filePath: String) =
        filePath.substringAfter("/").substringBefore(" ")

    companion object {
        private val log = LoggerFactory.getLogger(DiffHandlingService::class.java)
    }

    val Range.lineEnd: Int
        get() = this.lineStart + this.lineCount - 1
}