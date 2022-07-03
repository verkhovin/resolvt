package dev.ithurts.application.service.internal.diff.git

import dev.ithurts.application.model.LineRange

data class LineRangeMutator(
    var start: Int,
    var end: Int,
) {
    fun toLineRange(): LineRange {
        return LineRange(start, end)
    }

    companion object {
        fun of(lineRange: LineRange) = LineRangeMutator(
            lineRange.start,
            lineRange.end
        )
    }
}