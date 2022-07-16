package dev.ithurts.application.service.internal.diff

import dev.ithurts.application.model.LineRange
import dev.ithurts.application.service.internal.diff.advancedbinding.AdvancedBindingService
import dev.ithurts.application.service.internal.diff.git.DiffDirection
import dev.ithurts.application.service.internal.diff.git.GitDiffAnalyzer
import dev.ithurts.application.service.internal.git.trimDiffFilepath
import dev.ithurts.domain.debt.Binding
import dev.ithurts.domain.debt.BindingChange
import dev.ithurts.domain.debt.Debt
import dev.ithurts.domain.debt.DiffApplier
import io.reflectoring.diffparser.api.model.Diff
import org.springframework.stereotype.Service

@Service
class DiffApplyingService(
    private val gitDiffAnalyzer: GitDiffAnalyzer,
    private val advancedBindingService: AdvancedBindingService,
) : DiffApplier {
    override fun applyDiffs(debt: Debt, binding: Binding, diffs: List<Diff>, commitHash: String): List<BindingChange> {
        val currentBindingPosition = LineRange(binding.startLine, binding.endLine)
        val newFilePath = trimDiffFilepath(diffs.last().toFileName)

        val (newBindingPosition, codeChanged) = if (binding.isAdvanced()) {
            val advancedBinding = binding.advancedBinding!!
            val newLocation =
                advancedBindingService.lookupBindingLocation(debt, advancedBinding, newFilePath, commitHash)
            newLocation to hasCodeChangeInsideAdvancedBinding(newLocation, diffs)
        } else {
            val selectionChange = gitDiffAnalyzer.lookupCodeRangeChange(currentBindingPosition, diffs)
            selectionChange.position to selectionChange.wasSelectedCodeChanged
        }
        return binding.deriveChanges(
            newFilePath,
            codeChanged,
            newBindingPosition.start,
            newBindingPosition.end
        )
    }

    private fun hasCodeChangeInsideAdvancedBinding(
        newBindingLocation: LineRange,
        diffs: List<Diff>,
    ): Boolean = gitDiffAnalyzer.lookupCodeRangeChange(newBindingLocation, diffs, DiffDirection.REVERSE)
        .wasSelectedCodeChanged

}