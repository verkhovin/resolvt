package dev.ithurts.configuration

import dev.ithurts.repository.AccountRepository
import dev.ithurts.security.AccountPersistingOAuth2UserService
import dev.ithurts.security.OrganisationPermissionEvaluator
import dev.ithurts.security.api.PluginAuthenticationFilter
import dev.ithurts.service.PluginTokenManager
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.config.web.servlet.invoke
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.security.web.util.matcher.AntPathRequestMatcher

@EnableWebSecurity
@Order(2)
class WebSecurityConfiguration(
    private val accountPersistingOAuth2UserService: AccountPersistingOAuth2UserService,
) : WebSecurityConfigurerAdapter() {
    override fun configure(http: HttpSecurity) {
        http {
            authorizeRequests {
                authorize("/error", permitAll)
                authorize(anyRequest, authenticated)
            }
            oauth2Login {
                userInfoEndpoint {
                    userService = accountPersistingOAuth2UserService
                }
            }
        }
    }
}

@EnableWebSecurity
@Order(1)
class ApiSecurityConfiguration(
    private val pluginTokenManager: PluginTokenManager,
    private val accountRepository: AccountRepository,
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
            exceptionHandling {

            }

        }
    }
}

@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
class MethodSecurityConfig(private val organisationPermissionEvaluator: OrganisationPermissionEvaluator) :
    GlobalMethodSecurityConfiguration() {
    override fun createExpressionHandler(): MethodSecurityExpressionHandler {
        val expressionHandler = DefaultMethodSecurityExpressionHandler()
        expressionHandler.setPermissionEvaluator(organisationPermissionEvaluator)
        return expressionHandler
    }
}
