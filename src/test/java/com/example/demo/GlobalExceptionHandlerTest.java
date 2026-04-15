package com.example.demo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * GlobalExceptionHandler 集成测试（WebApplicationContext + MockMvcBuilders）
 * 验证统一错误响应格式（AC #5）
 */
@SpringBootTest
@DisplayName("全局异常处理器测试")
class GlobalExceptionHandlerTest {

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    /**
     * AC-5：访问不存在路由，返回 404 + 结构化错误（code: NOT_FOUND, fallback: false）
     */
    @Test
    @DisplayName("访问不存在路由应返回 404 + { code: NOT_FOUND, fallback: false }")
    void testNotFoundReturnsStructuredError() throws Exception {
        mockMvc.perform(get("/api/not-exist-route"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.fallback").value(false));
    }
}
