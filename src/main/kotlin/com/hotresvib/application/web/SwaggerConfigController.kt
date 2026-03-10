package com.hotresvib.application.web

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Fallback endpoint used by Swagger UI to obtain its configuration when the
 * Springdoc built-in handler is unavailable or misconfigured.  There have been
 * occasional issues with the auto-registered `/v3/api-docs/swagger-config`
 * mapping not being recognized, causing the UI to fail with a 500.  This
 * controller ensures a minimal configuration is always returned so the UI can
 * initialize.
 */
@RestController
class SwaggerConfigController {

    @GetMapping("/v3/api-docs/swagger-config")
    fun swaggerConfig(): Map<String, Any> {
        // the only required property is "url" pointing at the API docs
        return mapOf(
            "url" to "/v3/api-docs",
            // Swagger UI will merge additional parameters such as
            // "validatorUrl" or "configUrl" itself if it wants them
        )
    }
}
