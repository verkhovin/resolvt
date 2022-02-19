package dev.ithurts.application.model.debt

import dev.ithurts.domain.account.Account

data class DebtAccountDto(
    val name: String
) {
    companion object {
        fun from(account: Account?): DebtAccountDto {
            return DebtAccountDto(account?.name ?: "Unknown")
        }
    }
}
