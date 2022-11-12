package dev.ithurts.service.account

import dev.ithurts.service.SourceProvider
import org.springframework.data.repository.CrudRepository

interface AccountRepository: CrudRepository<Account, String> {
    fun findByEmailAndSourceProvider(email: String, sourceProvider: SourceProvider): Account?
    fun findByExternalIdAndSourceProvider(externalId: String, sourceProvider: SourceProvider): Account?
}