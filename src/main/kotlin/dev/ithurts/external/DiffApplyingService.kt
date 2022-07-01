package dev.ithurts.external

import dev.ithurts.application.internal.advancedbinding.AdvancedBindingService
import dev.ithurts.application.internal.git.GitDiffAnalyzer
import dev.ithurts.application.internal.git.trimDiffFilepath
import dev.ithurts.application.model.LineRange
import dev.ithurts.domain.debt.Binding
import dev.ithurts.domain.debt.BindingChange
import dev.ithurts.domain.debt.Debt
import dev.ithurts.domain.debt.DiffApplier
import io.reflectoring.diffparser.api.model.Diff
import org.springframework.stereotype.Service

@Service
class DiffApplyingService(
    private val gitDiffAnalyzer: GitDiffAnalyzer,
    private val advancedBindingService: AdvancedBindingService
) : DiffApplier {
    override fun applyDiffs(debt: Debt, binding: Binding, diffs: List<Diff>, commitHash: String): List<BindingChange> {
        val currentBindingPosition = LineRange(binding.startLine, binding.endLine)
        val newFilePath = trimDiffFilepath(diffs.last().toFileName)

        val (newBindingPosition, codeChanged) = if (binding.isAdvanced()) {
            val advancedBinding = binding.advancedBinding!!
            val newLocation = advancedBindingService.lookupBindingLocation(debt, advancedBinding, newFilePath, commitHash)
            newLocation to (newLocation != LineRange(binding.startLine, binding.endLine))
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
}