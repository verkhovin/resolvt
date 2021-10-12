package dev.ithurts.repository

import dev.ithurts.model.organisation.Organisation
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository

interface OrganisationRepository: CrudRepository<Organisation, Long> {
    @Query("from Organisation as o join o.members as m where m.account.id = :accountId")
    fun getByMemberAccountAndId(accountId: Long): List<Organisation>
}