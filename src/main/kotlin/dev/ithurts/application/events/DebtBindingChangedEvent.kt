package dev.ithurts.application.events

import dev.ithurts.domain.debt.BindingChange
import org.springframework.context.ApplicationEvent

class DebtBindingChangedEvent (
    source: Any,
    val debtId: String,
    val repositoryId: String,
    val commitHash: String,
    val changes: List<BindingChange>
): ApplicationEvent(source)


