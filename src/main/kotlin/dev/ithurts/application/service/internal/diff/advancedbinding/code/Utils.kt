package dev.ithurts.application.service.internal.diff.advancedbinding.code

fun jvmSimpleClassName(name: String?): String? {
    return name?.substringAfterLast(".")
}