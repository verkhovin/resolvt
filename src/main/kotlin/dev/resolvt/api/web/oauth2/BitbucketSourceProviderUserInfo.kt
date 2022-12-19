package dev.resolvt.api.web.oauth2

import dev.resolvt.service.SourceProvider

class BitbucketSourceProviderUserInfo(private val attributes: Map<String, Any>) : SourceProviderUserInfo {
    override val id: String
        get() = attributes["uuid"] as String
    override val displayName: String
        get() = attributes["display_name"] as String
    override val sourceProvider: SourceProvider
        get() = SourceProvider.BITBUCKET

}