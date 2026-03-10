package com.hotresvib.application.web

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@WebMvcTest(SwaggerConfigController::class)
class SwaggerConfigControllerTest @Autowired constructor(
    private val mockMvc: MockMvc
) {

    @Test
    fun `swagger config endpoint returns minimal config`() {
        mockMvc.get("/v3/api-docs/swagger-config") {
            accept = org.springframework.http.MediaType.APPLICATION_JSON
        }
            .andExpect {
                status { isOk() }
                content { contentType(org.springframework.http.MediaType.APPLICATION_JSON) }
                jsonPath("$.url") { value("/v3/api-docs") }
            }
    }
}
