package dev.ithurts.domain.account

import dev.ithurts.domain.SourceProvider
import org.springframework.data.repository.CrudRepository

interface AccountRepository: CrudRepository<Account, Long> {
    fun findByEmail(email: String): Account?
    fun findByExternalIdAndSourceProvider(externalId: String, sourceProvider: SourceProvider): Account?

}