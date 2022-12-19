package dev.resolvt.query.model

import dev.resolvt.service.account.Account

data class DebtAccountDto(
    val name: String
) {
    companion object {
        fun from(account: Account?): DebtAccountDto {
            return DebtAccountDto(account?.name ?: "Unknown")
        }
    }
}
