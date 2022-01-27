package dev.ithurts.application.dto.debt

import dev.ithurts.domain.debt.Debt
import dev.ithurts.domain.debt.DebtStatus

data class DebtDto(
    val id: Long,
    val title: String,
    val description: String,
    val status: DebtStatus,
    val filePath: String,
    val startLine: Int,
    val endLine: Int,
    val votes: Int,
    val voted: Boolean,
    val sourceLink: SourceLink,
    val repository: DebtRepositoryDto,
    val debtReporterAccount: DebtAccountDto,
    val test: String = "AAAA"
) {
    companion object {
        fun from(
            debt: Debt, sourceLink: SourceLink, debtRepositoryDTO: DebtRepositoryDto, debtReportedAccount: DebtAccountDto,
            currentUserVoted: Boolean
        ): DebtDto {
            return DebtDto(
                debt.identity,
                debt.title,
                debt.description,
                debt.status,
                debt.bindings[0].filePath,
                debt.bindings[0].startLine,
                debt.bindings[0].endLine,
                debt.votes.size,
                currentUserVoted,
                sourceLink,
                debtRepositoryDTO,
                debtReportedAccount
            )
        }
    }
}

data class SourceLink(
    val url: String,
    val text: String,
)