package dev.ithurts.application.dto.debt

import dev.ithurts.domain.debt.Debt
import dev.ithurts.domain.debt.DebtStatus
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

data class DebtDto(
    val id: String,
    val title: String,
    val description: String,
    val status: DebtStatus,
    val bindings: List<BindingDto>,
    val votes: Int,
    val voted: Boolean,
    val repository: DebtRepositoryDto,
    val reporter: DebtAccountDto,
    val createdAt: Instant,
    val updatedAt: Instant,
    val cost: Int
) {
    companion object {
        fun from(
            debt: Debt,
            bindings: List<BindingDto>,
            repository: DebtRepositoryDto,
            reporter: DebtAccountDto,
            currentUserVoted: Boolean,
            cost: Int,
        ): DebtDto {
            return DebtDto(
                debt.id,
                debt.title,
                debt.description,
                debt.status,
                bindings,
                debt.votes.size,
                currentUserVoted,
                repository,
                reporter,
                debt.createdAt,
                debt.updatedAt,
                cost
            )
        }
    }
}