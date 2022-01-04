package dev.ithurts.sourceprovider

import dev.ithurts.sourceprovider.model.SourceProviderOrganisation
import dev.ithurts.sourceprovider.model.SourceProviderRepository

interface SourceProviderClient {
    val organisationOwnerRole: String
    fun getDiff(accessToken: String, organisation: String, repository: String, spec: String): String
    fun getRepository(accessToken: String, organisation: String, repository: String): SourceProviderRepository
}