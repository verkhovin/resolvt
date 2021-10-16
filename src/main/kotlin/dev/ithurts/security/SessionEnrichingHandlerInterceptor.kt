package dev.ithurts.security

import dev.ithurts.service.web.SessionManager
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.servlet.ModelAndView
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
class SessionEnrichingHandlerInterceptor(
    private val sessionManager: SessionManager
) : HandlerInterceptor {
    override fun postHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
        model: ModelAndView?
    ) {
        request.getSession(false)?.let { session ->
            sessionManager.setAccountSessionProperties(session)
        }
    }
}