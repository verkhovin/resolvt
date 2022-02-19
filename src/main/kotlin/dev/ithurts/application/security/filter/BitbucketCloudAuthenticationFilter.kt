package dev.ithurts.application.security.filter

import dev.ithurts.domain.workspace.WorkspaceRepository
import io.jsonwebtoken.Jwts
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class BitbucketCloudAuthenticationFilter(
    private val workspaceRepository: WorkspaceRepository
) : OncePerRequestFilter() {
    private val decodingParser = Jwts.parserBuilder().build()

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val isInstall = request.requestURI.startsWith("/bitbucket/install")
        if (isInstall) {
            filterChain.doFilter(request, response)
            return
        }
        val authorizationHeader = request.getHeader("Authorization")
        if ((authorizationHeader == null) || !authorizationHeader.startsWith("JWT ")
        ) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED)
            return
        }
        val jwt = authorizationHeader.substring(4)
        val unsignedJwt = jwt.substringBeforeLast(".") + "."
        val claims = decodingParser.parseClaimsJwt(unsignedJwt)
        val clientKey = claims.body.issuer
        val subjectOrganisation = workspaceRepository.getBySourceProviderApplicationCredentials_ClientKey(clientKey)
        if (subjectOrganisation == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED)
            return
        }
        val verificationParser = Jwts.parserBuilder()
            .setSigningKey(subjectOrganisation.sourceProviderApplicationCredentials.secret.toByteArray()).build()
        verificationParser.parseClaimsJws(jwt)
        // TODO check request hash
        val authentication = UsernamePasswordAuthenticationToken(subjectOrganisation, null, emptyList())
        SecurityContextHolder.getContext().authentication = authentication

        filterChain.doFilter(request, response)
    }
}