package dev.ithurts.domain.debt

import javax.persistence.Embeddable

@Embeddable
data class DebtVote(
    val accountId: Long
)
