package dev.ithurts.application.security.oauth2

import dev.ithurts.domain.SourceProvider

interface SourceProviderUserInfo {
    val id: String
    val displayName: String
    val sourceProvider: SourceProvider
}