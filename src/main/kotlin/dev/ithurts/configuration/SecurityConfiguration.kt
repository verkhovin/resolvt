package dev.ithurts.configuration

import org.springframework.security.config.web.servlet.invoke
import dev.ithurts.security.AccountPersistingOAuth2UserService
import dev.ithurts.security.OrganisationPermissionEvaluator
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.builders.WebSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.web.access.expression.DefaultWebSecurityExpressionHandler

@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
class SecurityConfiguration(
    private val accountPersistingOAuth2UserService: AccountPersistingOAuth2UserService,
    private val organisationPermissionEvaluator: OrganisationPermissionEvaluator
) : WebSecurityConfigurerAdapter() {
    override fun configure(http: HttpSecurity) {
        http {
            authorizeRequests {
                authorize(anyRequest, authenticated)
            }
            oauth2Login {
                userInfoEndpoint {
                    userService = accountPersistingOAuth2UserService
                }
            }
        }
    }

    override fun configure(web: WebSecurity) {
        web.expressionHandler(
            DefaultWebSecurityExpressionHandler().also { it.setPermissionEvaluator(organisationPermissionEvaluator) }
        )
    }
}

