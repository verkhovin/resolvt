package dev.ithurts.application.service.internal.sourceprovider

import dev.ithurts.application.service.internal.sourceprovider.model.SourceProviderRepository

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

    fun checkIsMember(accessToken: String, workspaceId: String, accountId: String)
}