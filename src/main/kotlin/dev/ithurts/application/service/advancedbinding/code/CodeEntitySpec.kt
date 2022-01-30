package dev.ithurts.application.service.advancedbinding.code

import dev.ithurts.application.LineRange

data class CodeEntitySpec(
    val type: String,
    val name: String,
    val parameters: List<String>,
    val parent: CodeEntitySpec?,
    val lines: LineRange
)