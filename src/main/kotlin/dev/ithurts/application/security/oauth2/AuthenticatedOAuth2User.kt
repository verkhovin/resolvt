package dev.ithurts.application.security.oauth2

import dev.ithurts.domain.account.Account
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.oauth2.core.user.OAuth2User


class AuthenticatedOAuth2User(val account: Account, private val delegate: OAuth2User): OAuth2User {
    val accountId = account.identity!!

    override fun getName(): String = delegate.name

    override fun getAttributes(): MutableMap<String, Any> = delegate.attributes

    override fun getAuthorities(): MutableCollection<out GrantedAuthority> = delegate.authorities
}