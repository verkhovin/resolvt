package dev.ithurts.application.service.diff.binding

import dev.ithurts.application.service.diff.BindingSpec

data class BindingAdjustmentResult(
    val bindingSpec: BindingSpec,
    val events: List<Any>
)