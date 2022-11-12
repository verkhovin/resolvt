package dev.ithurts.api.web.oauth2

import dev.ithurts.service.SourceProvider

class GitHubSourceProviderUserInfo(private val attributes: Map<String, Any>): SourceProviderUserInfo {
    override val id: String
        get() = attributes["login"] as String
    override val displayName: String
        get() = attributes["name"] as String
    override val sourceProvider: SourceProvider
        get() = SourceProvider.GITHUB
}