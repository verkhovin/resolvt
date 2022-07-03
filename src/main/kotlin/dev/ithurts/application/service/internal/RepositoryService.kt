package dev.ithurts.application.service.internal

import dev.ithurts.application.model.RepositoryInfo
import dev.ithurts.domain.repository.Repository
import dev.ithurts.domain.repository.RepositoryRepository
import dev.ithurts.domain.workspace.Workspace
import dev.ithurts.domain.workspace.WorkspaceRepository
import dev.ithurts.application.exception.DebtReportFailedException
import dev.ithurts.application.service.internal.sourceprovider.SourceProviderCommunicationService
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