package dev.ithurts.service

import dev.ithurts.exception.EntityNotFoundException
import dev.ithurts.model.Account
import dev.ithurts.model.SourceProvider
import dev.ithurts.model.organisation.Organisation
import dev.ithurts.model.organisation.OrganisationMemberRole
import dev.ithurts.model.organisation.OrganisationMembership
import dev.ithurts.repository.AccountRepository
import dev.ithurts.repository.OrganisationMembershipRepository
import dev.ithurts.repository.OrganisationRepository
import dev.ithurts.service.core.OrganisationService
import dev.ithurts.sourceprovider.model.SourceProviderOrganisation
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import javax.transaction.Transactional

@Service
class OrganisationApiService(
    private val organisationService: OrganisationService,
    private val organisationMembershipRepository: OrganisationMembershipRepository,
    private val accountRepository: AccountRepository,
) {

    @Transactional
    fun createOrganisationFromExternalOne(
        sourceProviderOrganisation: SourceProviderOrganisation,
        owner: Account,
        clientKey: String,
        secret: String
    ): Long {
        val existingOrganisation = organisationService.getByExternalId(
            sourceProviderOrganisation.sourceProvider,
            sourceProviderOrganisation.id
        )
        return if (existingOrganisation != null) {
            existingOrganisation.active = true
            existingOrganisation.clientKey = clientKey
            existingOrganisation.secret = secret
            organisationService.save(existingOrganisation).id!!
        } else {
            val organisation = organisationService.save(
                Organisation(
                    sourceProviderOrganisation.name,
                    sourceProviderOrganisation.sourceProvider,
                    sourceProviderOrganisation.id,
                    clientKey,
                    secret
                )
            )
            organisation.addMember(owner, OrganisationMemberRole.ADMIN)
            organisationService.save(organisation)
            return organisation.id!!
        }
    }

    fun deactivateOrganisation(sourceProvider: SourceProvider, externalId: String) {
        organisationService.getByExternalId(sourceProvider, externalId)?.let {
            it.active = false
            organisationService.save(it)
        }
    }

    fun getMembership(organisationId: Long, accountId: Long): OrganisationMembership {
        return organisationMembershipRepository.findByOrganisationIdAndAccountId(organisationId, accountId)
            ?: throw EntityNotFoundException("membership", "organisationId/accountId", "$organisationId/$accountId")
    }

    fun addMemberByEmail(currentOrganisationId: Long, email: String) {
        val account = accountRepository.findByEmail(email) ?: throw EntityNotFoundException("acccount", "email", email)
        val organisation = organisationService.getById(currentOrganisationId)!!
        organisation.addMember(account)
        organisationService.save(organisation)
    }
}
