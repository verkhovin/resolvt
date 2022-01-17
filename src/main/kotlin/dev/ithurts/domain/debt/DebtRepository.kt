package dev.ithurts.domain.debt

import dev.ithurts.domain.debt.Debt
import dev.ithurts.domain.debt.DebtStatus
import dev.ithurts.domain.debt.DebtStatus.RESOLVED
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository

interface DebtRepository : CrudRepository<Debt, Long> {
    fun findByRepositoryIdAndStatusNot(repositoryId: Long, excludeWithStatus: DebtStatus = RESOLVED): List<Debt>

    @Query(
        "SELECT d FROM Debt d " +
                "JOIN Repository r ON d.repositoryId = r._id " +
                "JOIN Workspace o ON r.workspaceId = o.id " +
                "WHERE o.id = :workspaceId AND d.status <> :excludeWithStatus"
    )
    fun findByWorkspaceIdAndStatusNot(workspaceId: Long, excludeWithStatus: DebtStatus = RESOLVED): List<Debt>

    @Query(
        "SELECT d FROM Debt d " +
                "JOIN Repository r ON d.repositoryId = r._id " +
                "JOIN Workspace o ON r.workspaceId = o.id " +
                "WHERE o.id = :organisationId AND d.filePath in (:paths) AND d.status <> :excludeWithStatus"
    )
    fun findByOrganisationIdAndFilePathIn(
        organisationId: Long,
        paths: List<String>,
        excludeWithStatus: DebtStatus = RESOLVED
    ): List<Debt>
}