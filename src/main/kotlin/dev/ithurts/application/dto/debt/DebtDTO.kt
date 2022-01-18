package dev.ithurts.application.dto.debt

import dev.ithurts.domain.debt.Debt
import dev.ithurts.domain.debt.DebtStatus

data class DebtDTO(
    val id: Long,
    val title: String,
    val description: String,
    val status: DebtStatus,
    val filePath: String,
    val startLine: Int,
    val endLine: Int,
    val votes: Int,
    val sourceLink: SourceLink,
    val repository: DebtRepositoryDTO,
    val debtAccountDTO: DebtAccountDTO
) {
    companion object {
        fun from(debt: Debt, sourceLink: SourceLink, debtRepositoryDTO: DebtRepositoryDTO, debtAccountDTO: DebtAccountDTO): DebtDTO {
            return DebtDTO(
                debt.identity,
                debt.title,
                debt.description,
                debt.status,
                debt.filePath,
                debt.startLine,
                debt.endLine,
                debt.votes,
                sourceLink,
                debtRepositoryDTO,
                debtAccountDTO
            )
        }
    }
}

data class SourceLink(
    val url: String,
    val text: String,
)