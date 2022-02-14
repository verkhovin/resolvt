package dev.ithurts.domain.workspace

class SourceProviderApplicationCredentials(
    val clientKey: String,
    val secret: String
) {
    companion object {
        fun from(clientKey: String, secret: String): SourceProviderApplicationCredentials {
            return SourceProviderApplicationCredentials(clientKey, secret)
        }
    }
}