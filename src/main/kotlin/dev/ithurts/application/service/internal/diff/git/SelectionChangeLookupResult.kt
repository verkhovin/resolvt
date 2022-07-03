package dev.ithurts.application.service.internal.diff.git

import dev.ithurts.application.model.LineRange

data class SelectionChangeLookupResult(
    val position: LineRange,
    val wasSelectedCodeChanged: Boolean
)
