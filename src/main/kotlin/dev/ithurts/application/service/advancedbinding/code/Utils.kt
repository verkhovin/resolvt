package dev.ithurts.application.service.advancedbinding.code

fun jvmSimpleClassName(name: String?): String? {
    return name?.substringAfterLast(".")
}