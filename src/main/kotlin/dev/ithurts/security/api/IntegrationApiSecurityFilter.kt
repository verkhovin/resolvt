package dev.ithurts.security.api

import dev.ithurts.repository.OrganisationRepository
import io.jsonwebtoken.Jwts
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class IntegrationApiSecurityFilter(
    private val organisationRepository: OrganisationRepository
) : OncePerRequestFilter() {
    private val decodingParser = Jwts.parserBuilder().build()

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val authorizationHeader = request.getHeader("Authorization")
        if ((authorizationHeader == null
                    && !request.requestURI.startsWith("/bitbucket/install"))
            || !authorizationHeader.startsWith("JWT ")
        ) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED)
            return
        }
        val jwt = authorizationHeader.substring(4)
        val unsignedJwt = jwt.substringBeforeLast(".") + "."
        val claims = decodingParser.parseClaimsJwt(unsignedJwt)
        val clientKey = claims.body.issuer
        val subject = organisationRepository.getByClientKey(clientKey)
        if (subject == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED)
            return
        }
        val verificationParser = Jwts.parserBuilder().setSigningKey(subject.secret.toByteArray()).build()
        verificationParser.parseClaimsJws(jwt)
        // TODO check request hash
        val authentication = UsernamePasswordAuthenticationToken(subject, null, emptyList())
        SecurityContextHolder.getContext().authentication = authentication

        filterChain.doFilter(request, response)
    }
}