---
stepsCompleted: [1, 2, 3, 4, 5, 6, 7, 8]
status: 'complete'
completedAt: '2026-04-06'
lastStep: 8
inputDocuments:
  - "_bmad-output/planning-artifacts/prd.md"
  - "_bmad-output/planning-artifacts/epics.md"
  - "_bmad-output/planning-artifacts/product-brief-bmadTest.md"
workflowType: 'architecture'
project_name: 'bmadTest'
user_name: 'Dd'
date: '2026-04-06'
---

# Architecture Decision Document

_This document builds collaboratively through step-by-step discovery. Sections are appended as we work through each architectural decision together._

## Project Context Analysis

### Requirements Overview

**Functional Requirements:**

24项FR覆盖6个功能域：症状输入与AI分析（FR1-6）、位置获取（FR7-9）、医院查询与推荐（FR10-14）、结果展示（FR15-18）、合规安全（FR19-21）、运维可观测性（FR22-24）。核心架构驱动：后端作为AI+地图统一代理，前端保持无状态，位置数据不流经服务器。

**Non-Functional Requirements:**

- 性能：AI响应 < 3s（后端超时上限5s）、FCP < 2s、端到端 < 30s
- 安全：API Key后端环境变量、HTTPS强制、CORS严格配置、无持久化
- 可靠性：任意外部依赖（通义千问/高德地图）故障不导致系统崩溃，均有独立兜底响应
- 无障碍：WCAG 2.1 AA，Mobile First，375px基准断点，触控目标44×44px

**Scale & Complexity:**

- Primary domain: Full-stack Web Application (Healthcare AI + LBS)
- Complexity level: **High**
- Estimated architectural components: 8（前端SPA、后端API网关层、AI代理服务、地图代理服务、急症检测模块、多层Fallback处理器、调用日志模块、健康检查端点）

### Technical Constraints & Dependencies

- **通义千问 DashScope API**（阿里云）：后端代理调用，5s超时，Key不暴露前端
- **高德地图周边搜索API**：后端代理调用，默认5km半径，返回结果按距离升序
- **浏览器Geolocation API**：前端调用，坐标仅传高德地图API，不上传后端服务器
- **无数据库**：查询结果不持久化，PIPL合规基线
- **前后端分离**：Vue SPA + Spring Boot REST API，JSON通信，UTF-8

### Cross-Cutting Concerns Identified

1. **多层Fallback机制**：AI超时兜底文案 / GPS失败手动地址输入 / 高德配额超限提示
2. **API Key安全隔离**：后端环境变量管理，CORS仅允许指定前端域名
3. **PIPL合规**：位置数据使用前明确告知授权，数据最小化，不持久化
4. **结构化错误处理**：所有外部依赖均有独立降级路径，HTTP状态码语义正确
5. **可观测性**：接口调用日志（通义千问+高德）、Spring Boot Actuator健康检查

## Starter Template Evaluation

### Primary Technology Domain

前后端分离全栈 Web Application（Healthcare AI + LBS），技术栈已由PRD明确定义。

### 前端：Vue 3 + Vite SPA

**初始化命令：**

```bash
npm create vue@latest
```

**推荐选项：**

```
✔ Project name: frontend
✔ Add TypeScript? Yes
✔ Add JSX Support? No
✔ Add Vue Router for Single Page Application development? Yes
✔ Add Pinia for state management? Yes
✔ Add Vitest for Unit testing? Yes
✔ Add an End-to-End Testing Solution? No
✔ Add ESLint for code quality? Yes
✔ Add Prettier for code formatting? Yes
✔ Add Vue DevTools 7 extension? Yes
```

**一键命令（跳过交互）：**

```bash
npm create vue@latest -- --typescript --router --pinia --vitest --eslint --prettier
```

**当前版本：** Vue 3.5.x、Vite 8、Pinia 3、Vue Router 5

**额外安装（初始化后）：**

```bash
npm install axios
npm install @vitejs/plugin-legacy  # 微信X5浏览器兼容
```

