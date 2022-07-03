package dev.ithurts.domain.workspace

import dev.ithurts.domain.SourceProvider

data class SourceProviderWorkspace (
    val id: String,
    val name: String,
    val sourceProvider: SourceProvider
)