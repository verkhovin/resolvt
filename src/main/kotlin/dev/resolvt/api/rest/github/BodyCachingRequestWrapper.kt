package dev.resolvt.api.rest.github

import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.InputStreamReader
import javax.servlet.ReadListener
import javax.servlet.ServletInputStream
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletRequestWrapper

class BodyCachingRequestWrapper(request: HttpServletRequest): HttpServletRequestWrapper(request) {
    private val cache: ByteArray = request.inputStream.readAllBytes()

    override fun getInputStream(): ServletInputStream {
        return CacheServletInputStream(cache)
    }

    override fun getReader(): BufferedReader {
        return BufferedReader(InputStreamReader(ByteArrayInputStream(cache)))
    }
}

class CacheServletInputStream(cache: ByteArray) : ServletInputStream() {
    private val cacheInputStream = ByteArrayInputStream(cache)

    override fun read(): Int = cacheInputStream.read()

    override fun isFinished(): Boolean = cacheInputStream.available() == 0

    override fun isReady(): Boolean = true

    override fun setReadListener(listener: ReadListener?) {
        throw UnsupportedOperationException()
    }

}
