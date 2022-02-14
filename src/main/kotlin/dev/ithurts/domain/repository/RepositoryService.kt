package dev.ithurts.domain.repository

import dev.ithurts.domain.workspace.Workspace
import org.springframework.stereotype.Service

@Service
class RepositoryService(
    private val externalRepositoryService: ExternalRepositoryService
) {
    fun acknowledgeExternalRepositoryByWorkspace(workspace: Workspace, name: String): Repository {
        val mainBranch = externalRepositoryService.getExternalRepositoryMainBranch(workspace, name)
        return Repository(name, mainBranch, workspace.id)
    }
}