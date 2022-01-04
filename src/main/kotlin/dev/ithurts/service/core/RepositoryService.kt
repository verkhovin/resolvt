package dev.ithurts.service.core

import dev.ithurts.model.debt.Repository
import dev.ithurts.model.organisation.Organisation
import dev.ithurts.repository.RepositoryRepository
import dev.ithurts.sourceprovider.SourceProviderCommunicationService
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service

@Service
class RepositoryService(
    private val repositoryRepository: RepositoryRepository,
    private val sourceProviderCommunicationService: SourceProviderCommunicationService
) {
    @PreAuthorize("hasPermission(#organisation.id, 'Organisation', 'MEMBER')")
    fun changeName(organisation: Organisation, oldName: String, newName: String) {
        val repository = repositoryRepository.findByNameAndOrganisation(oldName, organisation)
            ?: return
        repository.name = newName
        repositoryRepository.save(repository)
    }

    @PreAuthorize("hasPermission(#organisation.id, 'Organisation', 'MEMBER')")
    fun getRepository(organisation: Organisation, repositoryName: String) =
        repositoryRepository.findByNameAndOrganisation(repositoryName, organisation)

    @PreAuthorize("hasPermission(#organisation.id, 'Organisation', 'MEMBER')")
    fun save(organisation: Organisation, repositoryName: String): Repository {
        val repository = sourceProviderCommunicationService.getRepository(organisation.externalId, repositoryName)
        return repositoryRepository.save(Repository(repositoryName, repository.mainBranch, organisation))
    }
}