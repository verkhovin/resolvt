package dev.ithurts.application.security.oauth2

import dev.ithurts.domain.SourceProvider

//TODO probably this should be a part of domain
interface SourceProviderUserInfo {
    val id: String
    val displayName: String
    val sourceProvider: SourceProvider
}