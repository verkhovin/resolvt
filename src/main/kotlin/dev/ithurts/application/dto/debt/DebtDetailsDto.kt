package dev.ithurts.application.dto.debt

import dev.ithurts.domain.debt.Debt
import java.time.LocalDateTime
import java.time.ZoneOffset

data class DebtDetailsDto(
    val debt: DebtDto,
    val bindings: List<BindingDto>,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val commits: List<CommitEventsDto>
) {
    companion object {
        fun from(
            debt: Debt,
            sourceLink: SourceLink,
            bindings: List<BindingDto>,
            repository: DebtRepositoryDto,
            reporter: DebtAccountDto,
            currentUserVoted: Boolean,
            bindingEventsByCommitHash: List<CommitEventsDto>
        ): DebtDetailsDto {
            return DebtDetailsDto(
                DebtDto.from(debt, sourceLink, repository, reporter, currentUserVoted),
                bindings,
                LocalDateTime.ofInstant(debt.createdAt, ZoneOffset.UTC),
                LocalDateTime.ofInstant(debt.updatedAt, ZoneOffset.UTC),
                bindingEventsByCommitHash
            )
        }
    }
}
