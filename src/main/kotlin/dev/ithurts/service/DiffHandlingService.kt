package dev.ithurts.service

import dev.ithurts.model.debt.Debt
import dev.ithurts.model.debt.DebtStatus
import io.reflectoring.diffparser.api.DiffParser
import io.reflectoring.diffparser.api.model.Diff
import io.reflectoring.diffparser.api.model.Hunk
import io.reflectoring.diffparser.api.model.Line.LineType.*
import io.reflectoring.diffparser.api.model.Range
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import kotlin.math.max

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

        val probablyAffectedDebts = debtService.getDebtsForFiles(parsedDiffs.map { it.fromFileName })
        val diffsNeedToProcess = parsedDiffs.groupBy { it.fromFileName.substringAfter("/").substringBefore(" ") }

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
                    return@hunk false
                }
                if (debt.startLine > hunk.fromFileRange.lineEnd) {
                    val offsetChange = hunk.toFileRange.lineCount - hunk.fromFileRange.lineCount
                    debt.startLine += offsetChange
                    debt.endLine += offsetChange
                    return@hunk true
                }
                if (debt.startLine < hunk.fromFileRange.lineStart && debt.endLine > hunk.fromFileRange.lineEnd) {
                    val offsetChange = hunk.toFileRange.lineCount - hunk.fromFileRange.lineCount
                    debt.endLine += offsetChange
                    debt.status = DebtStatus.PROBABLY_RESOLVED_PARTLY_CHANGED
                    return@hunk true
                }
                hunkResolvingStrategy.processHunk(debt, hunk)
            }
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(DiffHandlingService::class.java)
    }

    val Range.lineEnd: Int
        get() = this.lineStart + this.lineCount - 1
}