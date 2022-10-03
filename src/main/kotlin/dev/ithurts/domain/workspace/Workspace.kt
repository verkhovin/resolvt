package dev.ithurts.domain.workspace

import dev.ithurts.domain.SourceProvider
import dev.ithurts.domain.workspace.WorkspaceMemberRole.MEMBER
import dev.ithurts.domain.workspace.WorkspaceMemberStatus.ACTIVE
import org.bson.codecs.pojo.annotations.BsonId
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field

@Document(collection = "workspaces")
data class Workspace(
    val name: String,
    val sourceProvider: SourceProvider,
    val externalId: String,
    @Field
    val sourceProviderApplicationCredentials: SourceProviderApplicationCredentials,
    val active: Boolean = true,
    val members: MutableList<WorkspaceMember> = mutableListOf(),
    @BsonId
    val _id: String? = null,
) {
    val id: String
        get() = _id!!

    fun addMember(
        account: String,
        role: WorkspaceMemberRole = MEMBER,
        status: WorkspaceMemberStatus = ACTIVE,
    ) {
        members.add(WorkspaceMember(account, role, status))
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