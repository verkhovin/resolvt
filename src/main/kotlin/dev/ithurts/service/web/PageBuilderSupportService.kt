package dev.ithurts.service.web

import dev.ithurts.model.SourceProvider
import dev.ithurts.model.debt.Debt
import dev.ithurts.security.AuthenticationFacade
import org.springframework.stereotype.Service

@Service
class PageBuilderSupportService(
    private val authenticationFacade: AuthenticationFacade
){
    fun getSourceProviderConnectLink(): String {
        return when (authenticationFacade.account.sourceProvider) {
            SourceProvider.BITBUCKET -> "https://bitbucket.org/site/addons/authorize?addon_key=it-hurts-app"
        }
    }

    fun getSourceProviderSourceLink(debt: Debt): String {
        return when (authenticationFacade.account.sourceProvider) {
            //TODO in general it is not always /src/main
            SourceProvider.BITBUCKET -> "https://bitbucket.org/${debt.repository.organisation.externalId}/${debt.repository.name}/src/${debt.repository.mainBranch}/${debt.filePath}#lines-${debt.startLine}:${debt.endLine}"
        }
    }

    fun getFileName(path: String) = path.substringAfterLast("/")
}