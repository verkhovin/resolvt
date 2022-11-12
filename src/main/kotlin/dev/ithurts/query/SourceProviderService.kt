package dev.ithurts.query

import dev.ithurts.service.permission.AuthenticationFacade
import dev.ithurts.configuration.ApplicationProperties
import dev.ithurts.service.SourceProvider
import dev.ithurts.service.debt.model.Binding
import org.springframework.stereotype.Service

@Service
class SourceProviderService(
    private val authenticationFacade: AuthenticationFacade,
    private val applicationProperties: ApplicationProperties
) {
    fun getSourceProviderConnectLink(): String {
        return when (authenticationFacade.account.sourceProvider) {
            SourceProvider.BITBUCKET -> "https://bitbucket.org/site/addons/authorize?addon_key=${applicationProperties.bitbucket.appName}"
            SourceProvider.GITHUB -> "https://github.com/apps/${applicationProperties.github.appName}/installations/new"
        }
    }

    fun getSourceUrl(
        binding: Binding,
        repositoryName: String,
        mainBranch: String,
        workspaceExternalId: String
    ): String {
        return when (authenticationFacade.account.sourceProvider) {
            SourceProvider.BITBUCKET -> "https://bitbucket.org/$workspaceExternalId/$repositoryName/src/$mainBranch" +
                    "/${binding.filePath}#lines-${binding.startLine}:${binding.endLine}"
            SourceProvider.GITHUB -> "https://github.com/$workspaceExternalId/$repositoryName/blob/$mainBranch" +
                    "/${binding.filePath}#L${binding.startLine}-L${binding.endLine}"
        }
    }

    fun getCommitUrl(repositoryName: String, commitHash: String, workspaceExternalId: String): String {
        return when (authenticationFacade.account.sourceProvider) {
            SourceProvider.BITBUCKET -> "https://bitbucket.org/$workspaceExternalId/$repositoryName/commits/$commitHash"
            SourceProvider.GITHUB -> "https://github.com/$workspaceExternalId/$repositoryName/commit/$commitHash"
        }
    }
}