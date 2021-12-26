package dev.ithurts.controller.api.webhook.dto

data class RepoUpdated(
    val changes: RepoUpdateChanges
) : BitbucketWebhookEventData

data class RepoUpdateChanges(
    val slug: RepoUpdateChange
)

data class RepoUpdateChange(
    val old: String,
    val new: String
)