**Architectural Decisions Provided by Starter（前端）：**

- **Language**: TypeScript — 前后端接口契约类型安全，`isEmergency`等标志位不会运行时才暴露
- **Build Tooling**: Vite 8（Rolldown，Rust-based）— 极速热重载，生产打包优化
- **State Management**: Pinia 3 — 扁平化Store，管理「查询状态/AI结果/位置信息」三块状态
- **Routing**: Vue Router 5 — 首页（症状输入）→ 结果页（医院列表）SPA路由
- **HTTP Client**: Axios（额外安装）— 拦截器统一处理超时和错误边界，优于原生Fetch
- **Testing**: Vitest — 覆盖急症检测逻辑（召回率≥95%）和Fallback触发条件
- **Code Quality**: ESLint + Prettier — 统一代码规范

**前端目录结构：**

```
frontend/src/
├── components/      ← 纯渲染组件（SymptomInput, HospitalCard, EmergencyAlert）
├── views/           ← 页面视图（HomeView, ResultView）
├── stores/          ← Pinia状态（queryStore, locationStore）
├── services/        ← Axios封装（analyzeApi, hospitalApi）
├── utils/           ← 纯函数（emergencyDetector.ts — 可独立Vitest覆盖）
├── router/          ← Vue Router配置
└── types/           ← TypeScript接口定义（AnalysisResponse, Hospital等）
```

**浏览器兼容约束（Party Mode补充）：**

```typescript
// vite.config.ts
build: {
  target: 'es2015'  // 微信X5内核兼容，非默认esnext
}
```

---

### 后端：Spring Boot REST API

**骨架状态：** `demo/` 目录下已有 Spring Initializr 生成的空项目，直接复用。

**build.gradle 补充依赖：**

```groovy
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-actuator'
    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'
    developmentOnly 'org.springframework.boot:spring-boot-devtools'
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
}
```

**application.yml 配置结构：**

```yaml
dashscope:
  api-key: ${DASHSCOPE_API_KEY}   # 环境变量注入，不进版本控制
  timeout: 5000

amap:
  api-key: ${AMAP_API_KEY}        # 环境变量注入，不进版本控制
  radius: 5000

spring:
  web:
    cors:
      allowed-origins: ${FRONTEND_ORIGIN:http://localhost:5173}
```

**后端目录结构：**

```
src/main/java/com/example/demo/
├── controller/          ← REST端点（AnalysisController, HospitalController）
├── service/
│   ├── AiService.java          ← 通义千问代理调用（5s超时+兜底）
│   ├── HospitalService.java    ← 高德地图周边搜索代理
│   └── EmergencyDetector.java  ← 急症标志判断（isEmergency逻辑）
├── dto/                 ← 请求/响应对象（AnalysisRequest, AnalysisResponse, HospitalDto）
├── config/              ← CORS配置、RestTemplate Bean（含超时）
└── exception/           ← 全局异常处理（@RestControllerAdvice）
```

**HTTP客户端选型：** `RestTemplate` + `SimpleClientHttpRequestFactory`（非WebClient，避免引入响应式栈）

```java
// config/RestTemplateConfig.java
@Bean
public RestTemplate restTemplate() {
    SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
    factory.setConnectTimeout(5000);
    factory.setReadTimeout(5000);
    return new RestTemplate(factory);
}
```

**Note:** 前端初始化和后端依赖配置应作为实现阶段的第一批故事（Story 1.1）。

## Core Architectural Decisions

### Decision Priority Analysis

**Critical Decisions（阻断实现）：**
- API端点设计：两个独立接口（POST /api/analyze + POST /api/hospitals）
- 统一错误响应格式：`{ code, message, fallback }`
- API Key管理：后端环境变量注入，不进版本控制

**Important Decisions（影响架构形态）：**
- HTTP客户端：RestTemplate（后端）/ Axios（前端）
- 前端分层：services / utils / stores / components / types
- 浏览器兼容：Vite `build.target: 'es2015'`（微信X5）

