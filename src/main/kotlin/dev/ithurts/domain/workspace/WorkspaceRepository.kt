package dev.ithurts.domain.workspace

import dev.ithurts.domain.SourceProvider
import org.springframework.data.repository.CrudRepository

interface WorkspaceRepository : CrudRepository<Workspace, String> {
//    @Query("from Workspace as o join fetch o.members as m where m.account.id = :accountId")
//    fun getByMemberAccountId(accountId: Long): List<Workspace>
//
    fun getByMembers_accountId(accountId: String): List<Workspace>

    fun findBySourceProviderAndExternalId(sourceProvider: SourceProvider, externalId: String): Workspace?

    fun getBySourceProviderApplicationCredentials_ClientKey(clientKey: String): Workspace?
}