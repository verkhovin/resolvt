package dev.resolvt.service.debt.model

import dev.resolvt.service.Language

data class AdvancedBinding(
    val language: Language,
    val type: String,
    val name: String,
    val params: List<String>,
    val parent: String?
)