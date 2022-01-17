package dev.ithurts.domain

interface DomainEntity {
    val _id: Long?
    val id: Long
        get() = _id!!
}