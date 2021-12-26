package dev.ithurts.controller.api.dto

import dev.ithurts.model.Account

data class AccountDTO(
    val name: String
) {
    companion object {
        fun from(account: Account): AccountDTO {
            return AccountDTO(account.name)
        }
    }
}
