package dev.ithurts.security.api

import dev.ithurts.exception.PluginAuthFailedException
import dev.ithurts.model.api.TokenType
import dev.ithurts.repository.AccountRepository
import dev.ithurts.service.PluginTokenManager
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import javax.servlet.Filter
import javax.servlet.FilterChain
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest

class PluginAuthenticationFilter(
    private val pluginTokenManager: PluginTokenManager,
    private val accountRepository: AccountRepository,
) : Filter {
    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        request as HttpServletRequest
        val token = request.getHeader("Authorization")?.substring(7)
        if (token == null) {
            chain.doFilter(request, response)
            return
        }

        val accountId = pluginTokenManager.validateToken(TokenType.ACCESS, token)
        val account = accountRepository.findByIdOrNull(accountId) ?: throw PluginAuthFailedException("Invalid token")
        val authentication = UsernamePasswordAuthenticationToken(account, null, emptyList())
        SecurityContextHolder.getContext().authentication = authentication
        chain.doFilter(request, response)
    }
}