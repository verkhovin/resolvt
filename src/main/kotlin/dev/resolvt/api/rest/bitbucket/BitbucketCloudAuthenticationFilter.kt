package dev.resolvt.api.rest.bitbucket

import dev.resolvt.configuration.ApplicationProperties
import dev.resolvt.service.SourceProvider
import dev.resolvt.service.sourceprovider.bitbucket.buildQueryStringHash
import dev.resolvt.service.workspace.WorkspaceRepository
import io.jsonwebtoken.Jwts
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI
import java.net.URLEncoder
import java.nio.charset.Charset
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class BitbucketCloudAuthenticationFilter(
    private val workspaceRepository: WorkspaceRepository,
    private val applicationProperties: ApplicationProperties,
) : OncePerRequestFilter() {
    private val decodingParser = Jwts.parserBuilder().build()

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        authenticate(request)
        filterChain.doFilter(request, response)
    }

    private fun authenticate(request: HttpServletRequest) {
        val authorizationHeader = request.getHeader("Authorization")
        if ((authorizationHeader == null) || !authorizationHeader.startsWith("JWT ")
        ) {
            return
        }
        val jwt = authorizationHeader.substring(4)
        val unsignedJwt = jwt.substringBeforeLast(".") + "."
        val claims = decodingParser.parseClaimsJwt(unsignedJwt)
        val clientKey = claims.body.issuer
        val subjectOrganisation =
            workspaceRepository.getBySourceProviderAndSourceProviderApplicationCredentials_ClientKey(SourceProvider.BITBUCKET,
                clientKey) ?: return
        val verificationParser = Jwts.parserBuilder()
            .setSigningKey(subjectOrganisation.sourceProviderApplicationCredentials.secret!!.toByteArray()).build()
        try {
            verificationParser.parseClaimsJws(jwt)
        } catch (e: Exception) {
            logger.warn("Failed to verify token, proceeding without authentication")
            return
        }
        val qsh = buildQueryStringHash(buildCanonicalUrl(request))
        if (qsh != claims.body["qsh"]) {
            return
        }
        val authentication = UsernamePasswordAuthenticationToken(subjectOrganisation, null, emptyList())
        SecurityContextHolder.getContext().authentication = authentication
    }

    private fun buildCanonicalUrl(request: HttpServletRequest): String {
        val url = request.requestURL.toString()
        val uri = if (url.startsWith(applicationProperties.baseUrl)) {
            url.substringAfter(applicationProperties.baseUrl)
        } else request.requestURI
        val components = UriComponentsBuilder.fromUri(URI(uri)).build()
        val pathSegments = components.pathSegments.map {
            URLEncoder.encode(it, Charset.forName("UTF-8"))
        }.joinToString("/")
        val queryParams = components.queryParams
            .filter { it.key.lowercase() != "jwt" }
            .map {
                URLEncoder.encode(it.key, Charset.forName("UTF-8")) to
                        it.value.sorted()
                            .joinToString(",") { value -> URLEncoder.encode(value, Charset.forName("UTF-8")) }
            }.joinToString("&") { "${it.first}=${it.second}" }
        return "${request.method}&/${pathSegments}&${queryParams}"
    }

    companion object {}
}