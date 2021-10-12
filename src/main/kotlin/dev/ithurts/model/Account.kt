package dev.ithurts.model

import dev.ithurts.model.organisation.OrganisationMembership
import javax.persistence.*

@Entity
class Account(
    val email: String,
    val name: String,
    val sourceProvider: SourceProvider,
    val externalId: String,
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null
) {
    @OneToMany(mappedBy = "account", cascade = [CascadeType.ALL], orphanRemoval = true)
    val organisations: List<OrganisationMembership> = listOf()
}