package com.hotresvib.infrastructure.config

import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Bean
import org.thymeleaf.spring6.SpringTemplateEngine
import org.thymeleaf.spring6.view.ThymeleafViewResolver
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver

/**
 * Configuration for Thymeleaf template engine
 */
@Configuration
class ThymeleafConfig {

    /**
     * Configure email template resolver
     */
    @Bean
    fun emailTemplateResolver(): ClassLoaderTemplateResolver {
        val resolver = ClassLoaderTemplateResolver()
        resolver.prefix = "templates/"
        resolver.suffix = ".html"
        resolver.setTemplateMode(org.thymeleaf.templatemode.TemplateMode.HTML)
        resolver.characterEncoding = "UTF-8"
        resolver.isCacheable = true
        return resolver
    }

    /**
     * Configure Thymeleaf template engine
     */
    @Bean
    fun templateEngine(emailTemplateResolver: ClassLoaderTemplateResolver): SpringTemplateEngine {
        val engine = SpringTemplateEngine()
        engine.addTemplateResolver(emailTemplateResolver)
        engine.enableSpringELCompiler = true
        return engine
    }
}
