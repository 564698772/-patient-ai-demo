package com.example.demo;

import com.example.demo.dto.AnalysisResponse;
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

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * AnalysisController MockMvc 集成测试（Story 2.3）
 * 使用 @MockitoBean 替代 Spring Boot 3.x 的 @MockBean
 */
@SpringBootTest
@DisplayName("POST /api/analyze 控制器测试")
class AnalysisControllerTest {

    @MockitoBean
    private AiService aiService;

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    // ─── 正常响应 ───

    @Test
    @DisplayName("POST /api/analyze 正常响应：返回 200 + AnalysisResponse")
    void testAnalyzeSuccess() throws Exception {
        AnalysisResponse mockResponse = AnalysisResponse.builder()
                .department("消化内科")
                .reason("右下腹疼痛可能与阑尾炎有关")
                .isEmergency(false)
                .fallback(false)
                .build();
        when(aiService.analyze(anyString())).thenReturn(mockResponse);

        mockMvc.perform(post("/api/analyze")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"symptoms\": \"肚子右下方很痛，已经两小时了\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.department").value("消化内科"))
                .andExpect(jsonPath("$.reason").value("右下腹疼痛可能与阑尾炎有关"))
                .andExpect(jsonPath("$.isEmergency").value(false))
                .andExpect(jsonPath("$.fallback").value(false));
    }

    // ─── 400：symptoms 为空 ───

    @Test
    @DisplayName("POST /api/analyze：symptoms 为空字符串，返回 400 + INVALID_INPUT")
    void testAnalyzeEmptySymptoms() throws Exception {
        mockMvc.perform(post("/api/analyze")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"symptoms\": \"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"));
    }

    @Test
    @DisplayName("POST /api/analyze：symptoms 为 null，返回 400 + INVALID_INPUT")
    void testAnalyzeNullSymptoms() throws Exception {
        mockMvc.perform(post("/api/analyze")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"symptoms\": null}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"));
    }

    @Test
    @DisplayName("POST /api/analyze：缺少 symptoms 字段，返回 400 + INVALID_INPUT")
    void testAnalyzeMissingSymptoms() throws Exception {
        mockMvc.perform(post("/api/analyze")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"));
    }

    // ─── AI 超时返回 200 + fallback:true ───

    @Test
    @DisplayName("POST /api/analyze：AI 超时，返回 200 + fallback:true")
    void testAnalyzeFallbackOnTimeout() throws Exception {
        when(aiService.analyze(anyString())).thenReturn(AnalysisResponse.fallback());

        mockMvc.perform(post("/api/analyze")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"symptoms\": \"头痛发烧\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fallback").value(true))
                .andExpect(jsonPath("$.fallbackMessage").value("AI分析暂时不可用，请直接拨打120或前往最近医院"));
    }

    // ─── 急症标志 ───

    @Test
    @DisplayName("POST /api/analyze：急症响应，isEmergency=true")
    void testAnalyzeEmergencyResponse() throws Exception {
        AnalysisResponse emergencyResponse = AnalysisResponse.builder()
                .department("急诊科")
                .reason("胸痛疑似心脏病发作，需立即就诊")
                .isEmergency(true)
                .fallback(false)
                .build();
        when(aiService.analyze(anyString())).thenReturn(emergencyResponse);

        mockMvc.perform(post("/api/analyze")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"symptoms\": \"胸痛剧烈，心跳加速\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isEmergency").value(true))
                .andExpect(jsonPath("$.department").value("急诊科"));
    }
}
