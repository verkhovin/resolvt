package dev.ithurts.repository

import dev.ithurts.model.debt.Repository
import dev.ithurts.model.organisation.Organisation
import org.springframework.data.repository.CrudRepository

interface RepositoryRepository: CrudRepository<Repository, Long> {
    fun findByNameAndOrganisation(name: String, organisation: Organisation): Repository?
}