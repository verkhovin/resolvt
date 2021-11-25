package dev.ithurts.configuration

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Clock

@Configuration
class CommonConfiguration {
    @Bean
    fun clock(): Clock {
        return Clock.systemUTC()
    }
}