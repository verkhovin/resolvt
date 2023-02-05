package dev.resolvt.configuration

import dev.resolvt.api.web.SessionEnrichingHandlerInterceptor
import nz.net.ultraq.thymeleaf.LayoutDialect
import nz.net.ultraq.thymeleaf.decorators.strategies.GroupingRespectLayoutTitleStrategy
import org.springframework.boot.actuate.trace.http.HttpTraceRepository
import org.springframework.boot.actuate.trace.http.InMemoryHttpTraceRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.CacheControl
import org.springframework.web.servlet.config.annotation.*
import java.util.concurrent.TimeUnit


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

@Configuration
@EnableWebMvc
class MvcConfiguration: WebMvcConfigurer{
    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        registry.addResourceHandler("/public")
            .addResourceLocations("classpath:/static/public")
            .setCacheControl(CacheControl.maxAge(1, TimeUnit.DAYS))
        registry.addResourceHandler("/**")
            .addResourceLocations("classpath:/static/")
    }

    override fun addViewControllers(registry: ViewControllerRegistry) {
        registry.addViewController("/login").setViewName("login")
    }
}