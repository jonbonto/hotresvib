package com.hotresvib.infrastructure.config

import com.hotresvib.infrastructure.security.JwtAuthenticationFilter
import com.hotresvib.infrastructure.security.SecurityHeadersFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableMethodSecurity(prePostEnabled = true)
class SecurityConfig(
    private val jwtAuthenticationFilter: JwtAuthenticationFilter,
    private val securityHeadersFilter: SecurityHeadersFilter
) {

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            // Phase 11: Enable CSRF protection for state-changing operations
            .csrf { csrf ->
                csrf.csrfTokenRepository(org.springframework.security.web.csrf.CookieCsrfTokenRepository.withHttpOnlyFalse())
                // Disable CSRF for stateless API endpoints (JWT protected)
                csrf.ignoringRequestMatchers("/api/auth/**", "/api/webhooks/**", "/api/reservations/**")
            }
            .cors { it.configurationSource(corsConfigurationSource()) }
            // Phase 11: Stateless session management
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            // Phase 11: Security headers
            .headers { headers ->
                headers.frameOptions { it.deny() }
                headers.xssProtection()
                headers.contentSecurityPolicy { csp ->
                    csp.policyDirectives("default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline'; img-src 'self' data: https:; font-src 'self' data:;")
                }
                headers.httpStrictTransportSecurity { hsts ->
                    hsts.maxAgeInSeconds(31536000)
                    hsts.includeSubDomains(true)
                }
            }
            .authorizeHttpRequests {
                it.requestMatchers("/actuator/health").permitAll()
                it.requestMatchers("/api/auth/**").permitAll()
                it.requestMatchers("/api/hotels/**").permitAll()
                it.requestMatchers("/api/search/**").permitAll()
                it.requestMatchers("/api/reservations/check-availability").permitAll()
                it.requestMatchers("/api/webhooks/**").permitAll()
                it.requestMatchers("/swagger-ui/**","/swagger-ui.html","/swagger-ui/index.html").permitAll()
                it.requestMatchers("/v3/api-docs/**").permitAll()
                it.anyRequest().authenticated()
            }
            // Phase 11: Add security filters
            .addFilterBefore(securityHeadersFilter, UsernamePasswordAuthenticationFilter::class.java)
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()
        configuration.allowedOrigins = listOf(
            "http://localhost:3000",
            "http://localhost:3001",
            "http://localhost:8080"
        )
        configuration.allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
        configuration.allowedHeaders = listOf("*")
        configuration.allowCredentials = true
        configuration.exposedHeaders = listOf("Authorization", "X-CSRF-TOKEN")
        configuration.maxAge = 3600

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder(12)
}
