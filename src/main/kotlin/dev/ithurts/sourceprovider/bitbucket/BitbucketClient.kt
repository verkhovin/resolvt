package dev.ithurts.sourceprovider.bitbucket

import dev.ithurts.sourceprovider.bitbucket.model.BitbucketUserEmailInfo
import dev.ithurts.sourceprovider.bitbucket.model.BitbucketUserEmails
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.client.RestOperations
import org.springframework.web.client.exchange

@Component
class BitbucketClient(@Qualifier("bitbucketRestTemplate") private val restTemplate: RestOperations) {
    fun getUserPrimaryEmail(accessToken: String): String {
        val emails: BitbucketUserEmails = restTemplate.exchange<BitbucketUserEmails>(
            "/user/emails",
            HttpMethod.GET,
            HttpEntity(null, authorizationHeader(accessToken))
        ).body!!
        return (emails.values.firstOrNull { it.isPrimary } ?: emails.values[0]).email
    }

    fun authorizationHeader(accessToken: String): HttpHeaders {
        val httpHeaders = HttpHeaders()
        httpHeaders.setBearerAuth(accessToken)
        return httpHeaders
    }
}