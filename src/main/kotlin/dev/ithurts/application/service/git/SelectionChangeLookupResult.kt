package dev.ithurts.application.service.git

import dev.ithurts.application.LineRange

data class SelectionChangeLookupResult(
    val position: LineRange,
    val wasSelectedCodeChanged: Boolean
)
