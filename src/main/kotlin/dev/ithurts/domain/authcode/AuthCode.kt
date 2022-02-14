package dev.ithurts.domain.authcode

import org.bson.codecs.pojo.annotations.BsonId
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document(collection = "auth_codes")
data class AuthCode(
    var authCode: String,
    var codeChallenge: String,
    val accountId: String,
    var expiresAt: Instant,
    var used: Boolean = false,
    @BsonId
    val _id: String? = null
) {
    val id: String
        get() = _id!!
}