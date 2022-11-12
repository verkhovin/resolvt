package dev.ithurts.util

import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders

fun noBody(accessToken: String) = HttpEntity(null, httpHeaders(mapOf("Authorization" to "Bearer $accessToken")))
fun noBody(accessToken: String, headers: Map<String, String>) = HttpEntity(null,
    httpHeaders(mapOf("Authorization" to "Bearer $accessToken") + headers))

fun httpHeaders(headers: Map<String, String>): HttpHeaders {
    val httpHeaders = HttpHeaders()
    headers.forEach {
        httpHeaders.add(it.key, it.value)
    }
    return httpHeaders
}