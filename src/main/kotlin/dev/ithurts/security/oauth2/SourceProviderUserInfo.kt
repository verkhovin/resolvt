package dev.ithurts.security.oauth2

import dev.ithurts.model.SourceProvider

interface SourceProviderUserInfo {
    val id: String
    val displayName: String
    val sourceProvider: SourceProvider
}