package dev.ithurts.service

import dev.ithurts.model.organisation.Organisation
import dev.ithurts.repository.OrganisationRepository
import org.springframework.stereotype.Service

@Service
class OrganisationService(private val organisationRepository: OrganisationRepository) {
    fun getByMemberAccountId(accountId: Long): List<Organisation> {
        return organisationRepository.getByMemberAccountAndId(accountId)
    }
}
