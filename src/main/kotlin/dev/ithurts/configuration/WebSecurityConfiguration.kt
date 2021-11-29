package dev.ithurts.configuration

import dev.ithurts.repository.AccountRepository
import org.springframework.security.config.web.servlet.invoke
import dev.ithurts.security.AccountPersistingOAuth2UserService
import dev.ithurts.security.OrganisationPermissionEvaluator
import dev.ithurts.security.api.PluginAuthenticationFilter
import dev.ithurts.service.PluginTokenManager
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.builders.WebSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.access.expression.DefaultWebSecurityExpressionHandler
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.security.web.util.matcher.AntPathRequestMatcher

@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@Order(2)
class WebSecurityConfiguration(
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

@Configuration
@Order(1)
class ApiSecurityConfiguration(
    private val pluginTokenManager: PluginTokenManager,
    private val accountRepository: AccountRepository
) : WebSecurityConfigurerAdapter() {
    override fun configure(http: HttpSecurity?) {
        http {
            securityMatcher(AntPathRequestMatcher("/api/**"))
            csrf {
                disable()
            }
            authorizeRequests {
                authorize("/api/auth/**", permitAll)
                authorize("/api/**", authenticated)
            }
            sessionManagement {
                sessionCreationPolicy = SessionCreationPolicy.STATELESS
            }
            addFilterBefore<UsernamePasswordAuthenticationFilter>(
                PluginAuthenticationFilter(
                    pluginTokenManager,
                    accountRepository
                )
            )

        }
    }

}