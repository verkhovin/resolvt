package dev.ithurts.domain.account

import dev.ithurts.domain.DomainEntity
import dev.ithurts.domain.SourceProvider
import javax.persistence.*

@Entity
class Account (
    val email: String,
    val name: String,
    @Enumerated(EnumType.STRING)
    val sourceProvider: SourceProvider,
    val externalId: String,
): DomainEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    override val _id: Long? = null

}