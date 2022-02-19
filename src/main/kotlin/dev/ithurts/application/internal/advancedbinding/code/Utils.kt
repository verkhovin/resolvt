package dev.ithurts.application.internal.advancedbinding.code

fun jvmSimpleClassName(name: String?): String? {
    return name?.substringAfterLast(".")
}