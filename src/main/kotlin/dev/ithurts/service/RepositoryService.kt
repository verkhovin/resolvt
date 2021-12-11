package dev.ithurts.service

import dev.ithurts.model.organisation.Organisation
import dev.ithurts.repository.RepositoryRepository
import org.springframework.stereotype.Service

@Service
class RepositoryService(
    private val repositoryRepository: RepositoryRepository
){
    fun changeName(organisation: Organisation, oldName: String, newName: String) {
        val repository = repositoryRepository.findByNameAndOrganisation(oldName, organisation)
            ?: return
        repository.name = newName
        repositoryRepository.save(repository)
    }
}