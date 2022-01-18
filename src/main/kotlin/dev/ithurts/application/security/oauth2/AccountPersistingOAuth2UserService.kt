package dev.ithurts.application.security.oauth2

import dev.ithurts.domain.account.Account
import dev.ithurts.domain.account.AccountRepository
import dev.ithurts.application.sourceprovider.bitbucket.BitbucketClient
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service

@Service
class AccountPersistingOAuth2UserService(
    private val bitbucketClient: BitbucketClient,
    private val accountRepository: AccountRepository
) : DefaultOAuth2UserService() {

    override fun loadUser(userRequest: OAuth2UserRequest): OAuth2User {
        val oAuthUser = super.loadUser(userRequest)

        val account = when (userRequest.clientRegistration.registrationId) {
            "bitbucket" -> processBitbucketSignIn(userRequest, oAuthUser)
            else -> throw Exception("Unknown provider")
        }
        return AuthenticatedOAuth2User(account, oAuthUser)
    }

    private fun processBitbucketSignIn(userRequest: OAuth2UserRequest, oAuth2User: OAuth2User): Account {
        val userInfo = BitbucketSourceProviderUserInfo(oAuth2User.attributes)
        val accessToken = userRequest.accessToken.tokenValue
        val email = bitbucketClient.getUserPrimaryEmail(accessToken)

        return ensureUser(userInfo, email)
    }

    private fun ensureUser(userInfo: SourceProviderUserInfo, email: String): Account =
        accountRepository.findByEmail(email)
            ?: accountRepository.save(
                Account(email, userInfo.displayName, userInfo.sourceProvider, userInfo.id)
            )
}
