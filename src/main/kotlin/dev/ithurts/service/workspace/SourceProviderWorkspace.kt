package dev.ithurts.service.workspace

import dev.ithurts.service.SourceProvider

data class SourceProviderWorkspace (
    val id: String,
    val name: String,
    val sourceProvider: SourceProvider
)