package dev.ithurts.service.account

import dev.ithurts.service.SourceProvider
import org.springframework.data.repository.CrudRepository

interface AccountRepository: CrudRepository<Account, String> {
    fun findByEmail(email: String): Account?
    fun findByExternalIdAndSourceProvider(externalId: String, sourceProvider: SourceProvider): Account?
}