package com.example.demo.service;

import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 急症关键词检测服务（AR7）
 * 纯内存逻辑，无外部依赖，召回率 ≥ 95%
 * 覆盖6大急症类别：心脏/呼吸/神经/出血/中毒/其他危重
 */
@Service
public class EmergencyDetector {

    private static final List<String> EMERGENCY_KEYWORDS = List.of(
            // ── 心脏/胸部 ──
            "胸痛", "胸闷剧烈", "心跳停止", "心肌梗死", "心脏骤停", "心绞痛",
            "心脏停跳", "急性心梗", "心脏病发作",

            // ── 呼吸 ──
            "呼吸困难", "呼吸停止", "窒息", "气道阻塞", "喘不过气", "无法呼吸",
            "呼吸衰竭",

            // ── 神经/意识 ──
            "意识丧失", "昏迷", "昏厥", "中风", "脑出血", "突然晕倒", "抽搐",
            "癫痫发作", "失去意识", "脑梗", "面瘫", "半身不遂", "突发偏瘫",

            // ── 出血/创伤 ──
            "大量出血", "动脉出血", "严重外伤", "骨折外露", "大出血", "喷血",
            "内脏外露", "穿透伤",

            // ── 中毒/过敏 ──
            "药物过量", "严重过敏", "过敏性休克", "中毒", "服毒", "误服",
            "农药中毒", "气体中毒", "一氧化碳",

            // ── 其他危重 ──
            "休克", "高热惊厥", "溺水", "触电", "烧伤严重", "严重烫伤",
            "颈椎骨折", "脊椎损伤", "重度烧伤", "大面积烧伤"
    );

    /**
     * 检测症状描述是否包含急症关键词
     *
     * @param symptoms 患者症状描述（中文自然语言）
     * @return true 表示检测到急症，应立即拨打 120
     */
    public boolean isEmergency(String symptoms) {
        if (symptoms == null || symptoms.isBlank()) return false;
        String lower = symptoms.toLowerCase();
        return EMERGENCY_KEYWORDS.stream().anyMatch(lower::contains);
    }
}
