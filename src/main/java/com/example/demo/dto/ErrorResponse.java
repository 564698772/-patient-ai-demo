package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一错误响应格式（AR8）
 * - code：错误代码（机器读取），如 INVALID_INPUT, NOT_FOUND, INTERNAL_ERROR
 * - message：错误消息（人类可读，面向用户）
 * - fallback：是否为降级响应（外部依赖故障时为 true）
 * - fallbackMessage：降级时的用户提示（可选）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private String code;
    private String message;
    private Boolean fallback;
    private String fallbackMessage;

    public static ErrorResponse invalidInput(String message) {
        return ErrorResponse.builder()
                .code("INVALID_INPUT")
                .message(message)
                .fallback(false)
                .build();
    }

    public static ErrorResponse internalError(String message) {
        return ErrorResponse.builder()
                .code("INTERNAL_ERROR")
                .message(message)
                .fallback(false)
                .build();
    }

    public static ErrorResponse fallback(String code, String message, String fallbackMessage) {
        return ErrorResponse.builder()
                .code(code)
                .message(message)
                .fallback(true)
                .fallbackMessage(fallbackMessage)
                .build();
    }
}
