package com.hotresvib.infrastructure.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

/**
 * Security headers filter to add protective HTTP headers to all responses.
 * Prevents various security attacks like clickjacking, MIME type sniffing, and XSS.
 */
@Component
class SecurityHeadersFilter : OncePerRequestFilter() {
    
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        // Prevent MIME type sniffing
        response.addHeader("X-Content-Type-Options", "nosniff")
        
        // Prevent clickjacking
        response.addHeader("X-Frame-Options", "DENY")
        
        // Legacy XSS protection
        response.addHeader("X-XSS-Protection", "1; mode=block")
        
        // Content Security Policy
        response.addHeader(
            "Content-Security-Policy",
            "default-src 'self'; script-src 'self' 'unsafe-inline' 'unsafe-eval'; style-src 'self' 'unsafe-inline'; img-src 'self' data: https:; font-src 'self' data:; connect-src 'self' https:; frame-ancestors 'none';"
        )
        
        // HSTS - Strict-Transport-Security
        response.addHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains")
        
        // Referrer Policy
        response.addHeader("Referrer-Policy", "strict-origin-when-cross-origin")
        
        // Permissions Policy (formerly Feature-Policy)
        response.addHeader(
            "Permissions-Policy",
            "geolocation=(), microphone=(), camera=(), payment=()"
        )
        
        // Remove Server header to not expose technology stack
        response.setHeader("Server", "")
        
        filterChain.doFilter(request, response)
    }
}
