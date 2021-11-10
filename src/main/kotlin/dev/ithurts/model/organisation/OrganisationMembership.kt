package dev.ithurts.model.organisation

import dev.ithurts.model.Account
import javax.persistence.*

@Entity
class OrganisationMembership(
    @ManyToOne
    @MapsId("accountId")
    val account: Account,
    @ManyToOne()
    @MapsId("organisationId")
    val organisation: Organisation,
    @Enumerated(EnumType.STRING)
    val role: OrganisationMemberRole,
    @Enumerated(EnumType.STRING)
    val status: OrganisationMemebershipStatus
) {
    @EmbeddedId
    val id = OrganisationMembershipId(account.id!!, organisation.id!!)

}