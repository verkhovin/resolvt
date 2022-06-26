package dev.ithurts.domain.account

import dev.ithurts.domain.SourceProvider
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "accounts")
data class Account (
    val email: String,
    val name: String,
    val sourceProvider: SourceProvider,
    val externalId: String,
    @Id
    val _id: String? = null
) {
    val id: String
        get() = _id!!
}