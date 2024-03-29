package dev.resolvt.service.repository

import dev.resolvt.service.workspace.Workspace
import dev.resolvt.service.workspace.WorkspaceRepository
import dev.resolvt.service.sourceprovider.SourceProviderCommunicationService
import org.springframework.stereotype.Service

@Service
class RepositoryService(
    private val sourceProviderCommunicationService: SourceProviderCommunicationService,
    private val workspaceRepository: WorkspaceRepository,
    private val repositoryRepository: RepositoryRepository
) {
    fun ensureRepository(repositoryInfo: RepositoryInfo): Repository {
        val workspace = workspaceRepository.findBySourceProviderAndExternalId(
            repositoryInfo.sourceProvider, repositoryInfo.workspaceExternalId
        ) ?: throw DebtReportFailedException("No organisation found for ${repositoryInfo.workspaceExternalId}")

        return repositoryRepository.findByNameAndWorkspaceId(repositoryInfo.name, workspace.id)
            ?: acknowledgeExternalRepositoryByWorkspace(workspace, repositoryInfo.name).let {
                repositoryRepository.save(it)
            }
    }

    private fun acknowledgeExternalRepositoryByWorkspace(workspace: Workspace, name: String): Repository {
        val mainBranch = sourceProviderCommunicationService.getRepository(workspace, name).mainBranch
        return Repository(name, mainBranch, workspace.id)
    }
}