package dev.ithurts.service

import dev.ithurts.model.Account
import dev.ithurts.model.SourceProvider
import dev.ithurts.model.organisation.Organisation
import dev.ithurts.model.organisation.OrganisationMemberRole
import dev.ithurts.repository.OrganisationRepository
import dev.ithurts.sourceprovider.SourceProviderCommunicationService
import org.springframework.stereotype.Service
import javax.transaction.Transactional

@Service
class OrganisationService(
    private val organisationRepository: OrganisationRepository,
    private val sourceProviderCommunicationService: SourceProviderCommunicationService,
) {
    fun getByMemberAccountId(accountId: Long): List<Organisation> {
        return organisationRepository.getByMemberAccountAndId(accountId)
    }

    @Transactional
    fun createOrganisationFromExternalOne(externalOrganisationId: String, owner: Account): Long {
        val sourceProviderOrganisation = sourceProviderCommunicationService.getOrganisation(externalOrganisationId)
        val organisation = organisationRepository.save(
            Organisation(
                sourceProviderOrganisation.name,
                sourceProviderOrganisation.sourceProvider,
                sourceProviderOrganisation.id
            )
        )
        organisation.addMember(owner, OrganisationMemberRole.ADMIN)
        organisationRepository.save(organisation)
        return organisation.id!!
    }
}
