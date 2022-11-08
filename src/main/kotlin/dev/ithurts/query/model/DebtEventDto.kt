package dev.ithurts.query.model

import java.time.Instant

data class DebtEventDto(
    val commitHash: String,
    val commitUrl: String,
    val changes: List<ChangeDto>,
    val createdAt: Instant
)

data class ChangeDto(
    val binding: BindingDto,
    val type: ChangeType,
    val from: String?,
    val to: String?
)

enum class ChangeType(val title: String) {
    CODE_CHANGED("was changed"),
    MOVED ("was moved"),
    ADVANCED_BINDING_TARGET_LOST ("tracking was lost"),
    GENERIC("was changed")
}

