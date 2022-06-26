package dev.ithurts.domain.workspace

import dev.ithurts.domain.SourceProvider
import dev.ithurts.domain.workspace.WorkspaceMemberRole.*
import dev.ithurts.domain.workspace.WorkspaceMemberStatus.*
import org.bson.codecs.pojo.annotations.BsonId
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field

@Document(collection = "workspaces")
data class Workspace(
    val name: String,
    val sourceProvider: SourceProvider,
    val externalId: String,
    @Field
    var sourceProviderApplicationCredentials: SourceProviderApplicationCredentials,
    var active: Boolean = true,
    val members: MutableList<WorkspaceMember> = mutableListOf(),
    @BsonId
    val _id: String? = null
) {
    val id: String
        get() = _id!!

    fun addMember(
        account: String,
        role: WorkspaceMemberRole = MEMBER,
        status: WorkspaceMemberStatus = ACTIVE
    ) {
        members.add(WorkspaceMember(account, role, status))
    }

    fun getMember(account: String): WorkspaceMember? {
        return members.find { it.accountId == account }
    }

    fun connectWithSourceProviderApplication(sourceProviderApplicationCredentials: SourceProviderApplicationCredentials) {
        this.sourceProviderApplicationCredentials = sourceProviderApplicationCredentials
        this.active = true
    }

    fun deactivate() {
        this.active = false
    }
}