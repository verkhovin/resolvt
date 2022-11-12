package dev.ithurts.service.workspace

import dev.ithurts.service.SourceProvider
import org.springframework.data.repository.CrudRepository

interface WorkspaceRepository : CrudRepository<Workspace, String> {
//    @Query("from Workspace as o join fetch o.members as m where m.account.id = :accountId")
//    fun getByMemberAccountId(accountId: Long): List<Workspace>
//
    fun getByMembers_accountId(accountId: String): List<Workspace>

    fun findBySourceProviderAndExternalId(sourceProvider: SourceProvider, externalId: String): Workspace?

    fun getBySourceProviderAndSourceProviderApplicationCredentials_ClientKey(sourceProvider: SourceProvider, clientKey: Any): Workspace?
}