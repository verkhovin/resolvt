package dev.ithurts.service.debt

import dev.ithurts.service.debt.model.Debt
import dev.ithurts.service.debt.model.DebtStatus
import dev.ithurts.service.debt.model.DebtStatus.RESOLVED
import org.springframework.data.repository.CrudRepository

interface DebtRepository : CrudRepository<Debt, String> {
    fun findByRepositoryIdAndStatusNot(repositoryId: String, excludeWithStatus: DebtStatus = RESOLVED): List<Debt>

    fun findByWorkspaceIdAndStatusNot(workspaceId: String, excludeWithStatus: DebtStatus = RESOLVED): List<Debt>

    fun findByWorkspaceIdAndBindingsFilePathInAndStatusNot(
        workspaceId: String,
        paths: List<String>,
        excludeWithStatus: DebtStatus = RESOLVED
    ): List<Debt>
}
