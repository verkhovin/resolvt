//package dev.ithurts.repository
//
//import dev.ithurts.model.SourceProvider
//import dev.ithurts.model.UserExternalAccount
//import org.springframework.data.repository.CrudRepository
//
//interface UserExternalAccountRepository: CrudRepository<UserExternalAccount, String> {
//    fun findBySourceProviderAndExternalId(sourceProvider: SourceProvider, externalId: String): UserExternalAccount?
//}