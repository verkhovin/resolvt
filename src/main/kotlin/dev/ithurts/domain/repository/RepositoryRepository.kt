package dev.ithurts.domain.repository

import dev.ithurts.domain.repository.Repository
import org.springframework.data.repository.CrudRepository

interface RepositoryRepository: CrudRepository<Repository, Long> {
    fun findByNameAndWorkspaceId(name: String, workspace: Long): Repository?
}