package dev.resolvt.query.model

import dev.resolvt.service.debt.model.BindingStatus
import dev.resolvt.service.debt.model.Debt
import dev.resolvt.service.debt.model.DebtStatus
import java.time.Instant

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
    val cost: Int,
    val hasBindingTrackingLost: Boolean
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
                bindings.filter { it.status != BindingStatus.ARCHIVED },
                debt.votes.size,
                currentUserVoted,
                repository,
                reporter,
                debt.createdAt,
                debt.updatedAt,
                cost,
                debt.bindings.any { binding -> binding.status == BindingStatus.TRACKING_LOST }
            )
        }
    }
}