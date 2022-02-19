package dev.ithurts.application.internal.sourceprovider

import dev.ithurts.application.internal.sourceprovider.model.SourceProviderRepository

interface SourceProviderClient {
    val organisationOwnerRole: String
    fun getDiff(accessToken: String, organisation: String, repository: String, spec: String): String
    fun getRepository(accessToken: String, organisation: String, repository: String): SourceProviderRepository
    fun getFile(
        accessToken: String,
        workspace: String,
        repository: String,
        filePath: String,
        commitHash: String
    ): String
}