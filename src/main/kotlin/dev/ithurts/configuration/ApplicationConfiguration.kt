package dev.ithurts.configuration

import dev.ithurts.application.service.advancedbinding.JavaBindingService
import dev.ithurts.application.service.advancedbinding.KotlinBindingService
import dev.ithurts.application.service.advancedbinding.LanguageSpecificBindingService
import dev.ithurts.application.service.advancedbinding.code.JavaCodeAnalyzer
import dev.ithurts.application.service.advancedbinding.code.KotlinCodeAnalyzer
import dev.ithurts.domain.CostCalculationService
import dev.ithurts.domain.Language
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ApplicationConfiguration {
    @Bean
    fun languageSpecificBindingServices(
        kotlinCodeAnalyzer: KotlinCodeAnalyzer,
        javaCodeAnalyzer: JavaCodeAnalyzer
    ): Map<Language, LanguageSpecificBindingService> {
        return mapOf(
            Language.KOTLIN to KotlinBindingService(kotlinCodeAnalyzer),
            Language.JAVA to JavaBindingService(javaCodeAnalyzer)
        )
    }

    @Bean
    fun costCalculationService(): CostCalculationService {
        return CostCalculationService()
    }
}