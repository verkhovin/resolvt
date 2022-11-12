package dev.ithurts.service.workspace

import dev.ithurts.service.SourceProvider
import dev.ithurts.service.workspace.WorkspaceMemberRole.MEMBER
import dev.ithurts.service.workspace.WorkspaceMemberStatus.ACTIVE
import org.bson.codecs.pojo.annotations.BsonId
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field

@Document(collection = "workspaces")
data class Workspace(
    val name: String,
    val sourceProvider: SourceProvider,
    val type: WorkspaceType,
    val externalId: String,
    @Field
    val sourceProviderApplicationCredentials: SourceProviderApplicationCredentials,
    val active: Boolean = true,
    val members: List<WorkspaceMember> = listOf(),
    @BsonId
    val _id: String? = null,
) {
    val id: String
        get() = _id!!

    init {
        if (sourceProvider == SourceProvider.BITBUCKET && sourceProviderApplicationCredentials.secret == null) {
            throw IllegalArgumentException("Source provider secret must be provided for BITBUCKET")
        }
    }

    fun addMember(
        account: String,
        role: WorkspaceMemberRole = MEMBER,
        status: WorkspaceMemberStatus = ACTIVE,
    ): Workspace {
        return this.copy(
            members = members + WorkspaceMember(account, role, status)
        )
    }

    fun getMember(account: String): WorkspaceMember? {
        return members.find { it.accountId == account }
    }

    fun connectWithSourceProviderApplication(sourceProviderApplicationCredentials: SourceProviderApplicationCredentials): Workspace {
        if (this.sourceProviderApplicationCredentials != sourceProviderApplicationCredentials) {
            throw IllegalArgumentException("Wrong app credentials provided")
        }
        return this.copy(
            active = true
        )
    }

    fun deactivate(): Workspace = this.copy(active = false)
}