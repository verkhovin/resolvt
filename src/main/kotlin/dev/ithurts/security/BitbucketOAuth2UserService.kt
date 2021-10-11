package dev.ithurts.security

import dev.ithurts.model.Account
import dev.ithurts.repository.AccountRepository
import dev.ithurts.sourceprovider.bitbucket.BitbucketClient
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service

@Service
class BitbucketOAuth2UserService(
    private val bitbucketClient: BitbucketClient,
    private val accountRepository: AccountRepository
) : DefaultOAuth2UserService() {

    override fun loadUser(userRequest: OAuth2UserRequest): OAuth2User {
        val oAuthUser = super.loadUser(userRequest)

        when (userRequest.clientRegistration.registrationId) {
            "bitbucket" -> processBitbucketSignIn(userRequest, oAuthUser)
        }
        return oAuthUser
    }

    private fun processBitbucketSignIn(userRequest: OAuth2UserRequest, oAuth2User: OAuth2User) {
        val userInfo = BitbucketSourceProviderUserInfo(oAuth2User.attributes)
        val accessToken = userRequest.accessToken.tokenValue
        val email = bitbucketClient.getUserPrimaryEmail(accessToken)

        ensureUser(userInfo, email)
    }

    private fun ensureUser(userInfo: SourceProviderUserInfo, email: String) {
        accountRepository.findByEmail(email)
            ?: accountRepository.save(
                Account(email, userInfo.displayName, userInfo.sourceProvider, userInfo.id)
            )
    }

//    private fun upsertExternalAccount(
//        oAuthUser: BitbucketOAuth2User,
//        accessToken: String,
//        user: User
//    ) = userExternalAccountRepository.findBySourceProviderAndExternalId(BITBUCKET, oAuthUser.uuid!!)
//        ?.also { externalAccount ->
//            externalAccount.accessToken = accessToken
//            externalAccount.refreshToken = "TODO"
//        } ?: UserExternalAccount(
//        BITBUCKET, accessToken, "TODO", oAuthUser.uuid!!, user
//    )

}
