package dev.resolvt.service.beta

import org.springframework.data.repository.CrudRepository

interface PrivateBetaUserRepository : CrudRepository<PrivateBetaUser, String> {
    fun findByEmailAndAcceptedIsTrue(email: String): PrivateBetaUser?
}