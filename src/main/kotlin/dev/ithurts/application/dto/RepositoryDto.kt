package dev.ithurts.application.dto

data class RepositoryDto(
    val id: Long,
    val name: String,
    val workspaceId: Long,
)