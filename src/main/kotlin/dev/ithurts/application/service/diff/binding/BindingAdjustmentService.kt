package dev.ithurts.application.service.diff.binding

import dev.ithurts.application.service.diff.BindingSpec
import dev.ithurts.application.service.diff.trimDiffFilepath
import io.reflectoring.diffparser.api.model.Diff
import io.reflectoring.diffparser.api.model.Range
import org.springframework.stereotype.Service

@Service
class BindingAdjustmentService(
    private val hunkResolvingStrategy: HunkResolvingStrategy
) {
    fun applyDiffs(bindingSpec: BindingSpec, diffs: List<Diff>): BindingAdjustmentResult {
        val mutator = BindingSpecMutator.of(bindingSpec)
        diffs.forEach { diff ->
            diff.hunks.forEach hunk@{ hunk ->
                if (mutator.endLine < hunk.fromFileRange.lineStart) {
                    return@hunk
                }
                if (mutator.startLine > hunk.fromFileRange.lineEnd) {
                    val offsetChange = hunk.toFileRange.lineCount - hunk.fromFileRange.lineCount
                    mutator.startLine += offsetChange
                    mutator.endLine += offsetChange
                    return@hunk
                }
                if (mutator.startLine < hunk.fromFileRange.lineStart && mutator.endLine > hunk.fromFileRange.lineEnd) {
                    val offsetChange = hunk.toFileRange.lineCount - hunk.fromFileRange.lineCount
                    mutator.endLine += offsetChange
                    return@hunk
                }
                hunkResolvingStrategy.processHunk(mutator, hunk)
            }

            if (trimDiffFilepath(diff.fromFileName) != trimDiffFilepath(diff.toFileName)) {
                mutator.filePath = trimDiffFilepath(diff.toFileName)
            }
        }
        return mutator.toAdjustmentResult()
    }
}


val Range.lineEnd: Int
    get() = this.lineStart + this.lineCount - 1
