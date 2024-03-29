package dev.resolvt.configuration

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(prefix = "resolvt")
class ApplicationProperties(
    val baseUrl: String,
    val bitbucket: BitbucketProperties?,
    val github: GithubProperties?,
    val showMainPage: Boolean = false,
    val privateBeta: Boolean = false
)

class BitbucketProperties(
    val appName: String = ""
)

class GithubProperties(
    val appName: String = "",
    val appId: String = "",
    val tokenSignaturePrivateKey: String = "",
    val webhookSecret: String = "",
    val workspaceCreationWaiterSeconds: Int = 3
)