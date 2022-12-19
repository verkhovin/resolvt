package dev.resolvt.service.debt.model

data class PushInfo(
    val commitHash: String,
    val repositoryId: String,
    val workspaceExternalId: String,
    val repositoryExternalId: String
)
