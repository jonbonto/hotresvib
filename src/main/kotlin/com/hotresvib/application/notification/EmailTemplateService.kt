package com.hotresvib.application.notification

import org.springframework.stereotype.Service
import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context
import org.slf4j.LoggerFactory

/**
 * Service for rendering Thymeleaf email templates
 */
@Service
class EmailTemplateService(private val templateEngine: TemplateEngine) {

    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Render email template with provided context variables
     *
     * @param templateName Thymeleaf template name (e.g., "email/booking-confirmation")
     * @param context Map of template variables
     * @return Rendered HTML content
     */
    fun renderTemplate(templateName: String, context: Map<String, Any>): String {
        return try {
            val thymeleafContext = Context().apply {
                setVariables(context)
            }
            templateEngine.process(templateName, thymeleafContext)
        } catch (e: Exception) {
            logger.error("Error rendering email template: $templateName", e)
            throw IllegalArgumentException("Failed to render template: $templateName", e)
        }
    }

    /**
     * Render template with default values and locale settings
     */
    fun renderTemplateWithDefaults(templateName: String, variables: Map<String, Any>): String {
        val context = variables.toMutableMap().apply {
            putIfAbsent("appName", "HotResvib")
            putIfAbsent("supportEmail", "support@hotresvib.com")
            putIfAbsent("year", java.time.Year.now().value)
        }
        return renderTemplate(templateName, context)
    }
}
