package dev.ithurts.configuration

import dev.ithurts.service.account.AccountRepository
import dev.ithurts.service.workspace.WorkspaceRepository
import dev.ithurts.api.web.oauth2.AccountPersistingOAuth2UserService
import dev.ithurts.service.permission.WorkspacePermissionEvaluator
import dev.ithurts.api.rest.bitbucket.BitbucketCloudAuthenticationFilter
import dev.ithurts.api.rest.plugin.PluginAuthenticationFilter
import dev.ithurts.api.rest.plugin.PluginTokenManager
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


/**
 * Security config for Web MVC based access. Authentication is occurred via OAuth2 using Source Provider.
 * Authentication principal is [dev.ithurts.security.oauth2.AuthenticatedOAuth2User]
 */
@EnableWebSecurity
@Order(3)
class WebSecurityConfiguration(
    private val accountPersistingOAuth2UserService: AccountPersistingOAuth2UserService,
) : WebSecurityConfigurerAdapter() {
    override fun configure(http: HttpSecurity) {
        http {
            headers {
                cacheControl {
                    disable()
                }
            }
            authorizeRequests {
                authorize("/error", permitAll)
                authorize("/", permitAll)
                authorize("/public/**", permitAll)
                authorize("/favicon.ico", permitAll)
                authorize("/connect/bitbucket/descriptor", permitAll)
                authorize("/actuator/**", permitAll)
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

@EnableWebSecurity
@Order(2)
class IntegrationApiSecurityConfiguration(
    private val workspaceRepository: WorkspaceRepository,
    private val applicationProperties: ApplicationProperties
) : WebSecurityConfigurerAdapter() {
    override fun configure(http: HttpSecurity?) {
        http {
            securityMatcher(AntPathRequestMatcher("/bitbucket/**"))
            csrf {
                disable()
            }
            authorizeRequests {
                authorize(anyRequest, permitAll)
            }
            sessionManagement {
                sessionCreationPolicy = SessionCreationPolicy.STATELESS
            }
            addFilterBefore<UsernamePasswordAuthenticationFilter>(
                BitbucketCloudAuthenticationFilter(workspaceRepository, applicationProperties)
            )
            exceptionHandling {

            }

        }
    }
}

@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
class MethodSecurityConfig(private val workspacePermissionEvaluator: WorkspacePermissionEvaluator) :
    GlobalMethodSecurityConfiguration() {
    override fun createExpressionHandler(): MethodSecurityExpressionHandler {
        val expressionHandler = DefaultMethodSecurityExpressionHandler()
        expressionHandler.setPermissionEvaluator(workspacePermissionEvaluator)
        return expressionHandler
    }
}
