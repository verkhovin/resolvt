package dev.ithurts.domain

import javax.persistence.Transient

interface DomainEntity {
    val id: Long?
    @get:Transient
    val identity: Long
        get() = id!!
}