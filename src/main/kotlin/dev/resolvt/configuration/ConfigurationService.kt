package dev.resolvt.configuration

import dev.resolvt.service.SourceProvider
import org.springframework.core.env.Environment
import org.springframework.stereotype.Service

@Service
class ConfigurationService(
    private val environment: Environment
) {
    fun getEnabledSourceProviders(): List<SourceProvider> =
        environment.getRequiredProperty("resolvt.source-providers.enabled", List::class.java).map {
            it as String
            SourceProvider.valueOf(it.uppercase())
        }

    fun isBitbucketEnabled(): Boolean =
        environment.getRequiredProperty("resolvt.source-providers.enabled", List::class.java).contains("bitbucket")

    fun isGithubEnabled(): Boolean =
        environment.getRequiredProperty("resolvt.source-providers.enabled", List::class.java).contains("github")

}