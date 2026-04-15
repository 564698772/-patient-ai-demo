package com.example.demo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * CORS 预检请求测试（WebApplicationContext + MockMvcBuilders + 测试端点）
 * 验证 CorsConfig.java 对 OPTIONS 预检请求的处理（AC #1, #2, #3）
 */
@SpringBootTest
@DisplayName("CORS 预检请求测试")
class CorsPreflightMockMvcTest {

    /** 仅用于 CORS 测试的桩端点，不进入生产代码 */
    @TestConfiguration
    static class TestEndpointConfig {
        @Bean
        public PingController pingController() {
            return new PingController();
        }
    }

    @RestController
    static class PingController {
        @GetMapping("/test/ping")
        public String ping() {
            return "pong";
        }
    }

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(wac)
                .dispatchOptions(true)
                .build();
    }

    /**
     * AC-1 / AC-3：来自 localhost:5173 的预检请求应返回 CORS 许可头，且不使用 "*"
     */
    @Test
    @DisplayName("OPTIONS 预检：来自 localhost:5173 应返回 CORS 许可头")
    void testPreflightAllowsLocalhost() throws Exception {
        mockMvc.perform(options("/test/ping")
                        .header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", "GET")
                        .header("Access-Control-Request-Headers", "Content-Type"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Access-Control-Allow-Origin"))
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"))
                .andExpect(header().exists("Access-Control-Allow-Methods"));
    }

    /**
     * AC-2：来自 example.com 的预检请求不应包含 CORS 许可头
     */
    @Test
    @DisplayName("OPTIONS 预检：来自 example.com 不应返回匹配的 CORS 许可头")
    void testPreflightBlocksExternalDomain() throws Exception {
        mockMvc.perform(options("/test/ping")
                        .header("Origin", "https://example.com")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(result -> {
                    String allowOrigin = result.getResponse()
                            .getHeader("Access-Control-Allow-Origin");
                    if (allowOrigin != null) {
                        assert !allowOrigin.contains("example.com")
                                : "不应该允许 example.com，但得到：" + allowOrigin;
                    }
                });
    }
}
