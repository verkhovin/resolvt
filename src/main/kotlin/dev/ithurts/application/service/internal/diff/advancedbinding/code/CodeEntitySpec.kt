package dev.ithurts.application.service.internal.diff.advancedbinding.code

import dev.ithurts.application.model.LineRange

data class CodeEntitySpec(
    val type: String,
    val name: String,
    val parameters: List<String>,
    val parent: CodeEntitySpec?,
    val lines: LineRange
)