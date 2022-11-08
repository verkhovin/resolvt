package dev.ithurts.service.repository

import dev.ithurts.service.SourceProvider
import java.io.Serializable

data class RepositoryInfo(
    val name: String,
    val workspaceExternalId: String,
    val sourceProvider: SourceProvider
): Serializable