package dev.ithurts.security

import dev.ithurts.model.organisation.OrganisationMemberRole
import dev.ithurts.model.organisation.OrganisationMembership
import dev.ithurts.model.organisation.OrganisationMemebershipStatus.ACTIVE
import dev.ithurts.repository.OrganisationMembershipRepository
import org.springframework.security.access.PermissionEvaluator
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component
import java.io.Serializable

@Component
class OrganisationPermissionEvaluator(
    private val membershipRepository: OrganisationMembershipRepository
) : PermissionEvaluator {
    override fun hasPermission(authentication: Authentication?, targetDomainObject: Any?, permission: Any?): Boolean {
        throw NotImplementedError()
    }

    override fun hasPermission(
        authentication: Authentication?,
        targetId: Serializable,
        targetType: String,
        permission: Any
    ): Boolean {
        authentication ?: return false
        val requiredRole = OrganisationMemberRole.valueOf(permission as String)
        val principal = authentication.principal
        if (principal is AuthenticatedOAuth2User) {
            val membership =
                membershipRepository.findByOrganisationIdAndAccountId(targetId as Long, principal.accountId)
                    ?: return false
            return membership.status == ACTIVE && hasPermissionByRole(membership, requiredRole)
        }
        return false
    }

    private fun hasPermissionByRole(
        membership: OrganisationMembership,
        requiredRole: OrganisationMemberRole
    ) =
        membership.role == requiredRole || (membership.role == OrganisationMemberRole.ADMIN && requiredRole == OrganisationMemberRole.MEMBER)

}