package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI 症状分析响应（Story 2.2）
 * - department：推荐就诊科室
 * - reason：推荐理由（≤50字）
 * - isEmergency：是否为急症（由 EmergencyDetector 注入）
 * - fallback：是否为兜底响应（AI 超时/异常时为 true）
 * - fallbackMessage：兜底提示语
 *
 * ⚠️ 使用 @JsonProperty("isEmergency") 避免 Lombok 对 boolean 字段生成 isIsEmergency() 导致的序列化问题
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisResponse {

    private String department;
    private String reason;

    @JsonProperty("isEmergency")
    private boolean isEmergency;

    private boolean fallback;
    private String fallbackMessage;

    /**
     * 兜底响应工厂方法（AI 超时或异常时返回）
     */
    public static AnalysisResponse fallback() {
        return AnalysisResponse.builder()
                .department(null)
                .reason(null)
                .isEmergency(false)
                .fallback(true)
                .fallbackMessage("AI分析暂时不可用，请直接拨打120或前往最近医院")
                .build();
    }
}
