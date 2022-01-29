package dev.ithurts.application.service.diff

import dev.ithurts.domain.debt.Binding

data class BindingSpec(
    val filePath: String,
    val startLine: Int,
    val endLine: Int
) {
    companion object {
        fun of(binding: Binding): BindingSpec {
            return BindingSpec(binding.filePath, binding.startLine, binding.endLine)
        }
    }
}