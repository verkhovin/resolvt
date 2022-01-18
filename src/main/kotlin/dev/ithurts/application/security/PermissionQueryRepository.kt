package dev.ithurts.application.security

import dev.ithurts.application.dto.RepositoryDto
import dev.ithurts.application.service.RepositoryInfo
import dev.ithurts.domain.repository.Repository
import dev.ithurts.domain.workspace.Workspace
import dev.ithurts.domain.workspace.WorkspaceMember
import javax.persistence.EntityManager
import javax.persistence.Tuple

@org.springframework.stereotype.Repository
class PermissionQueryRepository(
    private val entityManager: EntityManager
) {
    fun getWorkspaceMemberByRepositoryId(id: Long, accountId: Long): WorkspaceMember? {
        val result = entityManager.createQuery(
            "SELECT w FROM Repository r " +
                    "LEFT JOIN Workspace w ON w.id = r.workspaceId " +
                    "WHERE r.id = :repositoryId",
            Tuple::class.java
        ).setParameter("repositoryId", id).singleResult

        return result.get(0, Workspace::class.java).members.firstOrNull { accountId == it.accountId }
    }

    fun getWorkspaceMemberByRepositoryInfo(repository: RepositoryInfo, accountId: Long): WorkspaceMember? {
        val result = entityManager.createQuery(
            "SELECT w FROM Repository r " +
                    "LEFT JOIN Workspace w ON w.id = r.workspaceId " +
                    "WHERE r.name = :repositoryName AND w.externalId = :workspaceExternalId AND w.sourceProvider = :sourceProvider",
            Tuple::class.java
        ).setParameter("repositoryName", repository.name)
            .setParameter("workspaceExternalId", repository.workspaceExternalId)
            .setParameter("sourceProvider", repository.sourceProvider).singleResult

        return result.get(0, Workspace::class.java).members.firstOrNull { accountId == it.accountId }
    }

    private fun toDto(tuple: Tuple): RepositoryDto {
        val repository = tuple.get(0, Repository::class.java)
        val workspace = tuple.get(1, Workspace::class.java)
        return RepositoryDto(
            repository.identity,
            repository.name,
            workspace.identity
        )

    }
}