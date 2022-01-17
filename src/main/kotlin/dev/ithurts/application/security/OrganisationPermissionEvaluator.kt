package dev.ithurts.application.security

import dev.ithurts.domain.account.Account
import dev.ithurts.domain.workspace.Workspace
import dev.ithurts.domain.workspace.WorkspaceMemberRole
import dev.ithurts.domain.workspace.WorkspaceRepository
import dev.ithurts.application.security.oauth2.AuthenticatedOAuth2User
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.access.PermissionEvaluator
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component
import java.io.Serializable

@Component
class OrganisationPermissionEvaluator(
    private val workspaceRepository: WorkspaceRepository
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
        val requiredRole = WorkspaceMemberRole.valueOf(permission as String)
        val accountId = when (val principal = authentication.principal) {
            is AuthenticatedOAuth2User -> principal.accountId
            is Account -> principal.id
            is Workspace -> return targetId == principal.id
            else -> return false
        }
        val workspace = workspaceRepository.findByIdOrNull(targetId as Long) ?:
            return false
        return workspace.checkAccountHasPermission(accountId, requiredRole)
    }
}