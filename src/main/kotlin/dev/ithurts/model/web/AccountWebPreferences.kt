package dev.ithurts.model.web

import dev.ithurts.model.Account
import javax.persistence.*

@Entity
class AccountWebPreferences (
    val currentOrganisationId: Long,
    @OneToOne(cascade = [CascadeType.ALL])
    val account: Account,
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null
)