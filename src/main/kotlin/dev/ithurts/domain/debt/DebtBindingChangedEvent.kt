package dev.ithurts.domain.debt
import org.springframework.context.ApplicationEvent

class DebtBindingChangedEvent (
    val source: Debt,
    val debtId: String,
    val repositoryId: String,
    val commitHash: String,
    val changes: List<BindingChange>
): ApplicationEvent(source)


