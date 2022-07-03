package dev.ithurts.application.service

import dev.ithurts.domain.account.AccountRepository
import dev.ithurts.domain.workspace.WorkspaceMemberRole
import dev.ithurts.domain.workspace.WorkspaceMemberStatus
import dev.ithurts.domain.workspace.WorkspaceRepository
import dev.ithurts.application.exception.EntityNotFoundException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service

@Service
class WorkspaceService(
    private val workspaceRepository: WorkspaceRepository,
    private val accountRepository: AccountRepository,
) {
    @PreAuthorize("hasPermission(#workspaceId, 'Workspace', 'MEMBER')")
    fun addMemberByEmail(workspaceId: String, email: String) {
        val account = accountRepository.findByEmail(email) ?: throw EntityNotFoundException("account", "email", email)
        val workspace = workspaceRepository.findByIdOrNull(workspaceId) ?: throw EntityNotFoundException(
            "workspace",
            "id",
            workspaceId
        )
        workspace.addMember(account.id, WorkspaceMemberRole.MEMBER, WorkspaceMemberStatus.ACTIVE)
        workspaceRepository.save(workspace)
    }
}
