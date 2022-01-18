package dev.ithurts.application.security

import dev.ithurts.application.service.RepositoryInfo
import dev.ithurts.domain.workspace.Workspace
import dev.ithurts.domain.workspace.WorkspaceMember
import javax.persistence.EntityManager
import javax.persistence.Tuple

@org.springframework.stereotype.Repository
class PermissionQueryRepository(
    private val entityManager: EntityManager
) {
    fun getWorkspaceMemberByWorkspaceId(id: Long, accountId: Long): WorkspaceMember? {
        val result = entityManager.createQuery(
            "SELECT w FROM Workspace w WHERE w.id = :workspaceId",
            Tuple::class.java
        ).setParameter("workspaceId", id).singleResult

        return result.get(0, Workspace::class.java).members.firstOrNull { accountId == it.accountId }
    }

    fun getWorkspaceMemberByRepositoryId(id: Long, accountId: Long): WorkspaceMember? {
        val result = entityManager.createQuery(
            "SELECT w FROM Repository r " +
                    "LEFT JOIN Workspace w ON w.id = r.workspaceId " +
                    "WHERE r.id = :repositoryId",
            Tuple::class.java
        ).setParameter("repositoryId", id).singleResult

        return result.get(0, Workspace::class.java).members.firstOrNull { accountId == it.accountId }
    }

    fun getWorkspaceMemberByDebtId(id: Long, accountId: Long): WorkspaceMember? {
        val result = entityManager.createQuery(
            "SELECT w FROM Debt d " +
                    "LEFT JOIN Repository r ON r.id = d.repositoryId " +
                    "LEFT JOIN Workspace w ON w.id = r.workspaceId " +
                    "WHERE d.id = :debtId",
            Tuple::class.java
        ).setParameter("debtId", id).singleResult

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
}