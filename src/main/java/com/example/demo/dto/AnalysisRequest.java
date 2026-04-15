package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI 症状分析请求（Story 2.3）
 * - symptoms：患者症状描述，不能为空
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisRequest {

    @NotBlank(message = "症状描述不能为空")
    private String symptoms;
}
