package dev.ithurts.application.service.git

import dev.ithurts.application.LineRange

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