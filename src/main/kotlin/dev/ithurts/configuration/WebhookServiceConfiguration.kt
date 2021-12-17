package dev.ithurts.configuration

import io.reflectoring.diffparser.api.DiffParser
import io.reflectoring.diffparser.api.UnifiedDiffParser
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class WebhookServiceConfiguration {
    @Bean
    fun diffParser() : DiffParser {
        return UnifiedDiffParser()
    }
}