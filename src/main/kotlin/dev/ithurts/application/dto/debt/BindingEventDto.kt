package dev.ithurts.application.dto.debt

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
    ADVANCED_BINDING_TARGET_RENAMED ("was renamed"),

}

