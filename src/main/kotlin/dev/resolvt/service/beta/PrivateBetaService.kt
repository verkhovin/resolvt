package dev.resolvt.service.beta

import dev.resolvt.configuration.ApplicationProperties
import org.springframework.stereotype.Service

@Service
class PrivateBetaService(
    private val privateBetaUserRepository: PrivateBetaUserRepository,
    private val applicationProperties: ApplicationProperties
) {
    fun canLogin(email: String): Boolean {
        if(!applicationProperties.privateBeta) return true
        return privateBetaUserRepository.findByEmailAndAcceptedIsTrue(email) != null
    }

    fun createBetaAccessRequest(email: String) {
        privateBetaUserRepository.save(PrivateBetaUser(email, false))
    }
}