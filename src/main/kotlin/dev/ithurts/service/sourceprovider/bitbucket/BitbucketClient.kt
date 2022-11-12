package dev.ithurts.service.sourceprovider.bitbucket

import dev.ithurts.configuration.Bitbucket
import dev.ithurts.service.account.Account
import dev.ithurts.service.sourceprovider.SourceProviderClient
import dev.ithurts.service.sourceprovider.bitbucket.model.BitbucketRepository
import dev.ithurts.service.sourceprovider.bitbucket.model.BitbucketUserEmailInfo
import dev.ithurts.service.sourceprovider.bitbucket.model.Values
import dev.ithurts.service.sourceprovider.model.SourceProviderRepository
import dev.ithurts.service.workspace.Workspace
import dev.ithurts.util.noBody
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.client.RestOperations
import org.springframework.web.client.exchange

@Service
@Bitbucket
class BitbucketClient(
    @Qualifier("bitbucketRestTemplate") val restTemplate: RestOperations
) : SourceProviderClient {
    override val organisationOwnerRole: String = "owner"

    override fun getDiff(accessToken: String, workspaceExternalId: String, repository: String, spec: String): String {
        return restTemplate.exchange(
            "/repositories/${workspaceExternalId}/${repository}/diff/${spec}?merge=false",
            HttpMethod.GET,
            noBody(accessToken),
            String::class.java
        ).body ?: ""
    }

    override fun getRepository(
        accessToken: String,
        workspaceExternalId: String,
        repository: String
    ): SourceProviderRepository {
        val bitbucketRepository = restTemplate.exchange<BitbucketRepository>(
            "/repositories/${workspaceExternalId}/${repository}",
            HttpMethod.GET,
            noBody(accessToken)
        ).body!!
        return SourceProviderRepository(bitbucketRepository.name, bitbucketRepository.mainbranch.name)
    }

    override fun getFile(
        accessToken: String,
        workspaceExternalId: String,
        repository: String,
        filePath: String,
        commitHashOrBranch: String
    ): String {
        return restTemplate.exchange<String>(
            "/repositories/${workspaceExternalId}/${repository}/src/${commitHashOrBranch}/${filePath}",
            HttpMethod.GET,
            noBody(accessToken)
        ).body!!
    }

    override fun checkIsMember(accessToken: String, workspace: Workspace, account: Account) {
        val status = restTemplate.exchange<Void>(
            "/workspaces/${workspace.externalId}/members/{accountID}",
            HttpMethod.GET,
            noBody(accessToken),
            account.externalId
        ).statusCode
        if (status != HttpStatus.OK) {
            throw IllegalArgumentException("Not a member")
        }
    }

    override fun getUserPrimaryEmail(accessToken: String): String {
        val emails: List<BitbucketUserEmailInfo> = restTemplate.exchange<Values<BitbucketUserEmailInfo>>(
            "/user/emails",
            HttpMethod.GET,
            noBody(accessToken)
        ).body!!.values
        return (emails.firstOrNull { it.isPrimary } ?: emails[0]).email
    }
}