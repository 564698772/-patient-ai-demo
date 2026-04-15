package com.example.demo;

import com.example.demo.dto.HospitalDto;
import com.example.demo.service.HospitalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * HospitalController MockMvc 集成测试（Story 3.2）
 */
@SpringBootTest
@DisplayName("POST /api/hospitals 控制器测试")
class HospitalControllerTest {

    @MockitoBean
    private HospitalService hospitalService;

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    // ─── 正常响应 ───

    @Test
    @DisplayName("POST /api/hospitals 正常响应：返回 200 + 医院列表")
    void testFindNearbySuccess() throws Exception {
        List<HospitalDto> mockHospitals = List.of(
                HospitalDto.builder()
                        .name("北京协和医院")
                        .address("北京市东城区王府井大街1号")
                        .distance(500)
                        .departments(List.of("消化内科"))
                        .build(),
                HospitalDto.builder()
                        .name("北京朝阳医院")
                        .address("北京市朝阳区工人体育场南路8号")
                        .distance(1200)
                        .departments(List.of())
                        .build()
        );
        when(hospitalService.findNearby(anyDouble(), anyDouble(), any()))
                .thenReturn(mockHospitals);

        mockMvc.perform(post("/api/hospitals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "latitude": 39.9,
                                    "longitude": 116.4,
                                    "department": "消化内科"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("北京协和医院"))
                .andExpect(jsonPath("$[0].distance").value(500))
                .andExpect(jsonPath("$[1].name").value("北京朝阳医院"));
    }

    // ─── 400：缺少 latitude ───

    @Test
    @DisplayName("POST /api/hospitals：缺少 latitude，返回 400 + INVALID_INPUT")
    void testFindNearbyMissingLatitude() throws Exception {
        mockMvc.perform(post("/api/hospitals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "longitude": 116.4
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"));
    }

    @Test
    @DisplayName("POST /api/hospitals：缺少 longitude，返回 400 + INVALID_INPUT")
    void testFindNearbyMissingLongitude() throws Exception {
        mockMvc.perform(post("/api/hospitals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "latitude": 39.9
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"));
    }

    // ─── 高德异常返回空列表 ───

    @Test
    @DisplayName("POST /api/hospitals：高德 API 异常，返回 200 + 空列表")
    void testFindNearbyEmptyOnApiError() throws Exception {
        when(hospitalService.findNearby(anyDouble(), anyDouble(), any()))
                .thenReturn(List.of());

        mockMvc.perform(post("/api/hospitals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "latitude": 39.9,
                                    "longitude": 116.4
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // ─── department 可选 ───

    @Test
    @DisplayName("POST /api/hospitals：不带 department 字段，正常返回 200")
    void testFindNearbyNoDepartment() throws Exception {
        when(hospitalService.findNearby(anyDouble(), anyDouble(), isNull()))
                .thenReturn(List.of());

        mockMvc.perform(post("/api/hospitals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "latitude": 39.9,
                                    "longitude": 116.4
                                }
                                """))
                .andExpect(status().isOk());
    }
}
