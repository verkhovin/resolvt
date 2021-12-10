package dev.ithurts.repository

import dev.ithurts.model.Account
import dev.ithurts.model.SourceProvider
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository

interface AccountRepository: CrudRepository<Account, Long> {
    fun findByEmail(email: String): Account?
    fun findByExternalIdAndSourceProvider(externalId: String, sourceProvider: SourceProvider): Account?

}