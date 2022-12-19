package dev.resolvt.service.debt

import dev.resolvt.service.debt.model.Debt
import org.springframework.context.ApplicationEvent

class DebtReportedEvent(
    val debt: Debt,
    source: Any
): ApplicationEvent(source)