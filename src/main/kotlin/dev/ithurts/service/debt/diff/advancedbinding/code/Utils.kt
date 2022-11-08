package dev.ithurts.service.debt.diff.advancedbinding.code

fun jvmSimpleClassName(name: String?): String? {
    return name?.substringAfterLast(".")
}