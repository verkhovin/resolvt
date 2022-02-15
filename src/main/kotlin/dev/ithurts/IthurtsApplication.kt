package dev.ithurts

import dev.ithurts.configuration.ApplicationProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties(ApplicationProperties::class)
class IthurtsApplication

fun main(args: Array<String>) {
    runApplication<IthurtsApplication>(*args)
}
