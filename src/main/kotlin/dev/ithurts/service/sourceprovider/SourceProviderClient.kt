package dev.ithurts.service.sourceprovider

import dev.ithurts.service.account.Account
import dev.ithurts.service.sourceprovider.model.SourceProviderRepository
import dev.ithurts.service.workspace.Workspace

interface SourceProviderClient {
    val organisationOwnerRole: String
    fun getDiff(accessToken: String, workspaceExternalId: String, repository: String, spec: String): String
    fun getRepository(accessToken: String, workspaceExternalId: String, repository: String): SourceProviderRepository
    fun getFile(
        accessToken: String,
        workspaceExternalId: String,
        repository: String,
        filePath: String,
        commitHashOrBranch: String
    ): String

    // TODO rollback signature changes if they are useless
    fun checkIsMember(accessToken: String, workspace: Workspace, account: Account)
    fun getUserPrimaryEmail(accessToken: String): String
}