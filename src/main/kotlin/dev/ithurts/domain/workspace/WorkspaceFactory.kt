package dev.ithurts.domain.workspace

class WorkspaceFactory {
    companion object {
        fun fromBitbucketWorkspace(
            ownerAccountId: String,
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