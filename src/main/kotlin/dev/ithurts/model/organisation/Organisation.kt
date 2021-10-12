package dev.ithurts.model.organisation

import dev.ithurts.model.Account
import dev.ithurts.model.SourceProvider
import dev.ithurts.model.organisation.OrganisationMemberRole.*
import dev.ithurts.model.organisation.OrganisationMemebershipStatus.*
import javax.persistence.*

@Entity
class Organisation(
    val name: String,
    val sourceProvider: SourceProvider,
    val externalId: String,
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null
) {
    @OneToMany(mappedBy = "organisation", cascade = [CascadeType.ALL], orphanRemoval = true)
    val members: List<OrganisationMembership> = listOf()

    fun addMember(
        account: Account,
        role: OrganisationMemberRole = MEMBER,
        status: OrganisationMemebershipStatus = ACTIVE
    ) {
        OrganisationMembership(account, this, role, status)
    }
}