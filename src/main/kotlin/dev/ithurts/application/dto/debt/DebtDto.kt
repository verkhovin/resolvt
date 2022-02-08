package dev.ithurts.application.dto.debt

import dev.ithurts.domain.debt.Debt
import dev.ithurts.domain.debt.DebtStatus
import java.time.LocalDateTime

data class DebtDto(
    val id: String,
    val title: String,
    val description: String,
    val status: DebtStatus,
    val filePath: String, //TODO MVP REMOVED
    val startLine: Int, //TODO MVP REMOVED
    val endLine: Int, //TODO MVP REMOVED
    val votes: Int,
    val voted: Boolean,
    val sourceLink: SourceLink, //TODO MVP REMOVED
    val repository: DebtRepositoryDto,
    val reporter: DebtAccountDto,
    val cost: Int = 0, // TODO MVP
) {
    companion object {
        fun from(
            debt: Debt,
            sourceLink: SourceLink,
            repository: DebtRepositoryDto,
            reporter: DebtAccountDto,
            currentUserVoted: Boolean
        ): DebtDto {
            return DebtDto(
                debt.id,
                debt.title,
                debt.description,
                debt.status,
                debt.bindings[0].filePath,
                debt.bindings[0].startLine,
                debt.bindings[0].endLine,
                debt.votes.size,
                currentUserVoted,
                sourceLink,
                repository,
                reporter,
            )
        }
    }
}