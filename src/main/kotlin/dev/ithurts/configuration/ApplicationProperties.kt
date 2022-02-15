package dev.ithurts.configuration

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(prefix = "ithurts")
class ApplicationProperties(
    val baseUrl: String,
    val bitbucket: BitbucketProperties,
)

class BitbucketProperties(
    val appName: String = ""
)