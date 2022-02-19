package dev.ithurts.application.internal.advancedbinding.code

import dev.ithurts.application.model.LineRange

data class CodeEntitySpec(
    val type: String,
    val name: String,
    val parameters: List<String>,
    val parent: CodeEntitySpec?,
    val lines: LineRange
)