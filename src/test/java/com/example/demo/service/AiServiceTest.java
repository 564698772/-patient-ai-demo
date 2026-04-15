package com.example.demo.service;

import com.example.demo.dto.AnalysisResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * AiService 单元测试（Story 2.2）
 * 使用 Mockito mock RestTemplate 和 EmergencyDetector
 * 通过 ReflectionTestUtils 注入 apiKey（@Value 在 Mockito 环境下不自动注入）
 */
@ExtendWith(MockitoExtension.class)
class AiServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private EmergencyDetector emergencyDetector;

    @InjectMocks
    private AiService aiService;

    @BeforeEach
    void setUp() {
        // @Value("${dashscope.api-key}") 在 Mockito 环境下不自动注入，需手动设置
        ReflectionTestUtils.setField(aiService, "apiKey", "test-key");
    }

    // ─── 正常响应解析测试 ───

    @Test
    void testSuccessfulResponseParsing() {
        // mock EmergencyDetector 返回 false（普通症状）
        when(emergencyDetector.isEmergency(anyString())).thenReturn(false);

        // mock RestTemplate 返回有效 DashScope 响应
        String mockApiResponse = """
                {
                    "choices": [
                        {
                            "message": {
                                "content": "{\\"department\\": \\"消化内科\\", \\"reason\\": \\"右下腹疼痛可能与阑尾炎有关，建议就诊消化内科或外科进一步检查。\\"}"
                            }
                        }
                    ]
                }
                """;
        when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
                .thenReturn(ResponseEntity.ok(mockApiResponse));

        AnalysisResponse result = aiService.analyze("肚子右下方很痛，已经两小时了");

        assertNotNull(result);
        assertFalse(result.isFallback());
        assertEquals("消化内科", result.getDepartment());
        assertEquals("右下腹疼痛可能与阑尾炎有关，建议就诊消化内科或外科进一步检查。", result.getReason());
        assertFalse(result.isEmergency());
    }

    // ─── 超时兜底测试 ───

    @Test
    void testTimeoutReturnsFallback() {
        when(emergencyDetector.isEmergency(anyString())).thenReturn(false);
        when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
                .thenThrow(new ResourceAccessException("Read timed out"));

        AnalysisResponse result = aiService.analyze("头痛");

        assertTrue(result.isFallback());
        assertEquals("AI分析暂时不可用，请直接拨打120或前往最近医院", result.getFallbackMessage());
        assertFalse(result.isEmergency());
        assertNull(result.getDepartment());
        assertNull(result.getReason());
    }

    // ─── API 错误兜底测试 ───

    @Test
    void testApiErrorReturnsFallback() {
        when(emergencyDetector.isEmergency(anyString())).thenReturn(false);
        when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
                .thenThrow(new RuntimeException("API error"));

        AnalysisResponse result = aiService.analyze("头痛");

        assertTrue(result.isFallback());
        assertNotNull(result.getFallbackMessage());
    }

    // ─── isEmergency 标志注入测试（急症+超时）───

    @Test
    void testEmergencyFlagWhenApiTimesOut() {
        // 急症场景 + API 超时 → 兜底响应（isEmergency=false，安全默认值）
        when(emergencyDetector.isEmergency(anyString())).thenReturn(true);
        when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
                .thenThrow(new ResourceAccessException("timeout"));

        AnalysisResponse result = aiService.analyze("胸痛剧烈");

        assertTrue(result.isFallback());
        // fallback() 方法固定返回 isEmergency=false（安全默认值）
        assertFalse(result.isEmergency());
    }

    // ─── 正常响应中急症标志注入测试 ───

    @Test
    void testEmergencyFlagInjectedInSuccessResponse() {
        // 急症关键词 + API 正常响应 → isEmergency 应为 true
        when(emergencyDetector.isEmergency(anyString())).thenReturn(true);

        String mockApiResponse = """
                {
                    "choices": [
                        {
                            "message": {
                                "content": "{\\"department\\": \\"急诊科\\", \\"reason\\": \\"胸痛可能为心脏疾病，建议立即就诊急诊科。\\"}"
                            }
                        }
                    ]
                }
                """;
        when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
                .thenReturn(ResponseEntity.ok(mockApiResponse));

        AnalysisResponse result = aiService.analyze("胸痛剧烈，心跳加速");

        assertFalse(result.isFallback());
        assertTrue(result.isEmergency());
        assertEquals("急诊科", result.getDepartment());
    }

    // ─── EmergencyDetector 被正确调用测试 ───

    @Test
    void testEmergencyDetectorIsCalledWithSymptoms() {
        String symptoms = "腹痛发烧";
        when(emergencyDetector.isEmergency(symptoms)).thenReturn(false);
        when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
                .thenThrow(new RuntimeException("network error"));

        aiService.analyze(symptoms);

        // 验证 EmergencyDetector 被调用了
        verify(emergencyDetector, times(1)).isEmergency(symptoms);
    }
}
