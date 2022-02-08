package dev.ithurts.application.dto.debt

import dev.ithurts.domain.bindingevent.ChangeType
import java.time.Instant

data class CommitEventsDto(
    val commitHash: String,
    val commitUrl: String,
    val events: List<BindingEventDto>
)

data class BindingEventDto (
    val binding: BindingDto,
    val changes: List<ChangeDto>,
    val createdAt: Instant
    )

data class ChangeDto(
    val type: ChangeType,
    val from: String?,
    val to: String?
)
