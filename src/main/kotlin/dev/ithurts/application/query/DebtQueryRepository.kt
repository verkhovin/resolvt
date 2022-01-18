package dev.ithurts.application.query

import dev.ithurts.application.dto.debt.DebtAccountDTO
import dev.ithurts.application.dto.debt.DebtDTO
import dev.ithurts.application.dto.debt.DebtRepositoryDTO
import dev.ithurts.application.dto.debt.SourceLink
import dev.ithurts.application.service.RepositoryInfo
import dev.ithurts.application.service.SourceProviderService
import dev.ithurts.domain.account.Account
import dev.ithurts.domain.debt.Debt
import dev.ithurts.domain.debt.DebtStatus
import dev.ithurts.domain.repository.Repository
import dev.ithurts.domain.workspace.Workspace
import org.springframework.security.access.prepost.PreAuthorize
import javax.persistence.EntityManager
import javax.persistence.Tuple
import org.springframework.stereotype.Repository as SpringRepository

@SpringRepository
class DebtQueryRepository(
    private val entityManager: EntityManager,
    private val sourceProviderService: SourceProviderService
) {

    @PreAuthorize("hasPermission(#repositoryId, 'Repository', 'MEMBER')")
    fun queryRepositoryActiveDebts(repositoryId: Long): List<DebtDTO> {
        val resultList = entityManager.createQuery(
            "SELECT d, r, w, a FROM Debt d " +
                    "LEFT JOIN Repository r ON r.id = d.repositoryId " +
                    "LEFT JOIN Workspace w ON w.id = r.workspaceId " +
                    "LEFT JOIN Account a ON a.id = d.creatorAccountId " +
                    "WHERE r.id = :repositoryId " +
                    "AND d.status <> :debtStatus",
            Tuple::class.java
        ).setParameter("repositoryId", repositoryId)
            .setParameter("debtStatus", DebtStatus.RESOLVED).resultList

        return resultList.map(::toDto)
    }

    @PreAuthorize("hasPermission(#repositoryInfo, 'Repository', 'MEMBER')")
    fun queryRepositoryActiveDebts(repositoryInfo: RepositoryInfo): List<DebtDTO> {
        val resultList = entityManager.createQuery(
            "SELECT d, r, w, a FROM Debt d " +
                    "LEFT JOIN Repository r ON r.id = d.repositoryId " +
                    "LEFT JOIN Workspace w ON w.id = r.workspaceId " +
                    "LEFT JOIN Account a ON a.id = d.creatorAccountId " +
                    "WHERE r.name = :repositoryName AND w.externalId = :workspaceExternalId AND w.sourceProvider = :sourceProvider " +
                    "AND d.status <> :debtStatus",
            Tuple::class.java
        ).setParameter("repositoryName", repositoryInfo.name)
            .setParameter("workspaceExternalId", repositoryInfo.workspaceExternalId)
            .setParameter("sourceProvider", repositoryInfo.sourceProvider)

            .setParameter("debtStatus", DebtStatus.RESOLVED).resultList

        return resultList.map(::toDto)
    }

    @PreAuthorize("hasPermission(#workspaceId, 'Workspace', 'MEMBER')")
    fun queryWorkspaceActiveDebts(workspaceId: Long): List<DebtDTO> {
        val resultList = entityManager.createQuery(
            "SELECT d, r, w, a FROM Debt d " +
                    "LEFT JOIN Repository r ON r.id = d.repositoryId " +
                    "LEFT JOIN Workspace w ON w.id = r.workspaceId " +
                    "LEFT JOIN Account a ON a.id = d.creatorAccountId " +
                    "WHERE r.id = :workspaceId " +
                    "AND d.status <> :debtStatus",
            Tuple::class.java
        ).setParameter("workspaceId", workspaceId)
            .setParameter("debtStatus", DebtStatus.RESOLVED).resultList

        return resultList.map(::toDto)
    }

    private fun toDto(it: Tuple): DebtDTO {
        val debt = it.get(0, Debt::class.java)
        val repo = it.get(1, Repository::class.java)
        val workspace = it.get(2, Workspace::class.java)
        val account = it.get(3, Account::class.java)
        return DebtDTO.from(
            it.get(0, Debt::class.java),
            SourceLink(
                sourceProviderService.getSourceUrl(
                    debt,
                    repo.name,
                    repo.mainBranch,
                    workspace.externalId
                ),
                getFileName(debt.filePath)
            ),
            DebtRepositoryDTO(repo.name),
            DebtAccountDTO(account?.name ?: "Unknown")
        )
    }

    private fun getFileName(path: String) = path.substringAfterLast("/")
}