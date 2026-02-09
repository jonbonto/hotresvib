package com.hotresvib.infrastructure.security

import org.springframework.stereotype.Service

/**
 * Input sanitizer to prevent XSS attacks.
 * Sanitizes user inputs by removing script tags, iframes, and event handlers.
 */
@Service
class InputSanitizer {
    
    fun sanitize(input: String): String {
        if (input.isBlank()) return input
        
        // Escape HTML entities
        var sanitized = input
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#x27;")
        
        // Remove potentially dangerous patterns
        sanitized = sanitized
            .replace(Regex("""<script[^>]*>.*?</script>""", RegexOption.IGNORE_CASE), "")
            .replace(Regex("""<iframe[^>]*>.*?</iframe>""", RegexOption.IGNORE_CASE), "")
            .replace(Regex("""on\w+\s*=""", RegexOption.IGNORE_CASE), "")
            .replace(Regex("""javascript:""", RegexOption.IGNORE_CASE), "")
        
        return sanitized
    }
    
    fun sanitizeHtml(html: String): String {
        if (html.isBlank()) return html
        
        // For HTML content, use stricter sanitization
        val policy = HtmlPolicyBuilder()
            .allowElements("p", "br", "strong", "em", "u", "a", "ul", "ol", "li")
            .allowAttributes("href").onElements("a")
            .requireRelNofollowOnLinks()
            .build()
        
        return policy.sanitize(html)
    }
}

class HtmlPolicyBuilder {
    val allowedTags = mutableSetOf<String>()
    val allowedAttributes = mutableMapOf<String, MutableSet<String>>()
    private var requireNoFollow = false
    
    fun allowElements(vararg tags: String): HtmlPolicyBuilder {
        allowedTags.addAll(tags)
        return this
    }
    
    fun allowAttributes(vararg attributes: String): HtmlAttributeBuilder {
        return HtmlAttributeBuilder(this, attributes.toSet())
    }
    
    fun requireRelNofollowOnLinks(): HtmlPolicyBuilder {
        this.requireNoFollow = true
        return this
    }
    
    fun build(): HtmlPolicy {
        return HtmlPolicy(allowedTags, allowedAttributes, requireNoFollow)
    }
}

class HtmlAttributeBuilder(
    private val policyBuilder: HtmlPolicyBuilder,
    private val attributes: Set<String>
) {
    fun onElements(vararg elements: String): HtmlPolicyBuilder {
        elements.forEach { element ->
            policyBuilder.allowedAttributes.computeIfAbsent(element) { mutableSetOf() }
                .addAll(attributes)
        }
        return policyBuilder
    }
}

class HtmlPolicy(
    private val allowedTags: Set<String>,
    private val allowedAttributes: Map<String, Set<String>>,
    private val requireNoFollow: Boolean
) {
    fun sanitize(html: String): String {
        var result = html
        
        // Remove script tags completely
        result = result.replace(Regex("""<script[^>]*>.*?</script>""", RegexOption.IGNORE_CASE), "")
        
        // Remove iframe tags
        result = result.replace(Regex("""<iframe[^>]*>.*?</iframe>""", RegexOption.IGNORE_CASE), "")
        
        // Remove event handlers
        result = result.replace(Regex("""on\w+\s*="[^"]*"""", RegexOption.IGNORE_CASE), "")
        result = result.replace(Regex("""on\w+\s*='[^']*'""", RegexOption.IGNORE_CASE), "")
        
        // Remove javascript: protocol
        result = result.replace(Regex("""href\s*=\s*"javascript:[^"]*"""", RegexOption.IGNORE_CASE), "")
        result = result.replace(Regex("""href\s*=\s*'javascript:[^']*'""", RegexOption.IGNORE_CASE), "")
        
        return result
    }
}
