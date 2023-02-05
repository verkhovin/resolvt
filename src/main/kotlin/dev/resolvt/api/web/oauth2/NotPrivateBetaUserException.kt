package dev.resolvt.api.web.oauth2

import org.springframework.security.oauth2.core.OAuth2AuthenticationException
import org.springframework.security.oauth2.core.OAuth2Error
import org.springframework.security.oauth2.core.OAuth2ErrorCodes.ACCESS_DENIED

class NotPrivateBetaUserException : OAuth2AuthenticationException(OAuth2Error(ACCESS_DENIED,  "User is not found in the list of private beta users", "/login"))