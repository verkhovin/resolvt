package dev.ithurts.model

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

@Entity
class Account(
    private val email: String,
    private val name: String,
    private val sourceProvider: SourceProvider,
    private val externalId: String,
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private val id: Long? = null
)