package dev.ithurts.application.service.internal.diff.git

import dev.ithurts.application.model.LineRange
import dev.ithurts.application.service.internal.diff.git.DiffDirection.*
import io.reflectoring.diffparser.api.model.Diff
import io.reflectoring.diffparser.api.model.Range
import org.springframework.stereotype.Service

@Service
class GitDiffAnalyzer(
    private val hunkResolvingStrategy: HunkResolvingStrategy
) {
    fun lookupCodeRangeChange(codeRange: LineRange, diffs: List<Diff>, direction: DiffDirection = DIRECT): SelectionChangeLookupResult {
        val mutator = LineRangeMutator.of(codeRange)
        var selectionChanged = false
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
                    selectionChanged = true
                    return@hunk
                }
                selectionChanged = hunkResolvingStrategy.processHunk(mutator, hunk, direction)
            }
        }
        return SelectionChangeLookupResult(mutator.toLineRange(), selectionChanged)
    }
}

val Range.lineEnd: Int
    get() = this.lineStart + this.lineCount - 1