**Deferred Decisions（Post-MVP延后）：**
- CI/CD流水线（MVP阶段本地运行即可）
- HTTPS配置（生产部署时处理）
- 高德地图JS SDK地图视图（Phase 2功能）

### Data Architecture

- **数据库**：无 — 查询结果不持久化，PIPL合规基线
- **数据验证**：后端使用 Jakarta Bean Validation（`@NotBlank`、`@NotNull`等）验证入参；前端Axios拦截器统一处理4xx响应
- **缓存**：无 — MVP阶段无需缓存

### Authentication & Security

- **认证授权**：无 — 核心功能无需登录，降低使用门槛（PRD核心设计原则）
- **CORS**：`@Configuration` 严格配置，仅允许 `${FRONTEND_ORIGIN}`（开发环境默认 `http://localhost:5173`）
- **API Key**：通义千问Key、高德Key均通过环境变量注入，`application.yml` 使用占位符，`.gitignore` 排除 `.env` 文件
- **HTTPS**：生产环境强制，开发环境本地HTTP
- **AI输出过滤**：`AiService` 层对通义千问响应做基础内容过滤，拦截异常输出后再返回前端

### API & Communication Patterns

**端点设计（两个独立接口）：**

| 端点 | 方法 | 职责 |
|------|------|------|
| `POST /api/analyze` | POST | 症状文本 → 科室建议 + `isEmergency` 标志 |
| `POST /api/hospitals` | POST | 坐标/地址 + 科室关键词 → 医院列表（距离排序） |
| `GET /actuator/health` | GET | 健康检查 |

**请求/响应示例：**

```json
// POST /api/analyze
// Request
{ "symptoms": "肚子右下方很痛，已经两小时了" }
// Response
{
  "department": "外科/急诊科",
  "reason": "症状提示可能为急腹症（阑尾炎可疑）",
  "isEmergency": false
}

// POST /api/hospitals
// Request（GPS模式）
{ "latitude": 39.9042, "longitude": 116.4074, "department": "外科" }
// Request（手动地址模式）
{ "address": "北京市朝阳区建国路", "department": "外科" }
// Response
[
  {
    "name": "XX医院", "distance": "800m", "address": "...",
    "departments": ["急诊科", "外科"], "phone": "010-XXXX"
  }
]
```

**统一错误响应格式：**

```json
{
  "code": "AI_TIMEOUT",
  "message": "AI分析暂时不可用，请直接拨打120或前往最近医院",
  "fallback": true
}
```

**错误码约定：**

| Code | 场景 |
|------|------|
| `AI_TIMEOUT` | 通义千问超过5s未响应 |
| `AI_ERROR` | 通义千问返回异常 |
| `MAP_ERROR` | 高德地图API异常 |
| `LOCATION_REQUIRED` | 未提供坐标也未提供地址 |
| `INVALID_INPUT` | 入参校验失败 |

**API通信规范：** JSON格式、UTF-8、HTTP状态码语义正确（200 / 400 / 500 / 503）

### Frontend Architecture

- **状态管理**：Pinia — `queryStore`（查询状态、AI结果）、`locationStore`（位置信息、定位模式）
- **路由**：Vue Router — `/`（HomeView，症状输入）→ `/result`（ResultView，医院列表）
- **前端分层职责**：

  | 层 | 目录 | 职责 |
  |----|------|------|
  | 视图 | `views/` | HomeView、ResultView |
  | 组件 | `components/` | SymptomInput、HospitalCard、EmergencyAlert、LocationInput |
  | 状态 | `stores/` | queryStore、locationStore |
  | 服务 | `services/` | analyzeApi.ts、hospitalApi.ts（Axios封装） |
  | 工具 | `utils/` | emergencyDetector.ts（纯函数，Vitest覆盖） |
  | 类型 | `types/` | AnalysisResponse、Hospital、ErrorResponse等TS接口 |

