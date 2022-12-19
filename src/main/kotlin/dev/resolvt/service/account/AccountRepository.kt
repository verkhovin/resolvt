package dev.resolvt.service.account

import dev.resolvt.service.SourceProvider
import org.springframework.data.repository.CrudRepository

interface AccountRepository: CrudRepository<Account, String> {
    fun findByEmailAndSourceProvider(email: String, sourceProvider: SourceProvider): Account?
    fun findByExternalIdAndSourceProvider(externalId: String, sourceProvider: SourceProvider): Account?
}