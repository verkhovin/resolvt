package dev.ithurts.controller.api.webhook.dto

data class BitbucketWebhookEvent<T: BitbucketWebhookEventData>(
    val event: String,
    val data: T
)

interface BitbucketWebhookEventData