package dev.ithurts.configuration

import dev.ithurts.security.SessionEnrichingHandlerInterceptor
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfiguration(
    private val sessionEnrichingHandlerInterceptor: SessionEnrichingHandlerInterceptor
): WebMvcConfigurer {
    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(sessionEnrichingHandlerInterceptor)
    }
}