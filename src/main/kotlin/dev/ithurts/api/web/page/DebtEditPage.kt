package dev.ithurts.api.web.page

import dev.ithurts.query.model.DebtDto
import dev.ithurts.service.debt.model.DebtStatus

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