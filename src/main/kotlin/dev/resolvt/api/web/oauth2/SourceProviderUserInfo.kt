package dev.resolvt.api.web.oauth2

import dev.resolvt.service.SourceProvider

interface SourceProviderUserInfo {
    val id: String
    val displayName: String
    val sourceProvider: SourceProvider
}