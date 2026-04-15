package com.example.demo;

import com.example.demo.service.AiService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * GlobalExceptionHandler 加固测试（Story 5.2）
 * 验证所有边界异常情况均有统一处理
 */
@SpringBootTest
@DisplayName("全局异常处理加固测试（Story 5.2）")
class GlobalExceptionHandlerEnhancedTest {

    @MockitoBean
    private AiService aiService;

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    // ─── JSON 格式错误（Story 5.2 新增）───

    @Test
    @DisplayName("请求体为无效 JSON：返回 400 + INVALID_INPUT")
    void testInvalidJsonBody() throws Exception {
        mockMvc.perform(post("/api/analyze")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("not-valid-json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"));
    }

    @Test
    @DisplayName("请求体为空（无 Content-Type）：返回 400")
    void testEmptyBody() throws Exception {
        mockMvc.perform(post("/api/analyze")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"));
    }

    // ─── 已有处理（回归验证）───

    @Test
    @DisplayName("404：访问不存在路由，返回 NOT_FOUND")
    void testNotFoundRoute() throws Exception {
        mockMvc.perform(post("/api/nonexistent")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    @DisplayName("医院请求中 latitude 超出范围：返回 400 + INVALID_INPUT")
    void testHospitalLatitudeOutOfRange() throws Exception {
        mockMvc.perform(post("/api/hospitals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"latitude\": 91.0, \"longitude\": 116.4}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"));
    }
}
