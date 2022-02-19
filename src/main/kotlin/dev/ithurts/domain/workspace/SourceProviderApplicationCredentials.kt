package dev.ithurts.domain.workspace

import com.bol.secure.Encrypted
import org.springframework.data.mongodb.core.mapping.Field

class SourceProviderApplicationCredentials(
    @Field
    val clientKey: String,
    @Field
    @Encrypted
    val secret: String
) {
    companion object {
        fun from(clientKey: String, secret: String): SourceProviderApplicationCredentials {
            return SourceProviderApplicationCredentials(clientKey, secret)
        }
    }
}