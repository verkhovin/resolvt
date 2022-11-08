package dev.ithurts.service.permission

import dev.ithurts.service.repository.RepositoryInfo
import dev.ithurts.service.debt.DebtRepository
import dev.ithurts.service.repository.RepositoryRepository
import dev.ithurts.service.workspace.WorkspaceMember
import dev.ithurts.service.workspace.WorkspaceRepository
import org.springframework.data.repository.findByIdOrNull

@org.springframework.stereotype.Repository
class PermissionQueryRepository(
    private val workspaceRepository: WorkspaceRepository,
    private val repositoryRepository: RepositoryRepository,
    private val debtRepository: DebtRepository
) {
    fun getWorkspaceMemberByWorkspaceId(id: String, accountId: String): WorkspaceMember? {
        return workspaceRepository.findByIdOrNull(id)?.let { workspace ->
            workspace.members.firstOrNull { it.accountId == accountId }
        }
    }

    fun getWorkspaceMemberByRepositoryId(id: String, accountId: String): WorkspaceMember? {
        val repository = repositoryRepository.findByIdOrNull(id) ?: return null
        return workspaceRepository.findByIdOrNull(repository.workspaceId)?.let { workspace ->
            workspace.members.firstOrNull { it.accountId == accountId }
        }
    }

    fun getWorkspaceMemberByDebtId(id: String, accountId: String): WorkspaceMember? {
        val debt = debtRepository.findByIdOrNull(id) ?: return null
        return workspaceRepository.findByIdOrNull(debt.workspaceId)?.let { workspace ->
            workspace.members.firstOrNull { it.accountId == accountId }
        }
    }

    fun getWorkspaceMemberByRepositoryInfo(repository: RepositoryInfo, accountId: String): WorkspaceMember? {
        return workspaceRepository.findBySourceProviderAndExternalId(
            repository.sourceProvider,
            repository.workspaceExternalId
        )?.let { workspace ->
            workspace.members.firstOrNull { it.accountId == accountId }
        }
    }
}