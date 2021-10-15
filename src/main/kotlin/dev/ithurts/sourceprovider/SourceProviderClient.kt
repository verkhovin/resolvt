package dev.ithurts.sourceprovider

import dev.ithurts.sourceprovider.model.SourceProviderOrganisation

interface SourceProviderClient {
    val organisationOwnerRole: String
    fun getUserOrganisations(accessToken: String, role: String): List<SourceProviderOrganisation>
    fun getOrganisation(accessToken: String, organisationId: String): SourceProviderOrganisation
}