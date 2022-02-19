package dev.ithurts.application.internal.git

import dev.ithurts.application.model.LineRange
import io.reflectoring.diffparser.api.model.Diff
import io.reflectoring.diffparser.api.model.Range
import org.springframework.stereotype.Service

@Service
class GitDiffAnalyzer(
    private val hunkResolvingStrategy: HunkResolvingStrategy
) {
    fun lookupSelectionChange(initialPosition: LineRange, diffs: List<Diff>): SelectionChangeLookupResult {
        val mutator = LineRangeMutator.of(initialPosition)
        var selectionChanded = false
        diffs.forEach { diff ->
            diff.hunks.forEach hunk@{ hunk ->
                if (mutator.end < hunk.fromFileRange.lineStart) {
                    return@hunk
                }
                if (mutator.start > hunk.fromFileRange.lineEnd) {
                    val offsetChange = hunk.toFileRange.lineCount - hunk.fromFileRange.lineCount
                    mutator.start += offsetChange
                    mutator.end += offsetChange
                    return@hunk
                }
                if (mutator.start < hunk.fromFileRange.lineStart && mutator.end > hunk.fromFileRange.lineEnd) {
                    val offsetChange = hunk.toFileRange.lineCount - hunk.fromFileRange.lineCount
                    mutator.end += offsetChange
                    selectionChanded = true
                    return@hunk
                }
                hunkResolvingStrategy.processHunk(mutator, hunk)
            }
        }
        return SelectionChangeLookupResult(mutator.toLineRange(), selectionChanded)
    }
}

val Range.lineEnd: Int
    get() = this.lineStart + this.lineCount - 1
