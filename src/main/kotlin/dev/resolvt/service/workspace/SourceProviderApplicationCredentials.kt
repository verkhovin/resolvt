package dev.resolvt.service.workspace

import com.bol.secure.Encrypted
import org.springframework.data.mongodb.core.mapping.Field

data class SourceProviderApplicationCredentials(
    @Field
    val clientKey: Any,
    @Field
    @Encrypted
    val secret: String?
) {
    companion object {
        fun from(clientKey: Any, secret: String?): SourceProviderApplicationCredentials {
            return SourceProviderApplicationCredentials(clientKey, secret)
        }
    }
}