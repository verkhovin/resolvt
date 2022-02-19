package dev.ithurts.application.model

import dev.ithurts.domain.SourceProvider
import java.io.Serializable

data class RepositoryInfo(
    val name: String,
    val workspaceExternalId: String,
    val sourceProvider: SourceProvider
): Serializable