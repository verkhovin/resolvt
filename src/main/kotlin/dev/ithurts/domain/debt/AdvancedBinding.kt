package dev.ithurts.domain.debt

import dev.ithurts.domain.Language

data class AdvancedBinding(
    val language: Language,
    val type: String,
    val name: String,
    val params: List<String>,
    val parent: String?
)