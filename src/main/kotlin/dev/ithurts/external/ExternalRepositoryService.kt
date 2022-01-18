package dev.ithurts.external

import dev.ithurts.application.sourceprovider.SourceProviderCommunicationService
import dev.ithurts.domain.workspace.Workspace
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import dev.ithurts.domain.repository.ExternalRepositoryService as DomainExternalRepositoryService

@Service
class ExternalRepositoryService(
    private val sourceProviderCommunicationService: SourceProviderCommunicationService
) : DomainExternalRepositoryService {
    @PreAuthorize("hasPermission(#workspace.id, 'Workspace', 'MEMBER')")
    override fun getExternalRepositoryMainBranch(workspace: Workspace, repositoryName: String): String {
        val repository = sourceProviderCommunicationService.getRepository(workspace.externalId, repositoryName)
        return repository.mainBranch
    }
}