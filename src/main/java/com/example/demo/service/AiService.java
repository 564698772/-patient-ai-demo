package com.example.demo.service;

import com.example.demo.dto.AnalysisResponse;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * AI 症状分析服务（Story 2.2 + Story 5.1）
 * 调用通义千问 DashScope OpenAI 兼容端点
 * 超时/异常时返回兜底响应，服务不崩溃
 * Story 5.1：添加调用耗时日志 + 失败频率告警（>3次/分钟 → WARN）
 */
@Slf4j
@Service
public class AiService {

    private static final String DASHSCOPE_URL =
            "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions";

    /** 失败频率告警阈值：1分钟内失败超过此次数则记录 WARN */
    private static final int FAILURE_WARN_THRESHOLD = 3;
    private static final long ONE_MINUTE_MS = 60_000L;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private EmergencyDetector emergencyDetector;

    @Value("${dashscope.api-key}")
    private String apiKey;

    private final ObjectMapper objectMapper = JsonMapper.builder().build();

    /** 最近失败时间戳队列（用于频率统计） */
    private final Deque<Long> recentFailures = new LinkedList<>();

    /**
     * 分析患者症状，返回科室推荐结果
     * Story 5.1：记录调用耗时和结果
     *
     * @param symptoms 患者症状描述
     * @return AnalysisResponse（正常或兜底）
     */
    public AnalysisResponse analyze(String symptoms) {
        boolean isEmergency = emergencyDetector.isEmergency(symptoms);
        long startTime = System.currentTimeMillis();

        try {
            String aiContent = callDashScope(symptoms);
            JsonNode parsed = objectMapper.readTree(aiContent);
            long elapsed = System.currentTimeMillis() - startTime;

            log.info("AI analysis succeeded in {}ms, department={}", elapsed,
                    parsed.path("department").asString("unknown"));

            return AnalysisResponse.builder()
                    .department(parsed.path("department").asString())
                    .reason(parsed.path("reason").asString())
                    .isEmergency(isEmergency)
                    .fallback(false)
                    .fallbackMessage(null)
                    .build();
        } catch (ResourceAccessException e) {
            long elapsed = System.currentTimeMillis() - startTime;
            log.warn("AI timeout calling DashScope after {}ms: {}", elapsed, e.getMessage());
            recordFailureAndWarnIfNeeded();
            return AnalysisResponse.fallback();
        } catch (Exception e) {
            long elapsed = System.currentTimeMillis() - startTime;
            log.warn("AI error calling DashScope after {}ms: {}", elapsed, e.getMessage());
            recordFailureAndWarnIfNeeded();
            return AnalysisResponse.fallback();
        }
    }

    /**
     * 记录失败并检查频率告警
     * Story 5.1：若1分钟内失败 >3次，记录 WARN 告警
     */
    void recordFailureAndWarnIfNeeded() {
        long now = System.currentTimeMillis();
        recentFailures.addLast(now);

        // 清除1分钟前的记录
        while (!recentFailures.isEmpty() && now - recentFailures.peekFirst() > ONE_MINUTE_MS) {
            recentFailures.pollFirst();
        }

        if (recentFailures.size() > FAILURE_WARN_THRESHOLD) {
            log.warn("AI service failure rate alert: {} failures in the last minute (threshold: {})",
                    recentFailures.size(), FAILURE_WARN_THRESHOLD);
        }
    }

    /**
     * 调用 DashScope API，返回 AI 生成的 JSON 字符串
     */
    private String callDashScope(String symptoms) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);

        String systemPrompt = "你是一个医疗分诊助手。用户描述症状后，你必须返回严格JSON格式（不要有多余文字）：" +
                "{\"department\": \"推荐科室\", \"reason\": \"推荐理由（50字以内）\"}";

        Map<String, Object> body = Map.of(
                "model", "qwen-plus",
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", symptoms)
                )
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(DASHSCOPE_URL, request, String.class);

        JsonNode root = objectMapper.readTree(response.getBody());
        return root.path("choices").get(0).path("message").path("content").asString();
    }
}
