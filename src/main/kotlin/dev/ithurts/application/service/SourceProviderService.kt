package dev.ithurts.application.service

import dev.ithurts.application.security.AuthenticationFacade
import dev.ithurts.configuration.ApplicationProperties
import dev.ithurts.domain.SourceProvider
import dev.ithurts.domain.debt.Binding
import org.springframework.stereotype.Service

@Service
class SourceProviderService(
    private val authenticationFacade: AuthenticationFacade,
    private val applicationProperties: ApplicationProperties
) {
    fun getSourceProviderConnectLink(): String {
        return when (authenticationFacade.account.sourceProvider) {
            SourceProvider.BITBUCKET -> "https://bitbucket.org/site/addons/authorize?addon_key=${applicationProperties.bitbucket.appName}"
        }
    }

    fun getSourceUrl(
        binding: Binding,
        repositoryName: String,
        mainBranch: String,
        workspaceExternalId: String
    ): String {
        return when (authenticationFacade.account.sourceProvider) {
            SourceProvider.BITBUCKET -> "https://bitbucket.org/${workspaceExternalId}/${repositoryName}/src/${mainBranch}/" +
                    "${binding.filePath}#lines-${binding.startLine}:${binding.endLine}"
        }
    }

    fun getCommitUrl(repositoryName: String, commitHash: String, workspaceExternalId: String): String {
        return when (authenticationFacade.account.sourceProvider) {
            SourceProvider.BITBUCKET -> "https://bitbucket.org/${workspaceExternalId}/${repositoryName}/commits/${commitHash}"
        }
    }
}