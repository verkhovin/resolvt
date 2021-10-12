package dev.ithurts.repository

import dev.ithurts.model.Account
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository

interface AccountRepository: CrudRepository<Account, Long> {
    fun findByEmail(email: String): Account?


}