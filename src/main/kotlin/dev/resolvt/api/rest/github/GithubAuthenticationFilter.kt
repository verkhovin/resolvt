package dev.resolvt.api.rest.github

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dev.resolvt.configuration.ApplicationProperties
import dev.resolvt.service.SourceProvider
import dev.resolvt.service.sourceprovider.github.GithubAppAuthentication
import dev.resolvt.service.sourceprovider.github.model.GithubWebhookEvent
import dev.resolvt.service.workspace.WorkspaceRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


class GithubAuthenticationFilter(
    private val workspaceRepository: WorkspaceRepository,
    private val applicationProperties: ApplicationProperties,
    private val objectMapper: ObjectMapper
): OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val requestWrapper = BodyCachingRequestWrapper(request)
        authenticate(requestWrapper)
        filterChain.doFilter(requestWrapper, response)
    }

    private fun authenticate(request: HttpServletRequest) {
        val appId = request.getHeader("x-github-hook-installation-target-id") ?: return
        if (appId != applicationProperties.github!!.appId) return
        val signature = request.getHeader("x-hub-signature-256") ?: return
        log.debug("Found x-hub-signature-256 header")
        val body = request.inputStream.readAllBytes()
        val digest = try {
            sha256Mac(body, applicationProperties.github.webhookSecret)
        } catch (e: Exception) {
            log.warn("Failed to calculate sha256Mac for request from github")
            return
        }

        if (digest == signature) {
            log.debug("Signature verified")
            try {
                val githubWebhookEvent: GithubWebhookEvent = objectMapper.readValue(body)
                val authentication: UsernamePasswordAuthenticationToken =
                    githubWebhookEvent.installation?.let { installation ->
                        val installationId = installation.id
                        val workspace =
                            workspaceRepository.getBySourceProviderAndSourceProviderApplicationCredentials_ClientKey(
                                SourceProvider.GITHUB,
                                installationId
                            )
                        if (workspace != null) {
                            UsernamePasswordAuthenticationToken(workspace, null, emptyList())
                        } else {
                            null
                        }
                    } ?: UsernamePasswordAuthenticationToken(GithubAppAuthentication(appId.toInt()), null, emptyList())
                SecurityContextHolder.getContext().authentication = authentication
            } catch (e: Exception) {
                log.warn("Possibly non-github request")
            }
        }
    }

    private fun sha256Mac(data: ByteArray, key: String): String {
        val algorithm = "HmacSHA256"
        val secretKeySpec = SecretKeySpec(key.toByteArray(), algorithm)
        val mac: Mac = Mac.getInstance(algorithm)
        mac.init(secretKeySpec)
        return "sha256=" + mac.doFinal(data).toHexString()
    }

    private fun ByteArray.toHexString() = this.joinToString(separator = "") { eachByte -> "%02x".format(eachByte) }

    companion object {
        val log: Logger = LoggerFactory.getLogger(GithubAuthenticationFilter::class.java)
    }
}