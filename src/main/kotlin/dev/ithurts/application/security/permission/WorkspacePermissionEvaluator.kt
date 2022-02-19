package dev.ithurts.application.security.permission

import dev.ithurts.application.security.oauth2.AuthenticatedOAuth2User
import dev.ithurts.application.model.RepositoryInfo
import dev.ithurts.domain.account.Account
import dev.ithurts.domain.workspace.Workspace
import dev.ithurts.domain.workspace.WorkspaceMember
import dev.ithurts.domain.workspace.WorkspaceMemberRole
import dev.ithurts.domain.workspace.WorkspaceMemberRole.*
import org.springframework.security.access.PermissionEvaluator
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component
import java.io.Serializable

@Component
class WorkspacePermissionEvaluator(
    private val permissionQueryRepository: PermissionQueryRepository
) : PermissionEvaluator {
    override fun hasPermission(authentication: Authentication?, targetDomainObject: Any?, permission: Any?): Boolean {
        throw NotImplementedError()
    }

    override fun hasPermission(
        authentication: Authentication?,
        target: Serializable,
        targetType: String,
        permission: Any
    ): Boolean {
        authentication ?: return false
        val requiredRole = valueOf(permission as String)
        val accountId = when (val principal = authentication.principal) {
            is AuthenticatedOAuth2User -> principal.accountId
            is Account -> principal.id
            is Workspace -> return target == principal.id
            else -> return false
        }
        return when (targetType) {
            "Workspace" -> evaluatePermissionToWorkspace(target, accountId, requiredRole)
            "Repository" -> evaluatePermissionToRepository(target, accountId, requiredRole)
            "Debt" -> evaluatePermissionToDebt(target, accountId, requiredRole)
            else -> false
        }
    }

    private fun evaluatePermissionToDebt(
        target: Serializable,
        accountId: String,
        requiredRole: WorkspaceMemberRole
    ): Boolean {
        val member = permissionQueryRepository.getWorkspaceMemberByDebtId(target as String, accountId) ?: return false
        return checkMemberPermission(member, requiredRole)
    }

    private fun evaluatePermissionToRepository(
        target: Serializable,
        accountId: String,
        requiredRole: WorkspaceMemberRole
    ): Boolean {
        when (target) {
            is String -> {
                val member =
                    permissionQueryRepository.getWorkspaceMemberByRepositoryId(target, accountId) ?: return false
                return checkMemberPermission(member, requiredRole)
            }
            is RepositoryInfo -> {
                val member =
                    permissionQueryRepository.getWorkspaceMemberByRepositoryInfo(target, accountId) ?: return false
                return checkMemberPermission(member, requiredRole)
            }
            else -> return false
        }
    }

    private fun checkMemberPermission(
        member: WorkspaceMember,
        requiredRole: WorkspaceMemberRole
    ) = member.role == requiredRole || (member.role == ADMIN && requiredRole == MEMBER)

    private fun evaluatePermissionToWorkspace(
        targetId: Serializable,
        accountId: String,
        requiredRole: WorkspaceMemberRole
    ): Boolean {
        val member = permissionQueryRepository.getWorkspaceMemberByWorkspaceId(targetId as String, accountId) ?: return false
        return checkMemberPermission(member, requiredRole)
    }
}