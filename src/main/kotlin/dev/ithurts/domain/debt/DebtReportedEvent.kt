package dev.ithurts.domain.debt

import org.springframework.context.ApplicationEvent

class DebtReportedEvent(
    val debt: Debt,
    source: Any
): ApplicationEvent(source)