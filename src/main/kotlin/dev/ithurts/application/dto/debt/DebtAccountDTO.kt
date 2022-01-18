package dev.ithurts.application.dto.debt

import dev.ithurts.domain.account.Account

data class DebtAccountDTO(
    val name: String
) {
    companion object {
        fun from(account: Account?): DebtAccountDTO {
            return DebtAccountDTO(account?.name ?: "Unknown")
        }
    }
}
