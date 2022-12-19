package dev.resolvt.service.sourceprovider

import dev.resolvt.service.sourceprovider.bitbucket.BitbucketAuthenticationProvider
import dev.resolvt.service.sourceprovider.bitbucket.BitbucketClient
import dev.resolvt.service.sourceprovider.model.SourceProviderRepository
import dev.resolvt.service.account.Account
import dev.resolvt.service.SourceProvider
import dev.resolvt.service.SourceProvider.*
import dev.resolvt.service.sourceprovider.github.GithubAppAuthentication
import dev.resolvt.service.sourceprovider.github.GithubAuthenticationProvider
import dev.resolvt.service.sourceprovider.github.GithubClient
import dev.resolvt.service.workspace.Workspace
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.stereotype.Service

@Service
class SourceProviderCommunicationService(
    private val bitbucketClient: BitbucketClient?,
    private val githubClient: GithubClient?,
    private val bitbucketAuthenticationProvider: BitbucketAuthenticationProvider?,
    private val githubAuthenticationProvider: GithubAuthenticationProvider?,
) {

    fun getDiff(workspaceExternalId: String, repository: String, spec: String): String {
        return client.getDiff(getAccessToken(), workspaceExternalId, repository, spec)
    }

    fun getFile(workspace: Workspace, repository: String, filePath: String, commitHash: String): String {
        return client.getFile(getAccessToken(workspace), workspace.externalId, repository, filePath, commitHash)
    }

    fun getRepository(workspace: Workspace, repository: String): SourceProviderRepository {
        return client.getRepository(getAccessToken(workspace), workspace.externalId, repository)
    }

    fun checkIsMember(workspace: Workspace, account: Account) {
        client.checkIsMember(getAccessToken(workspace), workspace, account)
    }

    fun getAccountPrimaryEmail(accessToken: String, sourceProvider: SourceProvider): String {
        return getClient(sourceProvider).getUserPrimaryEmail(accessToken)
    }

    private val client: SourceProviderClient
        get() = getClient(getCurrentSourceProvider())

    private fun getClient(sourceProvider: SourceProvider) = when (sourceProvider) {
        BITBUCKET -> bitbucketClient ?: throw SourceProviderIntegrationDisabledException(BITBUCKET)
        GITHUB -> githubClient ?: throw SourceProviderIntegrationDisabledException(GITHUB)
    }

    private fun getAccessToken(): String = authenticationProvider.getAuthentication()

    private fun getAccessToken(workspace: Workspace) = authenticationProvider.getAuthentication(workspace)

    private val authenticationProvider: SourceProviderAuthenticationProvider
        get() = when(getCurrentSourceProvider()) {
            BITBUCKET -> bitbucketAuthenticationProvider ?: throw SourceProviderIntegrationDisabledException(BITBUCKET)
            GITHUB -> githubAuthenticationProvider ?: throw SourceProviderIntegrationDisabledException(GITHUB)
        }

    private fun getCurrentSourceProvider(): SourceProvider {
        val authentication = SecurityContextHolder.getContext().authentication
        return when {
            authentication is OAuth2AuthenticationToken -> SourceProvider.valueOf(authentication.authorizedClientRegistrationId.uppercase())
            authentication.principal is Workspace -> (authentication.principal as Workspace).sourceProvider
            authentication.principal is Account -> (authentication.principal as Account).sourceProvider
            authentication.principal is GithubAppAuthentication -> GITHUB
            else -> throw IllegalStateException("Unknown authentication type")
        }
    }
}
