package dev.ithurts.model.organisation

import dev.ithurts.model.Account
import javax.persistence.EmbeddedId
import javax.persistence.Entity
import javax.persistence.ManyToOne
import javax.persistence.MapsId

@Entity
class OrganisationMembership(
    @ManyToOne
    @MapsId("accountId")
    val account: Account,
    @ManyToOne
    @MapsId("organisationId")
    val organisation: Organisation,
    val role: OrganisationMemberRole,
    val status: OrganisationMemebershipStatus
) {
    @EmbeddedId
    val id = OrganisationMembershipId(account.id!!, organisation.id!!)

}