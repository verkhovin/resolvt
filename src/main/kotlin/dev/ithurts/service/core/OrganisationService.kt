package dev.ithurts.service.core

import dev.ithurts.model.SourceProvider
import dev.ithurts.model.organisation.Organisation
import dev.ithurts.repository.OrganisationRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.access.prepost.PostAuthorize
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service

@Service
class OrganisationService(
    private val organisationRepository: OrganisationRepository
) {
    fun save(organisation: Organisation) = organisationRepository.save(organisation)

    @PreAuthorize("hasPermission(#organisationId, 'Organisation', 'MEMBER')")
    fun getById(organisationId: Long): Organisation? {
        return organisationRepository.findByIdOrNull(organisationId)
    }

    @PostAuthorize("returnObject == null || hasPermission(returnObject.id, 'Organisation', 'MEMBER')")
    fun getByExternalId(sourceProvider: SourceProvider, externalId: String): Organisation? {
        return organisationRepository.getBySourceProviderAndExternalId(sourceProvider, externalId)
    }

}