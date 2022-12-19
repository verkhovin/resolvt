package dev.resolvt

import dev.resolvt.configuration.ApplicationProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties(ApplicationProperties::class)
class ResolvtApplication

fun main(args: Array<String>) {
    runApplication<ResolvtApplication>(*args)
}
