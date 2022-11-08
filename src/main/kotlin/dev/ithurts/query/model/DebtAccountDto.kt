package dev.ithurts.query.model

import dev.ithurts.service.account.Account

data class DebtAccountDto(
    val name: String
) {
    companion object {
        fun from(account: Account?): DebtAccountDto {
            return DebtAccountDto(account?.name ?: "Unknown")
        }
    }
}