- **性能**：Vite `build.target: 'es2015'` + `@vitejs/plugin-legacy`（微信X5兼容）

### Infrastructure & Deployment

- **本地开发端口**：前端 `:5173`（Vite默认）/ 后端 `:8080`（Spring Boot默认）
- **环境配置**：后端通过环境变量注入敏感Key；前端通过 `.env.local` 管理后端API地址（`VITE_API_BASE_URL`）
- **MVP部署**：本地运行演示，无CI/CD要求
- **监控**：Spring Boot Actuator `/actuator/health`，响应时间 < 500ms

### Decision Impact Analysis

**实现顺序建议：**

1. 后端骨架配置（build.gradle依赖、CORS、RestTemplate Bean、全局异常处理）
2. DTO定义（AnalysisRequest/Response、HospitalRequest、HospitalDto）
3. AiService（通义千问调用 + 5s超时 + 兜底响应）
4. HospitalService（高德地图周边搜索）
5. Controller层（/api/analyze、/api/hospitals）
6. 前端初始化（npm create vue@latest + 安装Axios、plugin-legacy）
7. 前端类型定义（types/目录，与后端DTO对齐）
8. 前端服务层（analyzeApi、hospitalApi）
9. 前端页面（HomeView → ResultView，EmergencyAlert组件）

**跨组件依赖关系：**
- 后端DTO定义直接影响前端 `types/` 目录接口，需先完成后端再对齐前端类型
- `isEmergency` 标志位贯穿：AiService → Controller响应 → 前端queryStore → EmergencyAlert组件
- Fallback机制双端实现：后端AiService（超时降级）+ 前端Axios拦截器（错误边界）

## Implementation Patterns & Consistency Rules

### 识别的潜在冲突点

共识别 **5类** AI Agent可能产生不一致的区域：命名、格式、结构、错误处理、加载状态。

### Naming Patterns（命名规范）

**后端（Java/Spring Boot）：**

| 类型 | 规范 | 示例 |
|------|------|------|
| 类名 | `PascalCase` | `AiService`、`HospitalController` |
| 方法/变量 | `camelCase` | `analyzeSymptoms()`、`isEmergency` |
| 包名 | 全小写点分隔 | `com.example.demo.service` |
| 常量 | `UPPER_SNAKE_CASE` | `DEFAULT_RADIUS`、`AI_TIMEOUT_MS` |

**前端（TypeScript/Vue）：**

| 类型 | 规范 | 示例 |
|------|------|------|
| 组件文件 | `PascalCase.vue` | `HospitalCard.vue`、`EmergencyAlert.vue` |
| 工具/服务文件 | `camelCase.ts` | `emergencyDetector.ts`、`analyzeApi.ts` |
| TypeScript接口 | `PascalCase` | `AnalysisResponse`、`Hospital` |
| Pinia Store | `use`前缀 + `camelCase` | `useQueryStore()`、`useLocationStore()` |
| CSS类名 | `kebab-case` | `.hospital-card`、`.emergency-alert` |

**API字段命名：** JSON字段统一使用 `camelCase`（`isEmergency`、`departmentName`），前后端保持一致，不混用 `snake_case`。

### Structure Patterns（结构规范）

**后端测试位置：** 与源码镜像的 `src/test/java/` 目录，不co-locate

```
src/test/java/com/example/demo/
├── controller/AnalysisControllerTest.java
├── service/AiServiceTest.java
└── service/EmergencyDetectorTest.java   ← 急症词召回率≥95%测试
```

**前端测试位置：** 与源文件co-locate，后缀 `.spec.ts`

```
src/utils/emergencyDetector.ts
src/utils/emergencyDetector.spec.ts     ← 同目录
```

**配置文件位置：**
- 后端敏感配置：环境变量（不进版本控制）
- 前端环境变量：`.env.local`（不进版本控制），`.env.example`（进版本控制作为模板）

### Format Patterns（格式规范）

**API响应格式：**

