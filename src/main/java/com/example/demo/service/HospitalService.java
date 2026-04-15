package com.example.demo.service;

import com.example.demo.dto.HospitalDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 高德地图周边医院查询服务（Story 3.1）
 * 调用高德地图 Place Around API 查询附近医院
 * 按距离排序返回结果；异常时返回空列表 + WARN 日志
 */
@Slf4j
@Service
public class HospitalService {

    private static final String AMAP_AROUND_URL = "https://restapi.amap.com/v3/place/around";
    // 高德地图医院类型代码
    private static final String HOSPITAL_TYPE_CODE = "090100";

    @Autowired
    private RestTemplate restTemplate;

    @Value("${amap.api-key}")
    private String apiKey;

    @Value("${amap.radius:5000}")
    private int radius;

    private final ObjectMapper objectMapper = JsonMapper.builder().build();

    /**
     * 查询附近医院
     *
     * @param lat        纬度
     * @param lon        经度
     * @param department 目标科室（用于关键词过滤）
     * @return 按距离排序的医院列表，异常时返回空列表
     */
    public List<HospitalDto> findNearby(double lat, double lon, String department) {
        long startTime = System.currentTimeMillis();
        try {
            String url = buildUrl(lat, lon, department);
            String responseBody = restTemplate.getForObject(url, String.class);

            if (responseBody == null) {
                log.warn("HospitalService: AMap returned null response for lat={}, lon={}", lat, lon);
                return List.of();
            }

            log.debug("HospitalService: AMap raw response: {}", responseBody);
            List<HospitalDto> result = parseResponse(responseBody);
            long elapsed = System.currentTimeMillis() - startTime;
            log.info("HospitalService: AMap query succeeded in {}ms, found {} hospitals", elapsed, result.size());
            return result;
        } catch (Exception e) {
            long elapsed = System.currentTimeMillis() - startTime;
            log.warn("HospitalService: Failed to query AMap API after {}ms: {}", elapsed, e.getMessage());
            return List.of();
        }
    }

    /**
     * 构建高德周边搜索 URL
     */
    private String buildUrl(double lat, double lon, String department) {
        log.debug("HospitalService: searching near lat={}, lon={}, department={}, radius={}m", lat, lon, department, radius);

        return UriComponentsBuilder.fromUriString(AMAP_AROUND_URL)
                .queryParam("key", apiKey)
                .queryParam("location", lon + "," + lat)
                .queryParam("keywords", "医院")
                .queryParam("types", HOSPITAL_TYPE_CODE)
                .queryParam("radius", radius)
                .queryParam("sortrule", "distance")
                .queryParam("output", "json")
                .queryParam("offset", 10)
                .queryParam("page", 1)
                .toUriString();
    }

    /**
     * 解析高德 API 响应，提取医院列表
     */
    private List<HospitalDto> parseResponse(String responseBody) throws Exception {
        JsonNode root = objectMapper.readTree(responseBody);

        String status = root.path("status").asText();
        if (!"1".equals(status)) {
            String info = root.path("info").asText();
            log.warn("HospitalService: AMap API error - status={}, info={}", status, info);
            return List.of();
        }

        JsonNode pois = root.path("pois");
        List<HospitalDto> hospitals = new ArrayList<>();

        if (pois.isArray()) {
            for (JsonNode poi : pois) {
                HospitalDto hospital = parsePoi(poi);
                if (hospital != null) {
                    hospitals.add(hospital);
                }
            }
        }

        // 按距离排序（高德已按距离返回，但防御性排序）
        hospitals.sort(Comparator.comparingInt(h -> (h.getDistance() != null ? h.getDistance() : Integer.MAX_VALUE)));

        return hospitals;
    }

    /**
     * 解析单个 POI 节点为 HospitalDto
     */
    private HospitalDto parsePoi(JsonNode poi) {
        try {
            String name = poi.path("name").asText();
            String address = poi.path("address").asText();
            int distance = poi.path("distance").asInt(0);

            // 从 biz_ext 中提取科室信息（如有）
            List<String> departments = new ArrayList<>();
            JsonNode bizExt = poi.path("biz_ext");
            if (!bizExt.isMissingNode() && bizExt.has("tag")) {
                String tag = bizExt.path("tag").asText();
                if (!tag.isBlank()) {
                    departments.add(tag);
                }
            }

            return HospitalDto.builder()
                    .name(name)
                    .address(address)
                    .distance(distance)
                    .departments(departments)
                    .build();
        } catch (Exception e) {
            log.warn("HospitalService: Failed to parse POI: {}", e.getMessage());
            return null;
        }
    }
}
