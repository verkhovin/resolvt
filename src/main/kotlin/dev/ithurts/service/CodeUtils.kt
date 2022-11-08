package dev.ithurts.application.utils

fun jvmSimpleClassName(className: String?): String? {
    return className?.substringAfterLast(".")
}