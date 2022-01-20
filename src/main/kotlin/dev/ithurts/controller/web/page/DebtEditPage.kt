package dev.ithurts.controller.web.page

import dev.ithurts.application.dto.debt.DebtDto
import dev.ithurts.domain.debt.DebtStatus

data class DebtEditPage(
    val debt: DebtDto
) {
    val form = DebtEditForm(
        debt.title,
        debt.description,
        debt.status,
        debt.filePath,
        "${debt.startLine}:${debt.endLine}"
    )
}

data class DebtEditForm(
    val title: String,
    val description: String,
    var status: DebtStatus?, // Null if status dropdown is disabled, should be set by chosen action
    val filePath: String,
    val linesSpec: String
)
