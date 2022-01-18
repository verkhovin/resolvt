package dev.ithurts.domain.workspace

import javax.persistence.*

@Embeddable
class WorkspaceMember(
    val accountId: Long,
    @Enumerated(EnumType.STRING)
    val role: WorkspaceMemberRole,
    @Enumerated(EnumType.STRING)
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