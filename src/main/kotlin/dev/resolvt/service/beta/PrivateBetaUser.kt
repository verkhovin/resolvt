package dev.resolvt.service.beta

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "privateBetaUsers")
class PrivateBetaUser(
    val email: String,
    val accepted: Boolean,
    @Id
    val _id: String? = null
) {
    val id: String
        get() = _id!!
}