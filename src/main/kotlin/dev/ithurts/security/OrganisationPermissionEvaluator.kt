package dev.ithurts.security

import dev.ithurts.model.organisation.OrganisationMemberRole
import dev.ithurts.model.organisation.OrganisationMemebershipStatus
import dev.ithurts.model.organisation.OrganisationMemebershipStatus.ACTIVE
import dev.ithurts.repository.OrganisationRepository
import org.springframework.security.access.PermissionEvaluator
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component
import java.io.Serializable

@Component
class OrganisationPermissionEvaluator(
    private val organisationRepository: OrganisationRepository
): PermissionEvaluator {
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
        permission as String
        val principal = authentication.principal
        if (principal is AuthenticatedOAuth2User) {
            val organisation = organisationRepository.getWithMembership(targetId as Long, principal.accountId)!!
            if (organisation.members.isEmpty()) return false
            val membership = organisation.members[0]
            return membership.status == ACTIVE && membership.role == OrganisationMemberRole.valueOf(permission)
        }
        return false
    }
}