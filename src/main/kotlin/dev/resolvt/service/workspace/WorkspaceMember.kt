package dev.resolvt.service.workspace

class WorkspaceMember(
    val accountId: String,
    val role: WorkspaceMemberRole,
    val status: WorkspaceMemberStatus
) {
    val isActive: Boolean
        get() = status == WorkspaceMemberStatus.ACTIVE
}

enum class WorkspaceMemberStatus {
    ACTIVE, INACTIVE, INVITED
}

enum class WorkspaceMemberRole {
    ADMIN, MEMBER
}