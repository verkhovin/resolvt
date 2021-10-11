package dev.ithurts.configuration

import org.springframework.security.config.web.servlet.invoke
import dev.ithurts.security.BitbucketOAuth2UserService
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter

@EnableWebSecurity
class SecurityConfiguration(
    private val bitbucketOAuth2UserService: BitbucketOAuth2UserService
) : WebSecurityConfigurerAdapter() {
    override fun configure(http: HttpSecurity) {
        http {
            authorizeRequests {
                authorize(anyRequest, authenticated)
            }
            oauth2Login {
                userInfoEndpoint {
                    userService = bitbucketOAuth2UserService
                }
            }
        }
    }
}

