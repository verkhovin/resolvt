package dev.ithurts.sourceprovider.bitbucket.dto.webhook

data class BitbucketWebhookEvent<T>(
    val event: String,
    val data: T
)

interface BitbucketWebhookEventData