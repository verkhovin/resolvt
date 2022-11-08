package dev.ithurts.service.debt

import dev.ithurts.service.debt.model.Debt
import org.springframework.context.ApplicationEvent

class DebtReportedEvent(
    val debt: Debt,
    source: Any
): ApplicationEvent(source)