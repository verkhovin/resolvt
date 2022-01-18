package dev.ithurts.domain.authcode

import dev.ithurts.domain.authcode.AuthCode
import org.springframework.data.repository.CrudRepository
import java.time.Instant

interface AuthCodeRepository: CrudRepository<AuthCode, Long> {
    fun getByAccountIdAndExpiresAtAfterAndUsedIsFalse(userId: Long, expiresAfter: Instant = Instant.now()): AuthCode?
    fun getByAuthCodeAndCodeChallenge(authCode: String, codeChallenge: String): AuthCode?
}