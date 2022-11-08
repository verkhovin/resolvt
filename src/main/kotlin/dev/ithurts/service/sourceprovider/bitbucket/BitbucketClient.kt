package dev.ithurts.service.sourceprovider.bitbucket

import dev.ithurts.service.sourceprovider.SourceProviderClient
import dev.ithurts.service.sourceprovider.bitbucket.model.BitbucketRepository
import dev.ithurts.service.sourceprovider.bitbucket.model.BitbucketUserEmailInfo
import dev.ithurts.service.sourceprovider.bitbucket.model.Values
import dev.ithurts.service.sourceprovider.model.SourceProviderRepository
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.client.RestOperations
import org.springframework.web.client.exchange

@Service
class BitbucketClient(
    @Qualifier("bitbucketRestTemplate") val restTemplate: RestOperations
) : SourceProviderClient {
    override val organisationOwnerRole: String = "owner"

    override fun getDiff(accessToken: String, organisation: String, repository: String, spec: String): String {
        return restTemplate.exchange(
            "/repositories/${organisation}/${repository}/diff/${spec}?merge=false",
            HttpMethod.GET,
            noBody(accessToken),
            String::class.java
        ).body ?: ""
    }

    override fun getRepository(
        accessToken: String,
        organisation: String,
        repository: String
    ): SourceProviderRepository {
        val bitbucketRepository = restTemplate.exchange<BitbucketRepository>(
            "/repositories/${organisation}/${repository}",
            HttpMethod.GET,
            noBody(accessToken)
        ).body!!
        return SourceProviderRepository(bitbucketRepository.name, bitbucketRepository.mainbranch.name)
    }

    override fun getFile(
        accessToken: String,
        workspace: String,
        repository: String,
        filePath: String,
        commitHashOrBranch: String
    ): String {
        return restTemplate.exchange<String>(
            "/repositories/${workspace}/${repository}/src/${commitHashOrBranch}/${filePath}",
            HttpMethod.GET,
            noBody(accessToken)
        ).body!!
    }

    override fun checkIsMember(accessToken: String, workspaceId: String, accountId: String) {
        val status = restTemplate.exchange<Void>(
            "/workspaces/${workspaceId}/members/{accountId}",
            HttpMethod.GET,
            noBody(accessToken),
            accountId
        ).statusCode
        if (status != HttpStatus.OK) {
            throw IllegalArgumentException("Not a member")
        }
    }

    fun getUserPrimaryEmail(accessToken: String): String {
        val emails: List<BitbucketUserEmailInfo> = restTemplate.exchange<Values<BitbucketUserEmailInfo>>(
            "/user/emails",
            HttpMethod.GET,
            noBody(accessToken)
        ).body!!.values
        return (emails.firstOrNull { it.isPrimary } ?: emails[0]).email
    }

    private fun noBody(accessToken: String) = HttpEntity(null, authorizationHeader(accessToken))

    private fun authorizationHeader(value: String): HttpHeaders {
        val httpHeaders = HttpHeaders()
        httpHeaders.add("Authorization", "Bearer $value")
        return httpHeaders
    }
}