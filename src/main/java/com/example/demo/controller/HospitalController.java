package com.example.demo.controller;

import com.example.demo.dto.HospitalDto;
import com.example.demo.dto.HospitalRequest;
import com.example.demo.service.HospitalService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 附近医院查询 REST 端点（Story 3.2）
 * POST /api/hospitals
 * - 400：latitude/longitude 缺失或范围错误
 * - 200：医院列表（可能为空列表，高德异常时）
 */
@Slf4j
@RestController
@RequestMapping("/api")
public class HospitalController {

    @Autowired
    private HospitalService hospitalService;

    /**
     * 查询附近医院
     *
     * @param request 包含经纬度和科室信息的请求体
     * @return 按距离排序的医院列表
     */
    @PostMapping("/hospitals")
    public ResponseEntity<List<HospitalDto>> findNearby(@Valid @RequestBody HospitalRequest request) {
        log.debug("Received hospital search request");
        List<HospitalDto> hospitals = hospitalService.findNearby(
                request.getLatitude(),
                request.getLongitude(),
                request.getDepartment()
        );
        return ResponseEntity.ok(hospitals);
    }
}
