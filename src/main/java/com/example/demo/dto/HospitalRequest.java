package com.example.demo.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 附近医院查询请求（Story 3.2）
 * - latitude：纬度（-90 到 90）
 * - longitude：经度（-180 到 180）
 * - address：用户地址描述（可选）
 * - department：目标科室（可选，用于关键词过滤）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HospitalRequest {

    @NotNull(message = "纬度不能为空")
    @DecimalMin(value = "-90.0", message = "纬度范围为 -90 到 90")
    @DecimalMax(value = "90.0", message = "纬度范围为 -90 到 90")
    private Double latitude;

    @NotNull(message = "经度不能为空")
    @DecimalMin(value = "-180.0", message = "经度范围为 -180 到 180")
    @DecimalMax(value = "180.0", message = "经度范围为 -180 到 180")
    private Double longitude;

    private String address;

    private String department;
}
