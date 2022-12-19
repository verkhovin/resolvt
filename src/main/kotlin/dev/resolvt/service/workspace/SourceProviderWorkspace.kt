package dev.resolvt.service.workspace

import dev.resolvt.service.SourceProvider

data class SourceProviderWorkspace (
    val id: String,
    val name: String,
    val sourceProvider: SourceProvider
)