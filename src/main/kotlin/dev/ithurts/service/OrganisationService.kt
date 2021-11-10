package dev.ithurts.service

import dev.ithurts.exception.EntityNotFoundException
import dev.ithurts.model.Account
import dev.ithurts.model.organisation.Organisation
import dev.ithurts.model.organisation.OrganisationMemberRole
import dev.ithurts.model.organisation.OrganisationMembership
import dev.ithurts.repository.AccountRepository
import dev.ithurts.repository.OrganisationMembershipRepository
import dev.ithurts.repository.OrganisationRepository
import dev.ithurts.sourceprovider.SourceProviderCommunicationService
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import javax.transaction.Transactional

@Service
class OrganisationService(
    private val organisationRepository: OrganisationRepository,
    private val organisationMembershipRepository: OrganisationMembershipRepository,
    private val accountRepository: AccountRepository,
    private val sourceProviderCommunicationService: SourceProviderCommunicationService,
) {

    @PreAuthorize("hasPermission(#organisationId, 'Organisation', 'MEMBER')")
    fun getById(organisationId: Long): Organisation{
        return organisationRepository.findByIdOrNull(organisationId)
            ?: throw EntityNotFoundException("organisation", "id", organisationId.toString())
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

    fun getMembership(organisationId: Long, accountId: Long): OrganisationMembership {
        return organisationMembershipRepository.findByOrganisationIdAndAccountId(organisationId, accountId)
            ?: throw EntityNotFoundException("membership", "organisationId/accountId", "$organisationId/$accountId");
    }

//    fun getOrganisationMembership(organisationId: Long, accountId: Long): OrganisationMembership? {
//        val organisation = (organisationRepository.getWithMembership(organisationId, accountId)
//            ?: throw EntityNotFoundException("organisation", "id", organisationId.toString()))
//        return if (organisation.members.isEmpty()) null else organisation.members[0]
//    }

    @PreAuthorize("hasPermission(#currentOrganisationId, 'Organisation', 'ADMIN')")
    fun addMemberByEmail(currentOrganisationId: Long, email: String) {
        val account = accountRepository.findByEmail(email) ?: throw EntityNotFoundException("acccount", "email", email)
        val organisation = organisationRepository.findByIdOrNull(currentOrganisationId)!!
        organisation.addMember(account)
        organisationRepository.save(organisation)
    }
}