```json
// ✅ 正确：成功时直接返回数据，不包wrapper
{ "department": "外科", "isEmergency": false }

// ❌ 错误：不要包一层 { "data": { ... }, "status": "ok" }

// ✅ 正确：错误时统一格式
{ "code": "AI_TIMEOUT", "message": "...", "fallback": true }
```

**HTTP状态码约定：**

| 状态码 | 场景 |
|--------|------|
| `200` | 请求成功 |
| `400` | 入参校验失败（缺少symptoms、坐标和地址均未提供） |
| `500` | 系统内部异常 |
| `503` | 外部服务（通义千问/高德）不可用，含fallback响应 |

**数据格式约定：**
- 日期时间：ISO 8601字符串（`"2026-04-06T12:00:00Z"`）
- 布尔值：`true/false`，不用 `1/0`
- 空值：返回 `null`，不省略字段

### Process Patterns（流程规范）

**错误处理模式：**

```java
// ✅ 后端正确模式：外部服务异常在Service层catch，返回降级响应
public AnalysisResponse analyze(String symptoms) {
    try {
        return callDashScope(symptoms);
    } catch (ResourceAccessException e) {
        log.warn("AI timeout: {}", e.getMessage());
        return AnalysisResponse.fallback(); // 不向上抛出
    }
}
// ❌ 错误：让异常冒泡到Controller层再处理
```

```typescript
// ✅ 前端正确模式：Axios拦截器统一处理，组件不catch
// services/analyzeApi.ts — 拦截器处理错误
// ❌ 错误：在每个组件里 try { await analyzeApi() } catch(e) { ... }
```

**加载状态模式：**

```typescript
// ✅ 正确：统一在Store中管理，组件只读取
// useQueryStore.ts
const isLoading = ref(false)
const error = ref<ErrorResponse | null>(null)

async function analyze(symptoms: string) {
  isLoading.value = true
  error.value = null
  try { /* ... */ } finally { isLoading.value = false }
}

// ❌ 错误：组件内维护本地 const loading = ref(false)
```

### Enforcement Guidelines（强制规则）

**All AI Agents MUST（所有Agent必须遵守）：**

1. JSON字段名统一使用 `camelCase`，不混用 `snake_case`
2. 后端外部服务调用必须在Service层做异常降级，不向Controller抛出
3. 前端异步状态（isLoading/error/result）统一由Pinia Store管理，组件不持有本地状态
4. API Key绝不硬编码，统一从环境变量读取
5. 错误响应必须包含 `code` 字段，便于前端精确处理不同错误场景
6. 组件文件名 `PascalCase.vue`，工具/服务文件名 `camelCase.ts`，不混用

**Anti-Patterns（禁止模式）：**

```typescript
// ❌ 组件直接调用API
const result = await fetch('/api/analyze', { body: symptoms })

// ✅ 应通过Store Action调用
await queryStore.analyze(symptoms)
```

