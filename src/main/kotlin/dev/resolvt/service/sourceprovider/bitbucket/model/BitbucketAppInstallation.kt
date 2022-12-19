package dev.resolvt.service.sourceprovider.bitbucket.model

import com.fasterxml.jackson.annotation.JsonProperty

data class BitbucketAppInstallation(
    val productType: String,
    val principal: BitbucketAppInstallationPrincipal,
    val actor: BitbucketAppInstallationActor?,
    val clientKey: String?,
    val sharedSecret: String?
)

data class BitbucketAppInstallationPrincipal(
    val username: String?, // workspace slug
    val nickname: String?, // workspace slug if personal worksapce
    @JsonProperty("display_name") val displayName: String // workspace name
) {
    val bitbucketId: String
        get() = username ?: nickname ?: throw IllegalStateException("username or nickname is null")
}

data class BitbucketAppInstallationActor(
    val uuid: String
)
