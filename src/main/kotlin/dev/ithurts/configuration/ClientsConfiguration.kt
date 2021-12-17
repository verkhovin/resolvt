package dev.ithurts.configuration

import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate

@Configuration
class ClientsConfiguration {
    @Bean
    fun commonRestTemplate(builder: RestTemplateBuilder): RestTemplate {
        return builder.build()
    }

    @Bean
    fun bitbucketRestTemplate(restTemplateBuilder: RestTemplateBuilder) =
        restTemplateBuilder.rootUri("https://api.bitbucket.org/2.0").build()
}