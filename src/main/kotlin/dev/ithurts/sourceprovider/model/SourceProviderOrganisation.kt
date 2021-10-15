package dev.ithurts.sourceprovider.model

import dev.ithurts.model.SourceProvider

data class SourceProviderOrganisation (
    val id: String,
    val name: String,
    val sourceProvider: SourceProvider
)