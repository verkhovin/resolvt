package dev.ithurts.model.organisation

import java.io.Serializable
import java.util.*
import javax.persistence.Embeddable

@Embeddable
class OrganisationMembershipId(
    val accountId: Long,
    val organisationId: Long
): Serializable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as OrganisationMembershipId
        return accountId == that.accountId && organisationId == that.organisationId
    }

    override fun hashCode(): Int {
        return Objects.hash(accountId, organisationId)
    }
}