```java
// ❌ Controller直接调用第三方API
@PostMapping("/api/analyze")
public ResponseEntity<?> analyze(@RequestBody ...) {
    HttpClient.post("https://dashscope.aliyuncs.com/...");
}

// ✅ 应通过Service层代理
public ResponseEntity<?> analyze(@RequestBody ...) {
    return ResponseEntity.ok(aiService.analyze(request.getSymptoms()));
}

## Project Structure & Boundaries

### Complete Project Directory Structure

```
bmadTest/
├── demo/                          ← Spring Boot 后端（已有骨架）
│   ├── docs/                      ← 项目规划文档
│   │   ├── prd.md
│   │   └── architecture.md
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/demo/
│   │   │   │   ├── DemoApplication.java
│   │   │   │   ├── controller/
│   │   │   │   │   ├── AnalysisController.java    ← POST /api/analyze
│   │   │   │   │   └── HospitalController.java    ← POST /api/hospitals
│   │   │   │   ├── service/
│   │   │   │   │   ├── AiService.java             ← 通义千问代理（5s超时+兜底）
│   │   │   │   │   ├── HospitalService.java       ← 高德地图代理
│   │   │   │   │   └── EmergencyDetector.java     ← isEmergency判断逻辑
│   │   │   │   ├── dto/
│   │   │   │   │   ├── AnalysisRequest.java
│   │   │   │   │   ├── AnalysisResponse.java      ← 含 isEmergency 字段
│   │   │   │   │   ├── HospitalRequest.java       ← latitude/longitude/address/department
│   │   │   │   │   ├── HospitalDto.java           ← name/distance/address/departments
│   │   │   │   │   └── ErrorResponse.java         ← code/message/fallback
│   │   │   │   ├── config/
│   │   │   │   │   ├── RestTemplateConfig.java    ← Bean + 超时配置
│   │   │   │   │   └── CorsConfig.java            ← 跨域配置
│   │   │   │   └── exception/
│   │   │   │       └── GlobalExceptionHandler.java ← @RestControllerAdvice
│   │   │   └── resources/
│   │   │       ├── application.yml                ← 环境变量占位符配置
│   │   │       └── application-local.yml          ← 本地开发覆盖（.gitignore排除）
│   │   └── test/
│   │       └── java/com/example/demo/
│   │           ├── controller/
│   │           │   ├── AnalysisControllerTest.java
│   │           │   └── HospitalControllerTest.java
│   │           └── service/
│   │               ├── AiServiceTest.java
│   │               ├── HospitalServiceTest.java
│   │               └── EmergencyDetectorTest.java  ← 急症词召回率≥95%验证
│   ├── build.gradle
│   ├── settings.gradle
│   └── .gitignore
│
└── frontend/                      ← Vue 3 SPA（待初始化）
    ├── src/
    │   ├── main.ts
    │   ├── App.vue
    │   ├── views/
    │   │   ├── HomeView.vue        ← 症状输入页（FR1, FR7-8, FR18-19）
    │   │   └── ResultView.vue     ← 医院列表页（FR3, FR5, FR10-17）
    │   ├── components/
    │   │   ├── SymptomInput.vue   ← 症状文本输入框（FR1）
    │   │   ├── LocationInput.vue  ← GPS/手动地址（FR7-8）
    │   │   ├── EmergencyAlert.vue ← 120急救提示（FR4-5）
    │   │   ├── HospitalCard.vue   ← 单家医院卡片（FR12-13, FR17）
    │   │   ├── HospitalList.vue   ← 医院列表（FR11, FR14）
    │   │   └── DisclaimerBanner.vue ← 免责声明（FR18）
    │   ├── stores/
    │   │   ├── queryStore.ts      ← 查询状态/AI结果/isLoading/error
    │   │   └── locationStore.ts   ← 位置信息/定位模式（GPS|manual）
    │   ├── services/
    │   │   ├── analyzeApi.ts      ← POST /api/analyze 封装
    │   │   ├── hospitalApi.ts     ← POST /api/hospitals 封装
    │   │   └── http.ts            ← Axios实例 + 拦截器（统一错误处理）
    │   ├── utils/
    │   │   ├── emergencyDetector.ts      ← isEmergency标志解析（纯函数）
    │   │   └── emergencyDetector.spec.ts ← co-locate测试
    │   ├── router/
    │   │   └── index.ts           ← / → HomeView, /result → ResultView
    │   └── types/
    │       ├── analysis.ts        ← AnalysisRequest, AnalysisResponse
    │       ├── hospital.ts        ← Hospital, HospitalRequest
    │       └── error.ts           ← ErrorResponse
    ├── public/
    ├── index.html
    ├── vite.config.ts             ← build.target: 'es2015' + plugin-legacy
    ├── tsconfig.json
    ├── .env.example               ← VITE_API_BASE_URL=http://localhost:8080（进版本控制）
    ├── .env.local                 ← 实际值（.gitignore排除）
    └── package.json
