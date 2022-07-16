package dev.ithurts.domain.authcode

import org.springframework.data.repository.CrudRepository
import java.time.Instant

interface AuthCodeRepository: CrudRepository<AuthCode, String> {
    fun getByAccountIdAndExpiresAtAfterAndUsedIsFalse(accountId: String, expiresAfter: Instant = Instant.now()): AuthCode?
    fun getByAuthCodeAndCodeChallengeAndUsedIsFalse(authCode: String, codeChallenge: String): AuthCode?
}