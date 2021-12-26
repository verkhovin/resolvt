package dev.ithurts.configuration

import dev.ithurts.controller.web.SessionEnrichingHandlerInterceptor
import nz.net.ultraq.thymeleaf.LayoutDialect
import nz.net.ultraq.thymeleaf.decorators.strategies.GroupingRespectLayoutTitleStrategy
import org.springframework.boot.actuate.trace.http.HttpTraceRepository
import org.springframework.boot.actuate.trace.http.InMemoryHttpTraceRepository
import org.springframework.context.annotation.Bean
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

    @Bean
    fun layoutDialect(): LayoutDialect? {
        return LayoutDialect(GroupingRespectLayoutTitleStrategy())
    }

    @Bean
    fun httpTraceRepository(): HttpTraceRepository? {
        return InMemoryHttpTraceRepository()
    }
}