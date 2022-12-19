package dev.resolvt.service.workspace

import dev.resolvt.service.account.AccountRepository
import dev.resolvt.application.exception.EntityNotFoundException
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
        val workspace = workspaceRepository.findByIdOrNull(workspaceId) ?: throw EntityNotFoundException(
            "workspace",
            "id",
            workspaceId
        )
        val account = accountRepository.findByEmailAndSourceProvider(email, workspace.sourceProvider)
            ?: throw EntityNotFoundException("account", "email", email)
        workspaceRepository.save(workspace.addMember(account.id, WorkspaceMemberRole.MEMBER, WorkspaceMemberStatus.ACTIVE))
    }

    @PreAuthorize("hasPermission(#workspaceId, 'Workspace', 'MEMBER')")
    fun getWorkspaceById(workspaceId: String): Workspace {
        return workspaceRepository.findByIdOrNull(workspaceId)!! //FIXME NPE
    }
}
