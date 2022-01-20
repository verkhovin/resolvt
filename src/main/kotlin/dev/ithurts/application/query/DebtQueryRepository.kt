package dev.ithurts.application.query

import dev.ithurts.application.dto.debt.DebtAccountDto
import dev.ithurts.application.dto.debt.DebtDto
import dev.ithurts.application.dto.debt.DebtRepositoryDto
import dev.ithurts.application.dto.debt.SourceLink
import dev.ithurts.application.security.AuthenticationFacade
import dev.ithurts.application.service.RepositoryInfo
import dev.ithurts.application.service.SourceProviderService
import dev.ithurts.domain.account.Account
import dev.ithurts.domain.debt.Debt
import dev.ithurts.domain.debt.DebtStatus
import dev.ithurts.domain.repository.Repository
import dev.ithurts.domain.workspace.Workspace
import dev.ithurts.exception.EntityNotFoundException
import org.springframework.security.access.prepost.PreAuthorize
import javax.persistence.EntityManager
import javax.persistence.NoResultException
import javax.persistence.Tuple
import org.springframework.stereotype.Repository as SpringRepository

@SpringRepository
class DebtQueryRepository(
    private val entityManager: EntityManager,
    private val sourceProviderService: SourceProviderService,
    private val authenticationFacade: AuthenticationFacade,
) {

    fun queryDebt(debtId: Long): DebtDto {
        val result = try {
            entityManager.createQuery(
                "SELECT d, r, w, a FROM Debt d " +
                        "LEFT JOIN Repository r ON r.id = d.repositoryId " +
                        "LEFT JOIN Workspace w ON w.id = r.workspaceId " +
                        "LEFT JOIN Account a ON a.id = d.creatorAccountId " +
                        "WHERE d.id = :debtId",
                Tuple::class.java
            ).setParameter("debtId", debtId).singleResult
        } catch (e: NoResultException) {
            throw EntityNotFoundException("Debt", "id", debtId.toString())
        }

        return toDto(result)
    }


    @PreAuthorize("hasPermission(#repositoryId, 'Repository', 'MEMBER')")
    fun queryRepositoryActiveDebts(repositoryId: Long): List<DebtDto> {
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
    fun queryRepositoryActiveDebts(repositoryInfo: RepositoryInfo): List<DebtDto> {
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
    fun queryWorkspaceDebts(workspaceId: Long): List<DebtDto> {
        val resultList = entityManager.createQuery(
            "SELECT d, r, w, a FROM Debt d " +
                    "LEFT JOIN Repository r ON r.id = d.repositoryId " +
                    "LEFT JOIN Workspace w ON w.id = r.workspaceId " +
                    "LEFT JOIN Account a ON a.id = d.creatorAccountId " +
                    "WHERE w.id = :workspaceId",
            Tuple::class.java
        ).setParameter("workspaceId", workspaceId).resultList

        return resultList.map(::toDto)
    }

    private fun toDto(selectResult: Tuple): DebtDto {
        val debt = selectResult.get(0, Debt::class.java)
        val repo = selectResult.get(1, Repository::class.java)
        val workspace = selectResult.get(2, Workspace::class.java)
        val reporter = selectResult.get(3, Account::class.java)
        return DebtDto.from(
            selectResult.get(0, Debt::class.java),
            SourceLink(
                sourceProviderService.getSourceUrl(
                    debt,
                    repo.name,
                    repo.mainBranch,
                    workspace.externalId
                ),
                getFileName(debt.filePath)
            ),
            DebtRepositoryDto(repo.name),
            DebtAccountDto(reporter?.name ?: "Unknown"),
            debt.accountVoted(authenticationFacade.account.identity)
        )
    }

    private fun getFileName(path: String) = path.substringAfterLast("/")
}