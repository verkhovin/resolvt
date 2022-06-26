package dev.ithurts.external

import dev.ithurts.application.internal.git.GitDiffAnalyzer
import dev.ithurts.application.internal.git.trimDiffFilepath
import dev.ithurts.application.model.LineRange
import dev.ithurts.domain.debt.Binding
import dev.ithurts.domain.debt.BindingChange
import dev.ithurts.domain.debt.DiffApplier
import io.reflectoring.diffparser.api.model.Diff
import org.springframework.stereotype.Service

@Service
class DiffApplyingService(
    private val gitDiffAnalyzer: GitDiffAnalyzer
) : DiffApplier {
    override fun applyDiffs(binding: Binding, diffs: List<Diff>): List<BindingChange> {
        val currentBindingPosition = LineRange(binding.startLine, binding.endLine)
        val selectionChange = gitDiffAnalyzer.lookupCodeRangeChange(currentBindingPosition, diffs)
        val newFilePath = trimDiffFilepath(diffs.last().toFileName)
        val newBindingPosition: LineRange = selectionChange.position
        return binding.deriveChanges(
            newFilePath,
            selectionChange.wasSelectedCodeChanged,
            newBindingPosition.start,
            newBindingPosition.end
        )
    }
}