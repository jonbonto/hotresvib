package com.hotresvib.infrastructure.config

import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

/**
 * Adds high-priority redirects for common swagger-ui URLs so they fire
 * before the static resource handler attempts to serve files from the
 * webjar.
 */
@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE)
class SwaggerUiRedirectConfig : WebMvcConfigurer {
    override fun addViewControllers(registry: ViewControllerRegistry) {
        // redirect old path that some clients/bookmarks still use
        registry.addRedirectViewController("/swagger-ui.html", "/swagger-ui/index.html")
        // redirect trailing slash variant
        registry.addRedirectViewController("/swagger-ui/", "/swagger-ui/index.html")
    }
}
