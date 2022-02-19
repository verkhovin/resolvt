package dev.ithurts.application.internal.git

import dev.ithurts.application.model.LineRange

data class SelectionChangeLookupResult(
    val position: LineRange,
    val wasSelectedCodeChanged: Boolean
)
