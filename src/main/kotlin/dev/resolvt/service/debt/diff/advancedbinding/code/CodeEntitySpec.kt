package dev.resolvt.service.debt.diff.advancedbinding.code

import dev.resolvt.application.model.LineRange

data class CodeEntitySpec(
    val type: String,
    val name: String,
    val parameters: List<String>,
    val parent: CodeEntitySpec?,
    val lines: LineRange
)