package dev.ithurts.application.internal.sourceprovider

import dev.ithurts.domain.account.Account
import dev.ithurts.domain.SourceProvider
import dev.ithurts.domain.SourceProvider.*
import dev.ithurts.domain.workspace.Workspace
import dev.ithurts.application.internal.sourceprovider.bitbucket.BitbucketAuthorizationProvider
import dev.ithurts.application.internal.sourceprovider.bitbucket.BitbucketClient
import dev.ithurts.application.internal.sourceprovider.model.SourceProviderRepository
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.stereotype.Service

@Service
class SourceProviderCommunicationService(
    private val bitbucketClient: BitbucketClient,
    private val bitbucketAuthorizationProvider: BitbucketAuthorizationProvider
) {

    fun getDiff(workspaceId: String, repository: String, spec: String): String {
        return client.getDiff(getAccessToken(), workspaceId, repository, spec)
    }

    fun getFile(workspaceId: String, repository: String, filePath: String, commitHash: String): String {
        return client.getFile(getAccessToken(), workspaceId, repository, filePath, commitHash)
    }

    fun getRepository(workspace: Workspace, repository: String): SourceProviderRepository {
        return client.getRepository(getAccessToken(workspace), workspace.externalId, repository)
    }

    private val client: SourceProviderClient
        get() = when (getCurrentSourceProvider()) {
            BITBUCKET -> bitbucketClient
        }

    private fun getAccessToken(): String = when (getCurrentSourceProvider()) {
        BITBUCKET -> bitbucketAuthorizationProvider.getAuthorization()
    }

    private fun getAccessToken(workspace: Workspace) = when (getCurrentSourceProvider()) {
        BITBUCKET -> bitbucketAuthorizationProvider.getAuthorization(workspace)
    }

    private fun getCurrentSourceProvider(): SourceProvider {
        val authentication = SecurityContextHolder.getContext().authentication
        return when {
            authentication is OAuth2AuthenticationToken -> SourceProvider.valueOf(authentication.authorizedClientRegistrationId.uppercase())
            authentication.principal is Workspace -> (authentication.principal as Workspace).sourceProvider
            authentication.principal is Account -> (authentication.principal as Account).sourceProvider
            else -> throw IllegalStateException("Unknown authentication type")
        }
    }
}
