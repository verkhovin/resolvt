package dev.ithurts.repository

import dev.ithurts.model.organisation.OrganisationMembership
import dev.ithurts.model.organisation.OrganisationMembershipId
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository

interface OrganisationMembershipRepository: CrudRepository<OrganisationMembership, OrganisationMembershipId> {
//    @Query(
//        "from OrganisationMembership as m join fetch m.organisation as o join fetch m.account as a where o.id = :organisationId and a.id = :accountId"
//    )
    fun findByOrganisationIdAndAccountId(organisationId: Long, accountId: Long): OrganisationMembership?

    fun getByAccountId(accountId: Long): List<OrganisationMembership>
}