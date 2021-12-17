package dev.ithurts.service

import dev.ithurts.model.debt.Debt
import dev.ithurts.model.debt.DebtStatus
import io.reflectoring.diffparser.api.model.Hunk
import io.reflectoring.diffparser.api.model.Line
import org.springframework.stereotype.Component

@Component
class HunkResolvingStrategy {
    fun processHunk(debt: Debt, hunk: Hunk): Boolean {
        var needSave: Boolean = false
        var leftCursor = hunk.fromFileRange.lineStart - 1
        var rightCursor = hunk.fromFileRange.lineStart - 1
        var startOffset = 0
        var hadEndInTheHunk = false
        var hadChangesAbove = false
        for ((i, line) in hunk.lines.withIndex()) {
            when (line.lineType) {
                Line.LineType.FROM ->  {
                    leftCursor++
                }
                Line.LineType.TO -> {
                    rightCursor++
                    continue
                }
                else -> {
                    leftCursor++
                    rightCursor++
                }
            }

            if (leftCursor < hunk.fromFileRange.lineStart) {
                continue
            }

            if (leftCursor == debt.startLine) {
                if (line.lineType == Line.LineType.FROM) {
                    debt.status = DebtStatus.PROBABLY_RESOLVED_CODE_DELETED
                    needSave = true
                }
                if (leftCursor != rightCursor) {
                    startOffset = rightCursor - leftCursor
                    needSave = true
                }
                hadChangesAbove = false;
            }

            if (leftCursor == debt.endLine) {
                hadEndInTheHunk = true
                if (leftCursor != rightCursor) {
                    debt.endLine += rightCursor - leftCursor
                    needSave = true
                }

                if (line.lineType == Line.LineType.FROM) {
                    debt.status = DebtStatus.PROBABLY_RESOLVED_CODE_DELETED
                    needSave = true
                }

                if (line.lineType == Line.LineType.NEUTRAL && hadChangesAbove) {
                    debt.status = DebtStatus.PROBABLY_RESOLVED_PARTLY_CHANGED
                    needSave = true
                }
            }

            if (line.lineType == Line.LineType.FROM) {
                hadChangesAbove = true
            }
        }
        debt.startLine += startOffset

        // If end line was not found in the hunk, we need to adjust end position according to changes
        if (!hadEndInTheHunk) {
            debt.endLine += rightCursor - leftCursor
            // If end line was not found in the hunk, and we had changes between debt start and end of the hunk
            // we assume that the debt was resolved partly
            if (hadChangesAbove) {
                debt.status = DebtStatus.PROBABLY_RESOLVED_PARTLY_CHANGED
            }
            needSave = true
        }
        return needSave
    }
}