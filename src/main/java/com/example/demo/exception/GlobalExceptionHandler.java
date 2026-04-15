package com.example.demo.exception;

import com.example.demo.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.stream.Collectors;

/**
 * 全局异常处理器（AR6 + Story 5.2 加固）
 *
 * 职责：统一捕获未处理异常，返回结构化 ErrorResponse
 * 注意：外部依赖超时/错误（AI、地图）由 Service 层捕获并返回兜底，不在此处处理
 * 日志：不记录用户敏感数据（症状、坐标等）
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Bean Validation 校验失败（@NotBlank, @NotNull 等）
     * 场景：POST /api/analyze 的 symptoms 为空
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining("; "));
        String message = "入参校验失败：" + fieldErrors;
        log.warn("Validation error: {}", message);
        return ResponseEntity.badRequest().body(ErrorResponse.invalidInput(message));
    }

    /**
     * 参数类型转换错误
     * 场景：Query Parameter 传入非数字字符串
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String message = String.format("参数类型错误：%s 应为 %s，收到 %s",
                ex.getName(),
                ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "未知",
                ex.getValue());
        log.warn("Type mismatch: {}", message);
        return ResponseEntity.badRequest().body(ErrorResponse.invalidInput(message));
    }

    /**
     * 请求体 JSON 格式错误（Story 5.2 新增）
     * 场景：请求体不是有效 JSON，或缺少必要字段
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleMessageNotReadable(HttpMessageNotReadableException ex) {
        String message = "请求体格式错误，请检查 JSON 格式是否正确";
        log.warn("Message not readable: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(ErrorResponse.invalidInput(message));
    }

    /**
     * 404 Not Found（访问不存在的路由）
     * 需要 application.yml 配置：spring.mvc.throw-exception-if-no-handler-found=true
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NoHandlerFoundException ex) {
        String message = String.format("请求路径不存在：%s %s", ex.getHttpMethod(), ex.getRequestURL());
        log.warn("Not found: {}", message);
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.builder()
                        .code("NOT_FOUND")
                        .message(message)
                        .fallback(false)
                        .build());
    }

    /**
     * 所有其他异常（兜底）
     * 记录完整堆栈，但不向用户暴露技术细节
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex) {
        log.error("Unexpected exception", ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.internalError("系统出现问题，请稍后重试"));
    }
}
