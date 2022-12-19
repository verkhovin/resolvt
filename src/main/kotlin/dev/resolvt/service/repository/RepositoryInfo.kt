package dev.resolvt.service.repository

import dev.resolvt.service.SourceProvider
import java.io.Serializable

data class RepositoryInfo(
    val name: String,
    val workspaceExternalId: String,
    val sourceProvider: SourceProvider
): Serializable