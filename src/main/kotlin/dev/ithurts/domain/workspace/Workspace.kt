package dev.ithurts.domain.workspace

import dev.ithurts.domain.SourceProvider
import dev.ithurts.domain.repository.Repository
import dev.ithurts.domain.workspace.WorkspaceMemberRole.*
import dev.ithurts.domain.workspace.WorkspaceMemberStatus.*
import javax.persistence.*

@Entity
@Table(indexes = [
    Index(name = "unique_externalId_sourceProvider", columnList = "externalId, sourceProvider", unique = true),
    Index(name = "clientKey", columnList = "clientKey")
])
class Workspace(
    val name: String,
    @Enumerated(EnumType.STRING)
    val sourceProvider: SourceProvider,
    val externalId: String,
    @Embedded
    var sourceProviderApplicationCredentials: SourceProviderApplicationCredentials,
    var active: Boolean = true,
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private val _id: Long? = null
) {
    @ElementCollection(fetch = FetchType.EAGER)
    val members: MutableList<WorkspaceMember> = mutableListOf()
    val id = _id ?: throw IllegalStateException("Accessing Id of not persisted entity")

    fun addMember(
        account: Long,
        role: WorkspaceMemberRole = MEMBER,
        status: WorkspaceMemberStatus = ACTIVE
    ) {
        members.add(WorkspaceMember(account, role, status))
    }

    fun getMember(account: Long): WorkspaceMember? {
        return members.find { it.accountId == account }
    }

    fun connectWithSourceProviderApplication(sourceProviderApplicationCredentials: SourceProviderApplicationCredentials) {
        this.sourceProviderApplicationCredentials = sourceProviderApplicationCredentials
        this.active = true
    }

    fun deactivate() {
        this.active = false
    }

    fun checkAccountHasPermission(accountId: Long, requiredRole: WorkspaceMemberRole): Boolean {
        val member = members.firstOrNull { it.accountId == accountId && it.isActive } ?: return false
        return member.role == requiredRole || (member.role == ADMIN && requiredRole == MEMBER)
    }
}