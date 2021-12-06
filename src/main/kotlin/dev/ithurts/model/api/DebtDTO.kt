package dev.ithurts.model.api

import dev.ithurts.model.debt.Debt
import dev.ithurts.model.debt.DebtStatus

data class DebtDTO(
    val id: Long,
    val title: String,
    val description: String,
    val status: DebtStatus,
    val filePath: String,
    val startLine: Int,
    val endLine: Int,
    val votes: Int,
    val accountDTO: AccountDTO
) {
    companion object {
        fun from(debt: Debt): DebtDTO {
            return DebtDTO(
                debt.id!!,
                debt.title,
                debt.description,
                debt.status,
                debt.filePath,
                debt.startLine,
                debt.endLine,
                debt.votes,
                AccountDTO.from(debt.account)
            )
        }
    }
}