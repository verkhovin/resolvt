package dev.ithurts.utils

fun jvmSimpleClassName(className: String?): String? {
    return className?.substringAfterLast(".")
}