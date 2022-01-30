package dev.ithurts.application.service.git

import io.reflectoring.diffparser.api.model.Hunk
import io.reflectoring.diffparser.api.model.Line
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class HunkResolvingStrategy {
    fun processHunk(mutator: LineRangeMutator, hunk: Hunk, direction: Direction = Direction.DIRECT): Boolean {
        var needSave = false
        var leftCursor = hunk.fromFileRange.lineStart - 1
        var rightCursor = hunk.fromFileRange.lineStart - 1
        var startOffset = 0
        var hadEndInTheHunk = false
//        var hadChangesAbove = false
        for (line in hunk.lines) {
            when (line.lineType) {
                direction.FROM ->  {
                    leftCursor++
                }
                direction.TO -> {
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

            if (leftCursor == mutator.start) {
//                log.info("Found start of debt at line $leftCursor")
//                if (line.lineType == direction.FROM) {
//                    log.info("Marking as probably resolved: code deleted")
//                    mutator.codeDeleted()
//                    needSave = true
//                }
                if (leftCursor != rightCursor) {
//                    log.info("Setting offset for start")
                    startOffset = rightCursor - leftCursor
                    needSave = true
                }
//                hadChangesAbove = false
            }

            if (leftCursor == mutator.end) {
                log.info("Found end of debt at line $leftCursor")
                hadEndInTheHunk = true
                if (leftCursor != rightCursor) {
                    log.info("Setting offset for end")
                    mutator.end += rightCursor - leftCursor
                    needSave = true
                }

//                if (line.lineType == direction.FROM) {
//                    log.info("Marking as probably resolved: code deleted")
//                    mutator.codeDeleted()
//                    needSave = true
//                }

//                if (hadChangesAbove) {
//                    log.info("Marking as probably resolved: partly changed")
//                    mutator.partlyChanged()
//                    needSave = true
//                }
            }

//            if (line.lineType == direction.FROM) {
//                hadChangesAbove = true
//            }
        }
        mutator.start += startOffset

        // If end line was not found in the hunk, we need to adjust end position according to changes
        if (!hadEndInTheHunk) {
            log.info("End line not found in the hunk, adjusting end position")
            mutator.end += rightCursor - leftCursor
            // If end line was not found in the hunk, and we had changes between debt start and end of the hunk
            // we assume that the debt was resolved partly
//            if (hadChangesAbove) {
//                log.info("Marking as probably resolved: partly changed")
//                mutator.partlyChanged()
//            }
            needSave = true
        }
        return needSave
    }

    companion object {
        val log: Logger = LoggerFactory.getLogger(HunkResolvingStrategy::class.java)
    }
}

enum class Direction(
    val FROM: Line.LineType,
    val TO: Line.LineType
) {
    DIRECT(Line.LineType.FROM, Line.LineType.TO),
    REVERSE(Line.LineType.TO, Line.LineType.FROM)
}