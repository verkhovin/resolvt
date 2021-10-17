package dev.ithurts.repository

import dev.ithurts.model.organisation.Organisation
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository

interface OrganisationRepository : CrudRepository<Organisation, Long> {
    @Query("from Organisation as o join fetch o.members as m where m.account.id = :accountId")
    fun getByMemberAccountId(accountId: Long): List<Organisation>

    @Query("from Organisation as o join o.members as m where o.id = :organisationId and m.account.id = :accountId")
    fun getWithMembership(organisationId: Long, accountId: Long): Organisation?
}