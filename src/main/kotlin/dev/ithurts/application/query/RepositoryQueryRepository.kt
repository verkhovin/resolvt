package dev.ithurts.application.query

import dev.ithurts.application.model.RepositoryDto
import dev.ithurts.application.model.RepositoryInfo
import dev.ithurts.domain.repository.RepositoryRepository
import dev.ithurts.domain.workspace.WorkspaceRepository
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service

@Service
class RepositoryQueryRepository(
    private val repositoryRepository: RepositoryRepository,
    private val workspaceRepository: WorkspaceRepository
) {
    @PreAuthorize("hasPermission(#repositoryInfo, 'Repository', 'MEMBER')")
    fun getRepository(repositoryInfo: RepositoryInfo): RepositoryDto {
        val workspace = workspaceRepository.findBySourceProviderAndExternalId(
            repositoryInfo.sourceProvider,
            repositoryInfo.workspaceExternalId
        )!!
        val repository = repositoryRepository.findByNameAndWorkspaceId(repositoryInfo.name, workspace.id)!!
        return RepositoryDto(repository.name, repository.mainBranch)
    }
}