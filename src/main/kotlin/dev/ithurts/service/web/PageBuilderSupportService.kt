package dev.ithurts.service.web

import dev.ithurts.model.SourceProvider
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
}