package dev.ithurts.configuration

import dev.ithurts.service.SourceProvider
import org.springframework.core.env.Environment
import org.springframework.stereotype.Service

@Service
class ConfigurationService(
    private val environment: Environment,
) {
    fun getEnabledSourceProviders(): List<SourceProvider> =
        environment.getRequiredProperty("ithurts.source-providers.enabled", List::class.java).map {
            it as String
            SourceProvider.valueOf(it.uppercase())
        }

    fun isBitbucketEnabled(): Boolean =
        environment.getRequiredProperty("ithurts.source-providers.enabled", List::class.java).contains("bitbucket")

    fun isGithubEnabled(): Boolean =
        environment.getRequiredProperty("ithurts.source-providers.enabled", List::class.java).contains("github")

}