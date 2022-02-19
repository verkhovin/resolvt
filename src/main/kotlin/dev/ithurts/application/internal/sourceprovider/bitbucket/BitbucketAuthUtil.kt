package dev.ithurts.application.internal.sourceprovider.bitbucket

import org.apache.tomcat.util.buf.HexUtils
import java.security.MessageDigest

fun buildQueryStringHash(canonicalUrl: String): String? {
    val md: MessageDigest = MessageDigest.getInstance("SHA-256")
    md.update(canonicalUrl.toByteArray(charset("UTF-8")))
    val digest: ByteArray = md.digest()
    return HexUtils.toHexString(digest)
}
