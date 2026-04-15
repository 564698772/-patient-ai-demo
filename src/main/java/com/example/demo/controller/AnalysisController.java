package com.example.demo.controller;

import com.example.demo.dto.AnalysisRequest;
import com.example.demo.dto.AnalysisResponse;
import com.example.demo.service.AiService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * AI 症状分析 REST 端点（Story 2.3）
 * POST /api/analyze
 * - 400：symptoms 为空（Bean Validation → GlobalExceptionHandler）
 * - 200：正常响应或 fallback:true（AI 超时时）
 */
@Slf4j
@RestController
@RequestMapping("/api")
public class AnalysisController {

    @Autowired
    private AiService aiService;

    /**
     * 分析患者症状
     *
     * @param request 包含 symptoms 字段的请求体
     * @return AnalysisResponse（正常分析结果，或 AI 超时时的兜底响应）
     */
    @PostMapping("/analyze")
    public ResponseEntity<AnalysisResponse> analyze(@Valid @RequestBody AnalysisRequest request) {
        log.debug("Received analysis request");
        AnalysisResponse response = aiService.analyze(request.getSymptoms());
        return ResponseEntity.ok(response);
    }
}
