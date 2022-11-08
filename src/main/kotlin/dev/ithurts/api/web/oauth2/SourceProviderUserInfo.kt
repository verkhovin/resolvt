package dev.ithurts.api.web.oauth2

import dev.ithurts.service.SourceProvider

interface SourceProviderUserInfo {
    val id: String
    val displayName: String
    val sourceProvider: SourceProvider
}