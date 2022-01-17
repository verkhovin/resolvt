package dev.ithurts.domain.workspace

import dev.ithurts.domain.SourceProviderWorkspace

class WorkspaceFactory {
    companion object {
        fun fromBitbucketWorkspace(
            ownerAccountId: Long,
            sourceProviderWorkspace: SourceProviderWorkspace,
            sourceProviderApplicationCredentials: SourceProviderApplicationCredentials
        ): Workspace {
            val workspace = Workspace(
                sourceProviderWorkspace.name,
                sourceProviderWorkspace.sourceProvider,
                sourceProviderWorkspace.id,
                sourceProviderApplicationCredentials,
                true
            )
            workspace.addMember(ownerAccountId, WorkspaceMemberRole.ADMIN)
            return workspace
        }
    }
}