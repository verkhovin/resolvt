package dev.ithurts.service.debt.model

import dev.ithurts.service.Language

data class AdvancedBinding(
    val language: Language,
    val type: String,
    val name: String,
    val params: List<String>,
    val parent: String?
)