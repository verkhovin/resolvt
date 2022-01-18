package dev.ithurts.application.service

import dev.ithurts.domain.SourceProvider
import dev.ithurts.domain.debt.Debt
import dev.ithurts.application.security.AuthenticationFacade
import dev.ithurts.domain.repository.Repository
import org.springframework.stereotype.Service

@Service
class SourceProviderService(
    private val authenticationFacade: AuthenticationFacade
){
    fun getSourceProviderConnectLink(): String {
        return when (authenticationFacade.account.sourceProvider) {
            SourceProvider.BITBUCKET -> "https://bitbucket.org/site/addons/authorize?addon_key=it-hurts-app"
        }
    }

    fun getSourceUrl(debt: Debt, repositoryName: String, mainBranch: String, workspaceExternalId: String): String {
        return when (authenticationFacade.account.sourceProvider) {
            SourceProvider.BITBUCKET -> "https://bitbucket.org/${workspaceExternalId}/${repositoryName}/src/${mainBranch}/${debt.filePath}#lines-${debt.startLine}:${debt.endLine}"
        }
    }
}