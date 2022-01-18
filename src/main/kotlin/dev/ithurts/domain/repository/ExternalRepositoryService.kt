package dev.ithurts.domain.repository

import dev.ithurts.domain.workspace.Workspace

interface ExternalRepositoryService {
    fun getExternalRepositoryMainBranch(workspace: Workspace, repositoryName: String): String
}