```

### Architectural Boundaries

**API边界：**

| 边界 | 入口 | 出口 |
|------|------|------|
| 前端 → 后端 | `POST /api/analyze`、`POST /api/hospitals` | JSON响应（camelCase） |
| 后端 → 通义千问 | DashScope REST API | 科室建议文本 + 急症标志 |
| 后端 → 高德地图 | POI周边搜索API | 医院POI列表 |
| 前端 → 浏览器 | Geolocation API | 经纬度坐标（不上传后端） |

**服务边界：**
- `AiService` 只负责通义千问调用和超时降级，不关心地图
- `HospitalService` 只负责高德地图查询，不调用AI
- `EmergencyDetector` 无外部依赖，纯逻辑判断，可独立测试
- `GlobalExceptionHandler` 统一处理所有未捕获异常，不依赖具体Service

### Requirements to Structure Mapping

| 功能域 | FR | 主要文件 |
|--------|-----|---------|
| 症状输入+AI分析 | FR1-6 | `AiService.java`、`EmergencyDetector.java`、`SymptomInput.vue`、`EmergencyAlert.vue` |
| 位置获取 | FR7-9 | `LocationInput.vue`、`locationStore.ts`、`HospitalService.java` |
| 医院查询推荐 | FR10-14 | `HospitalService.java`、`HospitalController.java`、`HospitalList.vue`、`HospitalCard.vue` |
| 结果展示 | FR15-18 | `ResultView.vue`、`queryStore.ts`、`DisclaimerBanner.vue` |
| 合规安全 | FR19-21 | `CorsConfig.java`、`application.yml`、`.env.local` |
| 运维可观测 | FR22-24 | `GlobalExceptionHandler.java`、Spring Boot Actuator（自动配置） |

### Integration Points

**数据流：**

```
用户输入症状
    ↓
HomeView → queryStore.analyze()
    ↓
analyzeApi.ts → POST /api/analyze（:8080）
    ↓
AnalysisController → AiService → 通义千问 DashScope
    ↓ AnalysisResponse（含 isEmergency）
ResultView：isEmergency=true → EmergencyAlert | false → HospitalList
    ↓
hospitalApi.ts → POST /api/hospitals（:8080）
    ↓
HospitalController → HospitalService → 高德地图 API
    ↓ HospitalDto[]（距离排序）
