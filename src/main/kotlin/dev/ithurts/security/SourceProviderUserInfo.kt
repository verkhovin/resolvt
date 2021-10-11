package dev.ithurts.security

import dev.ithurts.model.SourceProvider

interface SourceProviderUserInfo {
    val id: String
    val displayName: String
    val sourceProvider: SourceProvider
}