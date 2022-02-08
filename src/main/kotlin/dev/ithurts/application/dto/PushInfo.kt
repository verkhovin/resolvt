package dev.ithurts.application.dto

data class PushInfo(
    val commitHash: String,
    val repositoryId: String,
    val workspaceExternalId: String,
    val repositoryExternalId: String
)
