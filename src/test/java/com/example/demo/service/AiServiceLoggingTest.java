package com.example.demo.service;

import com.example.demo.dto.AnalysisResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.Deque;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * AiService 日志与失败频率告警测试（Story 5.1）
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AiService 日志与失败频率告警测试")
class AiServiceLoggingTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private EmergencyDetector emergencyDetector;

    @InjectMocks
    private AiService aiService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(aiService, "apiKey", "test-key");
    }

    // ─── 失败频率记录 ───

    @Test
    @DisplayName("单次失败：recentFailures 队列长度为 1")
    void testSingleFailureRecorded() {
        when(emergencyDetector.isEmergency(anyString())).thenReturn(false);
        when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
                .thenThrow(new ResourceAccessException("timeout"));

        aiService.analyze("头痛");

        @SuppressWarnings("unchecked")
        Deque<Long> recentFailures = (Deque<Long>) ReflectionTestUtils.getField(aiService, "recentFailures");
        assertNotNull(recentFailures);
        assertEquals(1, recentFailures.size());
    }

    @Test
    @DisplayName("4次失败：超过阈值（3次），触发频率告警逻辑")
    void testFailureRateExceedsThreshold() {
        when(emergencyDetector.isEmergency(anyString())).thenReturn(false);
        when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
                .thenThrow(new ResourceAccessException("timeout"));

        // 调用4次，触发频率告警
        for (int i = 0; i < 4; i++) {
            AnalysisResponse result = aiService.analyze("头痛");
            assertTrue(result.isFallback());
        }

        @SuppressWarnings("unchecked")
        Deque<Long> recentFailures = (Deque<Long>) ReflectionTestUtils.getField(aiService, "recentFailures");
        assertNotNull(recentFailures);
        assertEquals(4, recentFailures.size());
    }

    @Test
    @DisplayName("成功响应不增加 recentFailures 计数")
    void testSuccessDoesNotRecordFailure() {
        when(emergencyDetector.isEmergency(anyString())).thenReturn(false);
        String mockApiResponse = """
                {
                    "choices": [
                        {
                            "message": {
                                "content": "{\\"department\\": \\"内科\\", \\"reason\\": \\"普通感冒。\\"}"
                            }
                        }
                    ]
                }
                """;
        when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
                .thenReturn(org.springframework.http.ResponseEntity.ok(mockApiResponse));

        aiService.analyze("发烧咳嗽");

        @SuppressWarnings("unchecked")
        Deque<Long> recentFailures = (Deque<Long>) ReflectionTestUtils.getField(aiService, "recentFailures");
        assertNotNull(recentFailures);
        assertEquals(0, recentFailures.size());
    }

    @Test
    @DisplayName("recordFailureAndWarnIfNeeded 直接调用：正确记录失败")
    void testRecordFailureDirectly() {
        aiService.recordFailureAndWarnIfNeeded();
        aiService.recordFailureAndWarnIfNeeded();
        aiService.recordFailureAndWarnIfNeeded();

        @SuppressWarnings("unchecked")
        Deque<Long> recentFailures = (Deque<Long>) ReflectionTestUtils.getField(aiService, "recentFailures");
        assertEquals(3, recentFailures.size());
    }
}
