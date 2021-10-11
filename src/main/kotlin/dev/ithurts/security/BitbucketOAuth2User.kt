package dev.ithurts.security

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.oauth2.core.user.OAuth2User


class BitbucketOAuth2User: OAuth2User {
    private val authorities = AuthorityUtils.createAuthorityList("ROLE_USER")
    private var attributes: MutableMap<String, Any>? = null

    var uuid: String? = null
    var displayName: String? = null

    override fun getName(): String {
        return displayName!!
    }

    override fun getAttributes(): MutableMap<String, Any>? {
        if (this.attributes == null) {
            this.attributes = mutableMapOf<String, Any>(
                "id" to uuid!!,
                "displayName" to displayName!!
            )
        }
        return this.attributes
    }

    override fun getAuthorities(): MutableCollection<out GrantedAuthority> {
        return authorities
    }

}