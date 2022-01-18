package dev.ithurts.application.sourceprovider

import dev.ithurts.application.sourceprovider.model.SourceProviderRepository

interface SourceProviderClient {
    val organisationOwnerRole: String
    fun getDiff(accessToken: String, organisation: String, repository: String, spec: String): String
    fun getRepository(accessToken: String, organisation: String, repository: String): SourceProviderRepository
}