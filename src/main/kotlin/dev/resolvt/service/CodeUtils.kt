package dev.resolvt.application.utils

fun jvmSimpleClassName(className: String?): String? {
    return className?.substringAfterLast(".")
}