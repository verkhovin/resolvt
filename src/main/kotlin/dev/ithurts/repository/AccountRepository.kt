package dev.ithurts.repository

import dev.ithurts.model.Account
import org.springframework.data.repository.CrudRepository

interface AccountRepository: CrudRepository<Account, String> {
    fun findByEmail(email: String): Account?
}