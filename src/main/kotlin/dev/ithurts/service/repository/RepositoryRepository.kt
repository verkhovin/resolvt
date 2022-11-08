package dev.ithurts.service.repository

import org.springframework.data.repository.CrudRepository

interface RepositoryRepository: CrudRepository<Repository, String> {
    fun findByNameAndWorkspaceId(name: String, workspace: String): Repository?
}