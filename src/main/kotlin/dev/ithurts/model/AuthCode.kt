package dev.ithurts.model

import java.time.Instant
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

@Entity
class AuthCode(
    var authCode: String,
    var codeChallenge: String,
    val accountId: Long,
    var expiresAt: Instant,
    var used: Boolean = false,
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null
)