package com.example.demo.service;

import com.example.demo.dto.HospitalDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * HospitalService 单元测试（Story 3.1）
 * 使用 Mockito mock RestTemplate
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("HospitalService 单元测试")
class HospitalServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private HospitalService hospitalService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(hospitalService, "apiKey", "test-amap-key");
        ReflectionTestUtils.setField(hospitalService, "radius", 5000);
    }

    // ─── 正常响应解析 ───

    @Test
    @DisplayName("findNearby 正常响应：返回按距离排序的医院列表")
    void testFindNearbySuccess() {
        String mockResponse = """
                {
                    "status": "1",
                    "info": "OK",
                    "count": "2",
                    "pois": [
                        {
                            "name": "北京协和医院",
                            "address": "北京市东城区王府井大街1号",
                            "distance": "500",
                            "biz_ext": {}
                        },
                        {
                            "name": "北京朝阳医院",
                            "address": "北京市朝阳区工人体育场南路8号",
                            "distance": "1200",
                            "biz_ext": {}
                        }
                    ]
                }
                """;
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(mockResponse);

        List<HospitalDto> result = hospitalService.findNearby(39.9, 116.4, "消化内科");

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("北京协和医院", result.get(0).getName());
        assertEquals(500, result.get(0).getDistance());
        assertEquals("北京朝阳医院", result.get(1).getName());
        assertEquals(1200, result.get(1).getDistance());
    }

    // ─── API 返回错误状态 ───

    @Test
    @DisplayName("findNearby API 返回 status!=1：返回空列表")
    void testFindNearbyApiError() {
        String mockResponse = """
                {
                    "status": "0",
                    "info": "INVALID_USER_KEY",
                    "pois": []
                }
                """;
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(mockResponse);

        List<HospitalDto> result = hospitalService.findNearby(39.9, 116.4, "外科");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ─── 网络异常 ───

    @Test
    @DisplayName("findNearby 网络超时：返回空列表，不抛出异常")
    void testFindNearbyNetworkTimeout() {
        when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenThrow(new ResourceAccessException("Connection timed out"));

        List<HospitalDto> result = hospitalService.findNearby(39.9, 116.4, "急诊科");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("findNearby 通用异常：返回空列表，不崩溃")
    void testFindNearbyGeneralException() {
        when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenThrow(new RuntimeException("Unknown error"));

        List<HospitalDto> result = hospitalService.findNearby(39.9, 116.4, "内科");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ─── null 响应 ───

    @Test
    @DisplayName("findNearby API 返回 null：返回空列表")
    void testFindNearbyNullResponse() {
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(null);

        List<HospitalDto> result = hospitalService.findNearby(39.9, 116.4, "内科");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ─── 空 pois 数组 ───

    @Test
    @DisplayName("findNearby pois 为空数组：返回空列表")
    void testFindNearbyEmptyPois() {
        String mockResponse = """
                {
                    "status": "1",
                    "info": "OK",
                    "count": "0",
                    "pois": []
                }
                """;
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(mockResponse);

        List<HospitalDto> result = hospitalService.findNearby(39.9, 116.4, "皮肤科");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ─── 按距离排序验证 ───

    @Test
    @DisplayName("findNearby 结果按距离升序排序")
    void testFindNearbyDistanceSorting() {
        String mockResponse = """
                {
                    "status": "1",
                    "info": "OK",
                    "pois": [
                        {
                            "name": "远处医院",
                            "address": "远处地址",
                            "distance": "2000",
                            "biz_ext": {}
                        },
                        {
                            "name": "近处医院",
                            "address": "近处地址",
                            "distance": "300",
                            "biz_ext": {}
                        }
                    ]
                }
                """;
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(mockResponse);

        List<HospitalDto> result = hospitalService.findNearby(39.9, 116.4, "骨科");

        assertEquals(2, result.size());
        // 近处医院应该排第一
        assertEquals("近处医院", result.get(0).getName());
        assertEquals(300, result.get(0).getDistance());
        assertEquals("远处医院", result.get(1).getName());
    }

    // ─── department 参数处理 ───

    @Test
    @DisplayName("findNearby department 为 null：不抛出异常，正常请求")
    void testFindNearbyNullDepartment() {
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn("""
                {"status": "1", "info": "OK", "pois": []}
                """);

        assertDoesNotThrow(() -> hospitalService.findNearby(39.9, 116.4, null));
    }
}
