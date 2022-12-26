package dev.resolvt.service.sourceprovider.github

import dev.resolvt.configuration.Github
import dev.resolvt.service.account.Account
import dev.resolvt.service.sourceprovider.SourceProviderClient
import dev.resolvt.service.sourceprovider.github.model.GithubRepositoryInfo
import dev.resolvt.service.sourceprovider.github.model.GithubUser
import dev.resolvt.service.sourceprovider.github.model.GithubUserEmailInfo
import dev.resolvt.service.sourceprovider.model.SourceProviderRepository
import dev.resolvt.service.workspace.Workspace
import dev.resolvt.service.workspace.WorkspaceType
import dev.resolvt.util.noBody
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.exchange

@Service
@Github
class GithubClient(
    @Qualifier("githubRestTemplate") private val restTemplate: RestTemplate
): SourceProviderClient {
    override val organisationOwnerRole: String
        get() = throw NotImplementedError("Not applicable")

    override fun getDiff(accessToken: String, workspaceExternalId: String, repository: String, spec: String): String {
        return restTemplate.exchange<String>(
            "/repos/$workspaceExternalId/$repository/compare/$spec",
            HttpMethod.GET,
            noBody(accessToken, mapOf("Accept" to "application/vnd.github.diff"))
        ).body!!
    }

    override fun getRepository(
        accessToken: String,
        workspaceExternalId: String,
        repository: String,
    ): SourceProviderRepository {
        val githubRepository = restTemplate.exchange<GithubRepositoryInfo>(
            "/repos/$workspaceExternalId/$repository",
            HttpMethod.GET,
            noBody(accessToken)
        ).body!!
        return SourceProviderRepository(githubRepository.name, githubRepository.defaultBranch)
    }

    override fun getFile(
        accessToken: String,
        workspaceExternalId: String,
        repository: String,
        filePath: String,
        commitHashOrBranch: String,
    ): String {
        return restTemplate.exchange<String>(
            "/repos/$workspaceExternalId/$repository/contents/$filePath",
            HttpMethod.GET,
            noBody(accessToken, mapOf("Accept" to "application/vnd.github.VERSION.raw"))
        ).body!!
    }

    override fun checkIsMember(accessToken: String, workspace: Workspace, account: Account) {
        when(workspace.type) {
            WorkspaceType.ORGANISATION -> {
                val status = restTemplate.exchange<Void>(
                    "/orgs/${workspace.name}/members/${account.externalId}",
                    HttpMethod.GET,
                    noBody(accessToken, mapOf("Accept" to "application/vnd.github+json"))
                ).statusCode
                if (status != HttpStatus.NO_CONTENT) {
                    throw IllegalArgumentException("Not a member")
                }
            }
            else -> {
                val sameAccount = restTemplate.exchange<GithubUser>(
                    "/users/${account.externalId}",
                    HttpMethod.GET,
                    noBody(accessToken, mapOf("Accept" to "application/vnd.github+json"))
                ).body!!.login == workspace.externalId
                if(!sameAccount) {
                    throw IllegalArgumentException("Not a member")
                }
            }
        }
    }

    override fun getUserPrimaryEmail(accessToken: String): String {
        val emails: List<GithubUserEmailInfo> = restTemplate.exchange<List<GithubUserEmailInfo>>(
            "/user/emails",
            HttpMethod.GET,
            noBody(accessToken, mapOf("Accept" to "application/vnd.github+json"))
        ).body!!
        return (emails.firstOrNull { it.primary } ?: emails[0]).email
    }
}