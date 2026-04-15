package com.example.demo;

import com.example.demo.service.EmergencyDetector;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * EmergencyDetector 单元测试
 * - ≥10 急症阳性场景（mustReturnTrue）
 * - ≥10 普通症状阴性场景（mustReturnFalse）
 * - 边界场景 + 混合场景
 * 召回率目标：≥ 95%（20个急症场景中至少19个通过）
 */
class EmergencyDetectorTest {

    private final EmergencyDetector detector = new EmergencyDetector();

    // ─── 急症阳性场景（应返回 true）───

    @Test
    void testChestPain() {
        assertTrue(detector.isEmergency("突然胸痛剧烈，持续5分钟"));
    }

    @Test
    void testBreathingDifficulty() {
        assertTrue(detector.isEmergency("呼吸困难，喘不过气来"));
    }

    @Test
    void testHeartAttack() {
        assertTrue(detector.isEmergency("心肌梗死症状，胸部压迫感"));
    }

    @Test
    void testCardiacArrest() {
        assertTrue(detector.isEmergency("心跳停止，没有脉搏"));
    }

    @Test
    void testLossOfConsciousness() {
        assertTrue(detector.isEmergency("突然意识丧失，倒在地上"));
    }

    @Test
    void testComa() {
        assertTrue(detector.isEmergency("病人昏迷，叫不醒"));
    }

    @Test
    void testStroke() {
        assertTrue(detector.isEmergency("疑似中风，口角歪斜，手脚无力"));
    }

    @Test
    void testBrainHemorrhage() {
        assertTrue(detector.isEmergency("脑出血，剧烈头痛后昏倒"));
    }

    @Test
    void testSeizure() {
        assertTrue(detector.isEmergency("抽搐，四肢不受控制"));
    }

    @Test
    void testMassiveBleed() {
        assertTrue(detector.isEmergency("车祸后大量出血，止不住"));
    }

    @Test
    void testArterialBleeding() {
        assertTrue(detector.isEmergency("动脉出血，血流如注"));
    }

    @Test
    void testSevereAllergy() {
        assertTrue(detector.isEmergency("严重过敏反应，全身红肿"));
    }

    @Test
    void testAnaphylacticShock() {
        assertTrue(detector.isEmergency("过敏性休克，血压下降"));
    }

    @Test
    void testDrugOverdose() {
        assertTrue(detector.isEmergency("药物过量，服了很多安眠药"));
    }

    @Test
    void testShock() {
        assertTrue(detector.isEmergency("病人休克，意识模糊，面色苍白"));
    }

    @Test
    void testDrowning() {
        assertTrue(detector.isEmergency("溺水后救上来，已无呼吸"));
    }

    @Test
    void testElectrocution() {
        assertTrue(detector.isEmergency("触电后昏迷，无意识"));
    }

    @Test
    void testSevereBurn() {
        assertTrue(detector.isEmergency("烧伤严重，大面积皮肤损伤"));
    }

    @Test
    void testPoisoning() {
        assertTrue(detector.isEmergency("农药中毒，误食了农药"));
    }

    @Test
    void testSuddenFaint() {
        assertTrue(detector.isEmergency("突然晕倒在路边，不省人事"));
    }

    // ─── 普通症状阴性场景（应返回 false）───

    @Test
    void testHeadache() {
        assertFalse(detector.isEmergency("头痛头晕两天了，可能是睡眠不足"));
    }

    @Test
    void testCough() {
        assertFalse(detector.isEmergency("咳嗽有痰，嗓子有点疼"));
    }

    @Test
    void testFever() {
        assertFalse(detector.isEmergency("发烧38度，有点怕冷"));
    }

    @Test
    void testStomachPain() {
        assertFalse(detector.isEmergency("肚子隐隐作痛，可能是吃坏东西了"));
    }

    @Test
    void testRunnyNose() {
        assertFalse(detector.isEmergency("流鼻涕，鼻塞，感冒症状"));
    }

    @Test
    void testSkinRash() {
        assertFalse(detector.isEmergency("皮肤有点痒，出现红疹"));
    }

    @Test
    void testJointPain() {
        assertFalse(detector.isEmergency("膝盖关节疼痛，走路有点不舒服"));
    }

    @Test
    void testToothache() {
        assertFalse(detector.isEmergency("牙齿疼痛，右侧后槽牙"));
    }

    @Test
    void testBackPain() {
        assertFalse(detector.isEmergency("腰酸背痛，久坐之后"));
    }

    @Test
    void testIndigestion() {
        assertFalse(detector.isEmergency("消化不良，饭后胃胀"));
    }

    @Test
    void testFatigue() {
        assertFalse(detector.isEmergency("最近很疲倦，精力不足"));
    }

    @Test
    void testSleepProblems() {
        assertFalse(detector.isEmergency("睡眠质量差，入睡困难"));
    }

    // ─── 边界场景 ───

    @Test
    void testNull() {
        assertFalse(detector.isEmergency(null));
    }

    @Test
    void testEmpty() {
        assertFalse(detector.isEmergency(""));
    }

    @Test
    void testBlankString() {
        assertFalse(detector.isEmergency("   "));
    }

    @Test
    void testMixedNormalAndEmergency() {
        // 混合场景：普通症状词 + 急症词 → 应返回 true
        assertTrue(detector.isEmergency("有点咳嗽，但突然胸痛剧烈"));
    }

    @Test
    void testMixedColdAndChestPain() {
        // 感冒症状中混有急症词 → 应返回 true
        assertTrue(detector.isEmergency("发烧流鼻涕，同时感觉胸闷剧烈"));
    }

    @Test
    void testLongDescription() {
        // 长段症状描述含急症词
        assertTrue(detector.isEmergency("昨天开始头痛，今天吃东西也不舒服，下午突然昏迷了，家人很担心"));
    }
}
