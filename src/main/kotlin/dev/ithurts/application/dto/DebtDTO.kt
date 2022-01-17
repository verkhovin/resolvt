package dev.ithurts.application.dto

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
    val repository: RepositoryDTO,
    val accountDTO: AccountDTO
) {
    companion object {
        fun from(debt: Debt, sourceLink: SourceLink, repositoryDTO: RepositoryDTO, accountDTO: AccountDTO): DebtDTO {
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
                repositoryDTO,
                accountDTO
            )
        }
    }
}

data class SourceLink(
    val url: String,
    val text: String,
)