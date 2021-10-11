package dev.ithurts

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class IthurtsApplication

fun main(args: Array<String>) {
    runApplication<IthurtsApplication>(*args)
}
