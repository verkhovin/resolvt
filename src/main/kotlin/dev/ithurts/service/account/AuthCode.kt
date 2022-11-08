package dev.ithurts.service.account

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document(collection = "auth_codes")
data class AuthCode(
    val authCode: String,
    val codeChallenge: String,
    val accountId: String,
    val expiresAt: Instant,
    val used: Boolean = false,
    @Id
    val _id: String? = null
) {
    val id: String
        get() = _id!!

    fun use(): AuthCode {
        return this.copy(
            used = true
        )
    }

    fun regenerate(authCode: String, codeChallenge: String, expiresAt: Instant): AuthCode {
        return this.copy(
            authCode = authCode,
            codeChallenge = codeChallenge,
            expiresAt = expiresAt
        )
    }
}