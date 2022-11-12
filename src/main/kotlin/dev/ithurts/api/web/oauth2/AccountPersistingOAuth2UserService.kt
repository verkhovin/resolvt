package dev.ithurts.api.web.oauth2

import dev.ithurts.service.account.Account
import dev.ithurts.service.account.AccountRepository
import dev.ithurts.service.sourceprovider.SourceProviderCommunicationService
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service

@Service
class AccountPersistingOAuth2UserService(
    private val sourceProviderCommunicationService: SourceProviderCommunicationService,
    private val accountRepository: AccountRepository
) : DefaultOAuth2UserService() {

    override fun loadUser(userRequest: OAuth2UserRequest): OAuth2User {
        val oAuthUser = super.loadUser(userRequest)

        val account = when (userRequest.clientRegistration.registrationId) {
            "bitbucket" -> processSignIn(userRequest, BitbucketSourceProviderUserInfo(oAuthUser.attributes))
            "github" -> processSignIn(userRequest, GitHubSourceProviderUserInfo(oAuthUser.attributes))
            else -> throw Exception("Unknown provider")
        }
        return AuthenticatedOAuth2User(account, oAuthUser)
    }

    private fun processSignIn(userRequest: OAuth2UserRequest, userInfo: SourceProviderUserInfo): Account {
        val accessToken = userRequest.accessToken.tokenValue
        val email = sourceProviderCommunicationService.getAccountPrimaryEmail(accessToken, userInfo.sourceProvider)
        return ensureUser(userInfo, email)
    }

    private fun ensureUser(userInfo: SourceProviderUserInfo, email: String): Account =
        accountRepository.findByExternalIdAndSourceProvider(userInfo.id, userInfo.sourceProvider)
            ?: accountRepository.save(
                Account(email, userInfo.displayName, userInfo.sourceProvider, userInfo.id)
            )
}
