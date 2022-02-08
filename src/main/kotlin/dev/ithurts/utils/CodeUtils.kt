package dev.ithurts.utils

fun simpleJvmClassName(className: String?): String? {
    return className?.substringAfterLast(".")
}