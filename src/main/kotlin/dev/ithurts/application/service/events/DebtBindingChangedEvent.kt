package dev.ithurts.application.service.events

import org.springframework.context.ApplicationEvent

class DebtBindingChangedEvent (
    source: Any,
    val debtId: String,
    val bindingId: String,
    val repositoryId: String,
    val commitHash: String,
    val changes: List<Change>
): ApplicationEvent(source)

class Change(
    val type: ChangeType,
    val from: String?,
    val to: String?
)

enum class ChangeType {
    CODE_CHANGED,
    MOVED,
    ADVANCED_BINDING_TARGET_RENAMED
}
