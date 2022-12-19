package dev.resolvt.service.sourceprovider.bitbucket.model


data class BitbucketWebhookEvent<T: BitbucketWebhookEventData>(
    val event: String,
    val data: T
)

interface BitbucketWebhookEventData