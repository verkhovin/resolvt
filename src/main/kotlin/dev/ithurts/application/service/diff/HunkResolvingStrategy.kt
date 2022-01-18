package dev.ithurts.application.service.diff

import dev.ithurts.domain.debt.Debt
import io.reflectoring.diffparser.api.model.Hunk
import io.reflectoring.diffparser.api.model.Line
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class HunkResolvingStrategy {
    fun processHunk(debt: Debt, hunk: Hunk): Boolean {
        var needSave = false
        var leftCursor = hunk.fromFileRange.lineStart - 1
        var rightCursor = hunk.fromFileRange.lineStart - 1
        var startOffset = 0
        var hadEndInTheHunk = false
        var hadChangesAbove = false
        for (line in hunk.lines) {
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
                log.info("Found start of debt at line $leftCursor")
                if (line.lineType == Line.LineType.FROM) {
                    log.info("Marking as probably resolved: code deleted")
                    debt.codeDeleted()
                    needSave = true
                }
                if (leftCursor != rightCursor) {
                    log.info("Setting offset for start")
                    startOffset = rightCursor - leftCursor
                    needSave = true
                }
                hadChangesAbove = false
            }

            if (leftCursor == debt.endLine) {
                log.info("Found end of debt at line $leftCursor")
                hadEndInTheHunk = true
                if (leftCursor != rightCursor) {
                    log.info("Setting offset for end")
                    debt.endLine += rightCursor - leftCursor
                    needSave = true
                }

                if (line.lineType == Line.LineType.FROM) {
                    log.info("Marking as probably resolved: code deleted")
                    debt.codeDeleted()
                    needSave = true
                }

                if (line.lineType == Line.LineType.NEUTRAL && hadChangesAbove) {
                    log.info("Marking as probably resolved: partly changed")
                    debt.partlyChanged()
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
            log.info("End line not found in the hunk, adjusting end position")
            debt.endLine += rightCursor - leftCursor
            // If end line was not found in the hunk, and we had changes between debt start and end of the hunk
            // we assume that the debt was resolved partly
            if (hadChangesAbove) {
                log.info("Marking as probably resolved: partly changed")
                debt.partlyChanged()
            }
            needSave = true
        }
        return needSave
    }

    companion object {
        val log = LoggerFactory.getLogger(HunkResolvingStrategy::class.java)
    }
}