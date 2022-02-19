package dev.ithurts.controller.web.page

import dev.ithurts.application.model.debt.DebtDto
import dev.ithurts.domain.debt.DebtStatus

data class DebtEditPage(
    val debt: DebtDto
) {
    val form = DebtEditForm(
        debt.title,
        debt.description,
        debt.status
    )
}

data class DebtEditForm(
    val title: String,
    val description: String,
    var status: DebtStatus
)