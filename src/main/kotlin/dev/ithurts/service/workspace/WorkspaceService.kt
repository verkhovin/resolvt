package dev.ithurts.service.workspace

import dev.ithurts.service.account.AccountRepository
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
        workspaceRepository.save(workspace.addMember(account.id, WorkspaceMemberRole.MEMBER, WorkspaceMemberStatus.ACTIVE))
    }

    @PreAuthorize("hasPermission(#workspaceId, 'Workspace', 'MEMBER')")
    fun getWorkspaceById(workspaceId: String): Workspace {
        return workspaceRepository.findByIdOrNull(workspaceId)!! //FIXME NPE
    }
}
