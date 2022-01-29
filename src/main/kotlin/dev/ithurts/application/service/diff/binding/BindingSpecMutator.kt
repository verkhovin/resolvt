package dev.ithurts.application.service.diff.binding

import dev.ithurts.application.service.diff.BindingSpec

data class BindingSpecMutator(
    var filePath: String,
    var startLine: Int,
    var endLine: Int,
    var events: MutableList<Any> = mutableListOf()
) {
    fun toAdjustmentResult(): BindingAdjustmentResult {
        return BindingAdjustmentResult(
            BindingSpec(
                filePath = filePath,
                startLine = startLine,
                endLine = endLine
            ),
            events
        )
    }

    companion object {
        fun of(bindingSpec: BindingSpec) = BindingSpecMutator(
            bindingSpec.filePath,
            bindingSpec.startLine,
            bindingSpec.endLine
        )
    }
}