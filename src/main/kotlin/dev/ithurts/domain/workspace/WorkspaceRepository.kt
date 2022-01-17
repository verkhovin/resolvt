package dev.ithurts.domain.workspace

import dev.ithurts.domain.SourceProvider
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository

interface WorkspaceRepository : CrudRepository<Workspace, Long> {
    @Query("from Workspace as o join fetch o.members as m where m.account.id = :accountId")
    fun getByMemberAccountId(accountId: Long): List<Workspace>

    fun findBySourceProviderAndExternalId(sourceProvider: SourceProvider, externalId: String): Workspace?

    fun getByClientKey(clientKey: String): Workspace?
}