HospitalList 渲染
```

**外部集成点：**
- 通义千问：`AiService.java`，超时5s，失败返回 `AnalysisResponse.fallback()`
- 高德地图：`HospitalService.java`，半径5km，按距离升序
- 浏览器Geolocation：`locationStore.ts`，失败切换 `LocationInput.vue` 手动模式

## Architecture Validation Results

### Coherence Validation ✅

**Decision Compatibility:**
- Vue 3.5 + Vite 8 + Pinia 3 + Vue Router 5 + TypeScript — 全部官方同代版本，无冲突
- Spring Boot + Gradle + Lombok + RestTemplate + Actuator — 标准组合，未引入响应式栈
- `build.target: 'es2015'` + `plugin-legacy` — 正确处理微信X5兼容性

**Pattern Consistency:**
- camelCase JSON字段在后端DTO和前端TypeScript types中统一
- 错误码（`AI_TIMEOUT`等）在后端ErrorResponse和前端error.ts中一一对应
- `isEmergency`标志位完整贯穿：AiService → Controller → queryStore → EmergencyAlert
- `emergencyDetector.ts`（前端）职责明确：解析响应标志触发UI行为，非重复判断逻辑

**Structure Alignment:**
- 项目结构与分层决策完全对应（controller/service/dto/config/exception）
- 前端分层（views/components/stores/services/utils/types）与状态管理模式吻合
- 测试文件位置规范统一（后端镜像目录，前端co-locate）

### Requirements Coverage Validation ✅

**Functional Requirements Coverage:**

| 功能域 | FR | 覆盖文件 |
|--------|-----|---------|
| 症状输入+AI分析 | FR1-6 | `AiService.java`、`EmergencyDetector.java`、`SymptomInput.vue`、`EmergencyAlert.vue` |
| 位置获取 | FR7-9 | `locationStore.ts`、`LocationInput.vue`、`HospitalService.java` |
| 医院查询推荐 | FR10-14 | `HospitalService.java`、`HospitalController.java`、`HospitalList.vue`、`HospitalCard.vue` |
| 结果展示 | FR15-18 | `ResultView.vue`、`queryStore.ts`、`DisclaimerBanner.vue` |
| 合规安全 | FR19-21 | `CorsConfig.java`、`application.yml`（环境变量占位符）、`.env.local` |
| 运维可观测 | FR22-24 | `GlobalExceptionHandler.java`、Spring Boot Actuator |

**Non-Functional Requirements Coverage:**

| NFR | 架构覆盖方式 |
|-----|------------|
| AI响应 < 3s（超时5s） | RestTemplate `setReadTimeout(5000)` |
| FCP < 2s | Vite 8 打包优化，gzip < 500KB |
| API Key安全 | `application.yml` 环境变量占位符，`.gitignore` 排除敏感文件 |
| 无持久化（PIPL合规） | 无数据库引入，无存储逻辑 |
| CORS严格配置 | `CorsConfig.java` 仅允许 `${FRONTEND_ORIGIN}` |
| 健康检查 < 500ms | Spring Boot Actuator `/actuator/health` |
| WCAG 2.1 AA | 组件实现层面处理（正确延后至开发阶段） |

### Implementation Readiness Validation ✅

**Decision Completeness:** 所有关键决策均已文档化，含版本号和选型理由
**Structure Completeness:** 完整目录树已定义，每个文件均有明确职责说明
**Pattern Completeness:** 5类冲突点（命名/格式/结构/错误处理/加载状态）均已覆盖，含反例

### Gap Analysis Results

| 优先级 | 缺口 | 处理方式 |
|--------|------|---------|
| 建议 | `.gitignore`内容细节 | 实现Story时处理，确保排除`application-local.yml`和`.env.local` |
| 建议 | 高德地图GCJ-02坐标系 | `HospitalService`实现时注意国内坐标系（GCJ-02，非WGS-84） |

无关键缺口，不阻断实现。

### Architecture Completeness Checklist

**✅ Requirements Analysis**
- [x] 项目上下文深度分析（24项FR，6个功能域）
- [x] 规模与复杂度评估（High，8个架构组件）
- [x] 技术约束识别（无数据库、无认证、PIPL合规）
- [x] 横切关注点映射（5类）

**✅ Architectural Decisions**
- [x] 关键决策文档化（含版本号）
- [x] 技术栈完整指定（前后端均覆盖）
- [x] 集成模式定义（通义千问/高德代理模式）
- [x] 性能约束架构支撑

**✅ Implementation Patterns**
- [x] 命名规范建立（前后端分别定义）
- [x] 结构模式定义（测试位置、配置文件）
- [x] 通信模式指定（API格式、状态管理）
- [x] 流程模式文档化（错误处理、加载状态，含反例）

**✅ Project Structure**
- [x] 完整目录结构定义
- [x] 组件边界建立
- [x] 集成点映射
- [x] 需求到结构的完整映射

### Architecture Readiness Assessment

**Overall Status: ✅ READY FOR IMPLEMENTATION**

**Confidence Level: 高**

**关键优势：**
1. 无数据库设计大幅降低MVP复杂度，专注核心链路验证
2. 双端Fallback机制（后端RestTemplate超时 + 前端Axios拦截器）覆盖所有外部依赖失效场景
3. `EmergencyDetector`独立可测，急症召回率（≥95%）可通过Vitest量化验证
4. 前后端DTO类型对齐，TypeScript接口与Java DTO一一对应，消除运行时类型错误

**Areas for Future Enhancement（Post-MVP）：**
- Phase 2：高德地图JS SDK地图视图、历史查询localStorage
- Phase 3：号源API接入、医保数据过滤、多轮对话式问诊
```
