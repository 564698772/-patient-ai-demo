package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 医院信息 DTO（Story 3.1）
 * - name：医院名称
 * - distance：距离（米）
 * - address：医院地址
 * - departments：支持的科室列表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HospitalDto {

    private String name;
    private Integer distance;
    private String address;
    private List<String> departments;
}
