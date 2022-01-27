package dev.ithurts.domain.debt

import dev.ithurts.domain.debt.DebtStatus.RESOLVED
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.security.access.prepost.PreAuthorize

interface DebtRepository : CrudRepository<Debt, Long> {
    fun findByRepositoryIdAndStatusNot(repositoryId: Long, excludeWithStatus: DebtStatus = RESOLVED): List<Debt>

    @Query(
        "SELECT d FROM Debt d " +
                "JOIN Repository r ON d.repositoryId = r.id " +
                "JOIN Workspace o ON r.workspaceId = o.id " +
                "WHERE o.id = :workspaceId AND d.status <> :excludeWithStatus"
    )
    fun findByWorkspaceIdAndStatusNot(workspaceId: Long, excludeWithStatus: DebtStatus = RESOLVED): List<Debt>

    @Query(
        "SELECT d FROM Debt d " +
                "JOIN d.bindings b " +
                "JOIN Repository r ON d.repositoryId = r.id " +
                "JOIN Workspace o ON r.workspaceId = o.id " +
                "WHERE o.id = :workspaceId AND b.filePath in (:paths) AND d.status <> :excludeWithStatus"
    )
    fun findByWorkspaceIdAndFilePaths(
        workspaceId: Long,
        paths: List<String>,
        excludeWithStatus: DebtStatus = RESOLVED
    ): List<Debt>
}