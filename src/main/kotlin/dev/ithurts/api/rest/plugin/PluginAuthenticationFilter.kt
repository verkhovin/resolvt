package dev.ithurts.api.rest.plugin

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import dev.ithurts.api.rest.Error
import dev.ithurts.application.model.TokenType
import dev.ithurts.service.account.AccountRepository
import io.jsonwebtoken.ExpiredJwtException
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class PluginAuthenticationFilter(
    private val pluginTokenManager: PluginTokenManager,
    private val accountRepository: AccountRepository,
) : OncePerRequestFilter() {
    private val objectMapper = jacksonObjectMapper()

    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {
        try {
            val token = request.getHeader("Authorization")?.substring(7)
            if (token == null) {
                filterChain.doFilter(request, response)
                return
            }

            val accountId = pluginTokenManager.validateToken(TokenType.ACCESS, token)
            val account =
                accountRepository.findByIdOrNull(accountId) ?: throw PluginAuthFailedException("Invalid token")
            val authentication = UsernamePasswordAuthenticationToken(account, null, emptyList())
            SecurityContextHolder.getContext().authentication = authentication
            filterChain.doFilter(request, response)
        } catch (e: ExpiredJwtException) {
            response.writeError(Error("token_expired", "Token expired"))
        }
    }

    private fun HttpServletResponse.writeError(error: Error) {
        val errorJson = objectMapper.writeValueAsString(error)
        this.writer.write(errorJson)
        this.status = 401
    }

    companion object {
        val log = LoggerFactory.getLogger(PluginAuthenticationFilter::class.java)
    }

}