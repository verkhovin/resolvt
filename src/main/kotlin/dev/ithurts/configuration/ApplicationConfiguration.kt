package dev.ithurts.configuration

import dev.ithurts.application.service.advancedbinding.code.KotlinCodeAnalyzer
import dev.ithurts.application.service.advancedbinding.KotlinBindingService
import dev.ithurts.application.service.advancedbinding.LanguageSpecificBindingService
import dev.ithurts.domain.CostCalculationService
import dev.ithurts.domain.Language
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ApplicationConfiguration {
    @Bean
    fun languageSpecificBindingServices(kotlinCodeAnalyzer: KotlinCodeAnalyzer): Map<Language, LanguageSpecificBindingService> {
        return mapOf(
            Language.KOTLIN to KotlinBindingService(kotlinCodeAnalyzer)
        )
    }

    @Bean
    fun costCalculationService(): CostCalculationService {
        return CostCalculationService()
    }
}