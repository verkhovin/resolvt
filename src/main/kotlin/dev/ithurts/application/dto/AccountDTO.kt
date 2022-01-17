package dev.ithurts.application.dto

import dev.ithurts.domain.account.Account

data class AccountDTO(
    val name: String
) {
    companion object {
        fun from(account: Account?): AccountDTO {
            return AccountDTO(account?.name ?: "Unknown")
        }
    }
}
