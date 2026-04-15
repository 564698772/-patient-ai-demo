---
stepsCompleted: ["step-01-validate-prerequisites", "step-02-design-epics", "step-03-create-stories", "step-04-final-validation"]
status: 'complete'
completedAt: '2026-04-06'
inputDocuments:
  - "_bmad-output/planning-artifacts/prd.md"
  - "_bmad-output/planning-artifacts/architecture.md"
---

# bmadTest（患者就医AI推荐系统）- Epic Breakdown

## Overview

This document provides the complete epic and story breakdown for bmadTest（患者就医AI推荐系统），decomposing the requirements from the PRD and Architecture into implementable stories.

## Requirements Inventory

### Functional Requirements

FR1: 患者可通过自然语言文本输入描述自身症状
FR2: 系统可将患者的症状描述提交至AI服务进行分析
FR3: 系统可基于AI分析结果向患者展示推荐就诊科室及简要说明
FR4: 系统可识别症状描述中的高危紧急信号，并触发急症优先响应模式
FR5: 当急症信号被识别时，系统可向患者展示急救提示（含120拨打建议）
FR6: 当AI服务不可用或超时时，系统可向患者展示兜底提示文案
FR7: 患者可授权系统获取其当前地理位置
FR8: 患者可在自动定位失败时手动输入地址作为替代位置
FR9: 系统可基于患者位置（自动或手动）执行附近医院查询
FR10: 系统可查询患者当前位置周边的医院列表
FR11: 系统可将医院列表按与患者的距离由近至远排序展示
FR12: 系统可在每家医院条目中展示医院名称、距离、地址信息
FR13: 系统可在医院条目中标注与AI推荐科室相匹配的科室信息
FR14: 系统可保证推荐列表至少包含3家医院结果（数据可用时）
FR15: 患者可查看本次查询的完整结果页面（AI分析 + 医院列表）
FR16: 患者可在结果页面看到AI推荐理由，以便自主判断是否合适
FR17: 患者可通过点击医院条目获取该医院的导航或更多信息（链接至地图）
FR18: 系统可在页面显著位置展示医疗AI免责声明
FR19: 系统可在首次获取位置前向患者明确告知位置数据的使用目的
FR20: 系统可确保患者症状与位置数据不被持久化存储至服务器
FR21: 系统可将AI服务API密钥与地图服务API密钥保存于后端，不暴露至前端
FR22: 运维人员可通过健康检查接口确认系统服务是否正常运行
FR23: 系统可记录AI服务与地图服务的接口调用日志，供排查使用
FR24: 系统可在AI或地图服务发生异常时给出可识别的错误响应，不崩溃

### NonFunctional Requirements

NFR1: 首屏内容渲染（FCP）< 2秒（4G网络，Vue打包gzip < 500KB）
NFR2: AI症状分析响应 < 3秒（通义千问接口，后端超时上限5秒）
NFR3: 高德地图POI查询响应 < 2秒（正常网络条件）
NFR4: 端到端查询完整流程 < 30秒（含用户输入时间）
NFR5: 页面交互响应（点击/滚动）< 100ms（Vue前端本地交互）
NFR6: 所有API密钥存储于Spring Boot后端环境变量，禁止出现在前端代码或版本控制中
NFR7: 生产环境全站强制HTTPS，HTTP请求重定向至HTTPS（MVP阶段本地运行豁免）
NFR8: 前端传递至后端的数据仅限症状文本与位置坐标，后端不记录、不持久化
NFR9: Spring Boot后端CORS严格配置，仅允许指定前端域名访问
NFR10: AI返回内容在后端做基础过滤，拦截明显的注入或敏感信息
NFR11: 任意单个外部依赖（通义千问/高德地图）故障不导致系统整体崩溃，均有独立兜底响应
NFR12: 后端接口异常返回结构化JSON错误响应，HTTP状态码语义正确（4xx/5xx）
NFR13: 前端对所有异步请求做错误边界处理，用户不会看到空白页或未处理的JS报错
NFR14: Spring Boot Actuator /actuator/health端点可用，响应时间 < 500ms
NFR15: 正文字体最小16px，主操作区文字 ≥ 18px
NFR16: 颜色对比度符合WCAG 2.1 AA（正文 ≥ 4.5:1，大字 ≥ 3:1）
NFR17: 急症警告不依赖颜色单一传达信息，同时使用图标与文字
NFR18: 所有表单输入框具备明确label或aria-label
NFR19: 触控目标最小尺寸44×44px
NFR20: 通义千问DashScope API后端代理调用，超时5秒，失败率>3次/分钟时记录告警日志
NFR21: 高德地图周边搜索API后端调用，半径默认5km可配置，返回结果按距离升序
NFR22: 前后端API通信格式统一为JSON，字符集UTF-8

### Additional Requirements

（来自Architecture文档的技术实现要求）

- AR1: 前端使用 `npm create vue@latest` 初始化（TypeScript + Vue Router + Pinia + Vitest + ESLint + Prettier），额外安装 Axios 和 @vitejs/plugin-legacy
- AR2: 后端 Spring Boot 骨架补充依赖：spring-boot-starter-web、spring-boot-starter-actuator、lombok、spring-boot-devtools
- AR3: RestTemplate Bean 配置 connectTimeout=5000ms、readTimeout=5000ms
- AR4: CorsConfig.java 配置 CORS，仅允许 ${FRONTEND_ORIGIN}（默认 http://localhost:5173）
- AR5: application.yml 使用环境变量占位符（${DASHSCOPE_API_KEY}、${AMAP_API_KEY}），不进版本控制
- AR6: GlobalExceptionHandler.java 使用 @RestControllerAdvice 统一处理所有未捕获异常
- AR7: EmergencyDetector.java 作为独立可测试服务，实现急症词识别（召回率≥95%）
- AR8: 统一错误响应格式 `{ "code": "...", "message": "...", "fallback": true/false }`
- AR9: Vite build.target 设置为 'es2015'，配合 @vitejs/plugin-legacy 确保微信X5浏览器兼容
- AR10: 前端 .env.local 存储 VITE_API_BASE_URL（不进版本控制），.env.example 作为模板进版本控制
- AR11: HospitalService 实现时注意高德地图 GCJ-02 坐标系（非WGS-84）
- AR12: EmergencyDetectorTest.java 必须覆盖急症词场景，验证召回率 ≥ 95%

### UX Design Requirements

N/A — 本项目暂无UX设计文档。无障碍和响应式要求已纳入NFR15-NFR19。

### FR Coverage Map

FR1: Epic 2 — 症状文本输入
FR2: Epic 2 — 症状提交AI分析
FR3: Epic 2 — 展示科室建议及推荐理由
FR4: Epic 2 — 高危症状识别与急症响应触发
FR5: Epic 2 — 急救提示展示（120拨打建议）
FR6: Epic 2 — AI超时兜底提示文案
FR7: Epic 3 — GPS自动定位授权
FR8: Epic 3 — 手动地址输入Fallback
FR9: Epic 3 — 基于位置执行医院查询
FR10: Epic 3 — 查询周边医院列表
FR11: Epic 3 — 按距离排序展示医院
FR12: Epic 3 — 展示医院名称、距离、地址
FR13: Epic 3 — 标注匹配科室信息
FR14: Epic 3 — 保证至少3家医院结果
FR15: Epic 4 — 完整结果页面（AI分析+医院列表）
FR16: Epic 4 — 展示AI推荐理由
FR17: Epic 4 — 医院导航/详情链接
FR18: Epic 4 — 医疗AI免责声明展示
FR19: Epic 4 — 位置数据使用目的告知
FR20: Epic 4 — 数据不持久化保证
FR21: Epic 4 — API密钥后端隔离
FR22: Epic 1 — 健康检查接口
FR23: Epic 5 — 接口调用日志记录
FR24: Epic 5 — 结构化错误响应

## Epic List

### Epic 1：项目骨架与基础设施
前后端项目均可在本地启动，健康检查端点可用，CORS配置正确，为所有后续Epic提供可运行的基础。
**覆盖 FRs：** FR22
**覆盖 NFRs：** NFR12, NFR14, NFR22
**覆盖 ARs：** AR1, AR2, AR3, AR4, AR5, AR6, AR8, AR10

### Epic 2：AI症状分析与急症检测
患者可输入自然语言症状描述，系统分析后返回推荐科室和推荐理由；高危症状触发急救提示；AI超时有兜底文案。
**覆盖 FRs：** FR1, FR2, FR3, FR4, FR5, FR6
**覆盖 NFRs：** NFR2, NFR10, NFR11
**覆盖 ARs：** AR7, AR12

### Epic 3：位置获取与附近医院查询
患者可授权GPS定位或手动输入地址，系统返回附近医院列表（按距离排序），含医院名称、距离、地址和匹配科室，至少3家。
**覆盖 FRs：** FR7, FR8, FR9, FR10, FR11, FR12, FR13, FR14
**覆盖 NFRs：** NFR3, NFR21
**覆盖 ARs：** AR11

### Epic 4：完整就医流程与合规展示
患者可查看完整结果页（AI分析+医院列表），含推荐理由、导航链接、免责声明；系统在获取位置前告知用途；数据不持久化；Mobile First响应式UI，满足无障碍标准。
**覆盖 FRs：** FR15, FR16, FR17, FR18, FR19, FR20, FR21
**覆盖 NFRs：** NFR1, NFR4, NFR5, NFR6, NFR8, NFR9, NFR13, NFR15, NFR16, NFR17, NFR18, NFR19

### Epic 5：可观测性与运维加固
系统记录AI和地图接口调用日志；所有异常返回可识别的结构化错误响应；API Key安全隔离，不暴露至前端。
**覆盖 FRs：** FR23, FR24
**覆盖 NFRs：** NFR6, NFR7, NFR20

---

## Epic 1：项目骨架与基础设施

前后端项目均可在本地启动，健康检查端点可用，CORS配置正确，为所有后续Epic提供可运行的基础。

### Story 1.1：后端 Spring Boot 依赖配置与健康检查

As a developer,
I want the Spring Boot backend to have all required dependencies configured and a working health check endpoint,
So that the backend is ready to receive API requests and can be monitored.

**Acceptance Criteria:**

**Given** 开发者克隆项目并进入 `demo/` 目录
**When** 执行 `./gradlew bootRun`
**Then** 服务在 :8080 端口成功启动，无报错日志
**And** 终端输出包含 `Started DemoApplication`

**Given** 服务已启动
**When** 请求 `GET http://localhost:8080/actuator/health`
**Then** 返回 `200 OK`，响应体为 `{"status":"UP"}`
**And** 响应时间 < 500ms

**Given** 开发者查看 `build.gradle`
**When** 检查依赖声明
**Then** 包含 `spring-boot-starter-web`、`spring-boot-starter-actuator`、`lombok`、`spring-boot-devtools`

**Given** 开发者查看 `src/main/resources/application.yml`
**When** 检查配置内容
**Then** 存在 `dashscope.api-key: ${DASHSCOPE_API_KEY}` 和 `amap.api-key: ${AMAP_API_KEY}` 占位符
**And** 存在 `application-local.yml`（已加入 .gitignore）供本地开发填写实际值

**覆盖 FRs：** FR22 | **覆盖 ARs：** AR2, AR5

---

### Story 1.2：前端 Vue 3 项目初始化与工程配置

As a developer,
I want a Vue 3 frontend project initialized with the correct toolchain and directory structure,
So that the frontend is ready for feature development with TypeScript, routing, state management, and tests.

**Acceptance Criteria:**

**Given** 开发者在项目根目录完成前端初始化并执行 `npm install && npm run dev`
**When** 开发服务器启动
**Then** 前端在 :5173 端口成功运行，浏览器无控制台报错

**Given** 开发者查看 `frontend/src/` 目录
**When** 检查目录结构
**Then** 存在以下目录：`views/`、`components/`、`stores/`、`services/`、`utils/`、`types/`、`router/`

**Given** 开发者查看 `frontend/vite.config.ts`
**When** 检查构建配置
**Then** `build.target` 设置为 `'es2015'`
**And** 已配置 `@vitejs/plugin-legacy`

**Given** 开发者查看 `frontend/` 根目录
**When** 检查环境变量文件
**Then** 存在 `.env.example`，内容包含 `VITE_API_BASE_URL=http://localhost:8080`
**And** `.env.local` 已加入 `.gitignore`

**Given** 开发者执行 `npm run type-check`
**When** 类型检查完成
**Then** 无 TypeScript 编译错误

**覆盖 ARs：** AR1, AR9, AR10

---

### Story 1.3：CORS 配置与前后端本地联通验证

As a developer,
I want the backend CORS policy configured to allow requests only from the frontend dev server,
So that the frontend at :5173 can call backend APIs at :8080 without CORS errors, while non-localhost origins are blocked.

**Acceptance Criteria:**

**Given** 后端已启动（:8080），前端已启动（:5173）
**When** 前端向 `http://localhost:8080/actuator/health` 发起 fetch 请求
**Then** 请求成功返回，浏览器控制台无 CORS 错误

**Given** 请求来源为非 localhost（如 `https://example.com`）
**When** 发起跨域请求
**Then** 浏览器阻止该请求，响应头不含 `Access-Control-Allow-Origin`

**Given** 开发者查看 `CorsConfig.java`
**When** 检查配置
**Then** 仅允许 `${FRONTEND_ORIGIN}`（默认 `http://localhost:5173`）

**Given** 开发者查看 `RestTemplateConfig.java`
**When** 检查 Bean 配置
**Then** `connectTimeout` 和 `readTimeout` 均设置为 5000ms

**Given** 后端收到格式非法的请求
**When** 请求到达任意 Controller
**Then** 返回 `{ "code": "INVALID_INPUT", "message": "...", "fallback": false }`，HTTP 状态码为 400

**覆盖 ARs：** AR3, AR4, AR6, AR8

---

## Epic 2：AI症状分析与急症检测

患者可输入自然语言症状描述，系统分析后返回推荐科室和推荐理由；高危症状触发急救提示；AI超时有兜底文案。

### Story 2.1：急症词检测服务（EmergencyDetector）

As a developer,
I want a pure-function emergency detector service that identifies high-risk symptom keywords,
So that life-threatening situations trigger an immediate 120 alert instead of a standard hospital recommendation.

**Acceptance Criteria:**

**Given** 症状描述包含高危关键词（如"胸痛"、"呼吸困难"、"意识丧失"、"大量出血"）
**When** 调用 `EmergencyDetector.isEmergency(symptoms)`
**Then** 返回 `true`

**Given** 症状描述为普通症状（如"头晕"、"咳嗽"、"腹痛"）
**When** 调用 `EmergencyDetector.isEmergency(symptoms)`
**Then** 返回 `false`

**Given** `EmergencyDetectorTest.java` 覆盖20个以上症状场景
**When** 执行 `./gradlew test`
**Then** 所有测试通过，急症词召回率 ≥ 95%（即高危场景中至少95%被正确识别）

**Given** 开发者查看 `EmergencyDetector.java`
**When** 检查类结构
**Then** 该类无外部依赖，为纯逻辑判断，可独立单元测试

**覆盖 FRs：** FR4 | **覆盖 ARs：** AR7, AR12

---

### Story 2.2：AI症状分析服务（AiService + 通义千问集成）

As a patient,
I want my symptom description analyzed by AI to get a department recommendation with reasoning,
So that I know which department to visit without needing medical knowledge.

**Acceptance Criteria:**

**Given** 后端配置了有效的 `DASHSCOPE_API_KEY` 环境变量
**When** 调用 `AiService.analyze("肚子右下方很痛，已经两小时了")`
**Then** 返回包含 `department`（科室名称）和 `reason`（推荐理由）的响应对象
**And** `isEmergency` 字段值由 `EmergencyDetector` 判断后注入

**Given** 通义千问 API 响应时间超过 5 秒
**When** `AiService` 触发超时
**Then** 不向上抛出异常，返回兜底 `AnalysisResponse`：`{ department: null, reason: null, isEmergency: false, fallback: true, fallbackMessage: "AI分析暂时不可用，请直接拨打120或前往最近医院" }`

**Given** 通义千问 API 返回错误响应
**When** `AiService` 捕获异常
**Then** 记录 `WARN` 级别日志并返回兜底响应，服务不崩溃

**Given** 开发者查看 `AiService.java`
**When** 检查 API 调用代码
**Then** 使用注入的 `RestTemplate` Bean（含5s超时），API Key 从配置属性读取，不硬编码

**覆盖 FRs：** FR2, FR3, FR6 | **覆盖 NFRs：** NFR2, NFR10, NFR11

---

### Story 2.3：症状分析 API 端点（AnalysisController）

As a frontend developer,
I want a REST endpoint that accepts symptom text and returns AI analysis results,
So that the frontend can trigger symptom analysis with a single API call.

**Acceptance Criteria:**

**Given** 发送 `POST /api/analyze`，请求体为 `{ "symptoms": "头痛发烧两天" }`
**When** AI分析成功
**Then** 返回 `200 OK`，响应体包含 `department`、`reason`、`isEmergency`（布尔值）字段
**And** `Content-Type` 为 `application/json; charset=UTF-8`

**Given** 发送 `POST /api/analyze`，请求体中 `symptoms` 为空字符串或缺失
**When** 入参校验失败
**Then** 返回 `400 Bad Request`，响应体为 `{ "code": "INVALID_INPUT", "message": "症状描述不能为空", "fallback": false }`

**Given** 发送包含高危关键词的症状（如"胸痛剧烈，呼吸困难"）
**When** AI分析完成
**Then** 响应中 `isEmergency` 为 `true`

**Given** AI服务超时
**When** 请求 `POST /api/analyze`
**Then** 返回 `200 OK`，响应体包含 `fallback: true` 和 `fallbackMessage` 字段（不返回503）

**覆盖 FRs：** FR1, FR2, FR3, FR4, FR5, FR6 | **覆盖 NFRs：** NFR12, NFR22

---

### Story 2.4：前端症状输入组件与 AI 分析调用

As a patient,
I want to type my symptoms and see the AI analysis result on screen,
So that I can understand which department I should visit and whether it's an emergency.

**Acceptance Criteria:**

**Given** 患者打开首页（HomeView）
**When** 页面加载完成
**Then** 显示症状输入框（含 placeholder 示例文字）和查询按钮
**And** 页面显著位置展示免责声明文字

**Given** 患者在输入框中输入症状并点击查询按钮
**When** 前端调用 `analyzeApi.analyze(symptoms)`
**Then** 按钮显示加载状态，查询按钮不可重复点击

**Given** AI 分析返回成功结果（`isEmergency: false`）
**When** 结果展示
**Then** 页面显示推荐科室名称和推荐理由文字

**Given** AI 分析返回 `isEmergency: true`
**When** 结果展示
**Then** 页面优先展示 `EmergencyAlert` 组件，包含"请立即拨打 **120**"提示文字和图标（不依赖颜色单一传达）

**Given** AI 分析返回 `fallback: true`
**When** 结果展示
**Then** 显示兜底提示文案，不显示空白区域

**Given** 患者未输入任何文字直接点击查询
**When** 前端校验
**Then** 输入框显示校验提示，不发起 API 请求

**覆盖 FRs：** FR1, FR3, FR5, FR6, FR18 | **覆盖 NFRs：** NFR13, NFR17, NFR18

---

## Epic 3：位置获取与附近医院查询

患者可授权GPS定位或手动输入地址，系统返回附近医院列表（按距离排序），含医院名称、距离、地址和匹配科室，至少3家。

### Story 3.1：后端医院查询服务（HospitalService + 高德地图集成）

As a patient,
I want the system to find hospitals near my location using a map service,
So that I receive a list of real, nearby hospitals sorted by distance.

**Acceptance Criteria:**

**Given** 后端配置了有效的 `AMAP_API_KEY` 环境变量，并提供有效坐标（latitude, longitude）
**When** 调用 `HospitalService.findNearby(latitude, longitude, department)`
**Then** 返回按距离升序排列的医院列表，每条包含 `name`、`distance`、`address`、`departments` 字段

**Given** 提供有效坐标，搜索半径5km内有医院
**When** 调用 `HospitalService.findNearby()`
**Then** 返回至少3家医院（数据可用时）

**Given** 提供手动输入的地址字符串（而非坐标）
**When** 调用服务
**Then** 服务先通过高德地理编码API将地址转为坐标，再执行周边搜索

**Given** 开发者查看 `HospitalService.java`
**When** 检查坐标处理逻辑
**Then** 代码注释说明使用 GCJ-02 坐标系（国内高德地图标准，非 WGS-84）

**Given** 高德 API 调用失败
**When** `HospitalService` 捕获异常
**Then** 返回空列表并记录 `WARN` 日志，不向上抛出异常

**覆盖 FRs：** FR9, FR10, FR11, FR14 | **覆盖 NFRs：** NFR3, NFR11, NFR21 | **覆盖 ARs：** AR11

---

### Story 3.2：医院查询 API 端点（HospitalController）

As a frontend developer,
I want a REST endpoint that accepts location and department, and returns a sorted hospital list,
So that the frontend can display nearby hospitals after AI analysis completes.

**Acceptance Criteria:**

**Given** 发送 `POST /api/hospitals`，请求体包含 `{ "latitude": 39.9042, "longitude": 116.4074, "department": "外科" }`
**When** 查询成功
**Then** 返回 `200 OK`，响应体为医院数组，每项含 `name`、`distance`、`address`、`departments`
**And** 数组按 `distance` 升序排列

**Given** 发送 `POST /api/hospitals`，请求体包含 `{ "address": "北京市朝阳区建国路", "department": "外科" }`
**When** 查询成功
**Then** 返回 `200 OK`，以地址为中心的医院列表（与坐标模式结果格式相同）

**Given** 请求体中 `latitude`/`longitude` 和 `address` 均未提供
**When** 入参校验失败
**Then** 返回 `400`，响应体为 `{ "code": "LOCATION_REQUIRED", "message": "请提供位置坐标或地址", "fallback": false }`

**Given** 高德 API 不可用
**When** 请求 `POST /api/hospitals`
**Then** 返回 `503`，响应体为 `{ "code": "MAP_ERROR", "message": "医院查询暂时不可用，请稍后重试", "fallback": true }`

**覆盖 FRs：** FR9, FR10, FR11, FR12, FR13, FR14 | **覆盖 NFRs：** NFR12, NFR22

---

### Story 3.3：前端位置获取（GPS 自动定位 + 手动地址 Fallback）

As a patient,
I want the app to automatically detect my location, with a manual address fallback if GPS fails,
So that I can always provide a location for the hospital search regardless of GPS availability.

**Acceptance Criteria:**

**Given** 患者首次使用应用
**When** 进入首页
**Then** 页面显示位置授权说明文字（告知位置数据仅用于查找附近医院）
**And** 触发浏览器 Geolocation 授权弹窗

**Given** 患者授权GPS定位
**When** 定位成功
**Then** `locationStore.latitude` 和 `locationStore.longitude` 存储坐标值
**And** 页面显示"定位成功"状态提示

**Given** 患者拒绝GPS授权或定位失败
**When** Geolocation API 返回错误
**Then** 自动切换为手动模式，显示 `LocationInput` 地址输入框
**And** 页面提示"无法自动定位，请手动输入地址"

**Given** 患者在手动模式下输入地址并提交
**When** 地址输入框提交
**Then** `locationStore.address` 存储输入的地址字符串，定位模式标记为 `manual`

**覆盖 FRs：** FR7, FR8 | **覆盖 NFRs：** NFR8

---

### Story 3.4：前端医院列表展示

As a patient,
I want to see a list of nearby hospitals with their distance, address, and relevant departments,
So that I can choose the most suitable hospital to visit.

**Acceptance Criteria:**

**Given** 症状分析和位置均已就绪
**When** 前端调用 `hospitalApi.findNearby()` 并收到响应
**Then** `HospitalList` 组件展示医院卡片列表，按距离升序排列

**Given** 医院列表成功加载
**When** 每张 `HospitalCard` 渲染
**Then** 显示医院名称、距离（如"800m"）、地址
**And** 高亮显示与 AI 推荐科室匹配的科室名称（FR13）

**Given** 高德 API 返回至少3家医院
**When** 列表渲染
**Then** 用户可看到至少3张医院卡片（FR14）

**Given** 用户点击某家医院卡片上的导航按钮
**When** 点击事件触发
**Then** 以新标签页打开高德地图导航链接（FR17）

**Given** 医院查询返回空列表
**When** 列表渲染
**Then** 显示"附近未找到医院，建议拨打120"提示，不显示空白

**覆盖 FRs：** FR10, FR11, FR12, FR13, FR14, FR17 | **覆盖 NFRs：** NFR13

---

## Epic 4：完整就医流程与合规展示

患者可查看完整结果页（AI分析+医院列表），含推荐理由、导航链接、免责声明；系统在获取位置前告知用途；数据不持久化；Mobile First响应式UI，满足无障碍标准。

### Story 4.1：完整结果页面与端到端就医查询流程

As a patient,
I want to see both the AI analysis result and the hospital list on a single result page,
So that I can make an informed decision about where to go in one complete view.

**Acceptance Criteria:**

**Given** 患者在首页完成症状输入和位置获取后点击查询
**When** 分析和医院查询均完成
**Then** 页面导航至 `/result`（ResultView），同时显示 AI 分析区域和医院列表区域

**Given** ResultView 已加载
**When** 患者查看页面
**Then** 上方显示 AI 推荐科室和推荐理由（FR16）
**And** 下方显示按距离排序的医院卡片列表

**Given** AI 返回 `isEmergency: true`
**When** ResultView 渲染
**Then** 120急救提示横幅优先展示在页面最顶部，医院列表展示在其下方

**Given** 患者在结果页点击"重新查询"或返回按钮
**When** 导航回首页
**Then** 清空上次查询状态，输入框为空

**覆盖 FRs：** FR15, FR16 | **覆盖 NFRs：** NFR4, NFR5, NFR13

---

### Story 4.2：免责声明、位置告知与数据合规

As a patient,
I want to be clearly informed about how my data is used, and to see a medical disclaimer on every result,
So that I can trust the system handles my information responsibly.

**Acceptance Criteria:**

**Given** 患者首次访问首页
**When** 页面加载完成
**Then** 在触发位置授权弹窗前，页面显示说明文字：位置数据仅用于查找附近医院，不上传至服务器（FR19）

**Given** ResultView 已加载
**When** 患者查看结果页
**Then** 页面显著位置展示 `DisclaimerBanner`，包含文字"本系统建议仅供参考，不构成医疗诊断，请遵医嘱"（FR18）

**Given** 查询完成后
**When** 开发者检查后端日志和数据库（无数据库）
**Then** 确认症状文本、位置坐标均未被持久化存储（FR20）
**And** 后端日志中不含患者症状原文（仅含调用状态日志）

**Given** 开发者检查前端打包产物和代码
**When** 检索 API Key 相关字符串
**Then** 前端代码中不含 DASHSCOPE_API_KEY 或 AMAP_API_KEY 的实际值（FR21）

**覆盖 FRs：** FR18, FR19, FR20, FR21 | **覆盖 NFRs：** NFR6, NFR8

---

### Story 4.3：Mobile First 响应式 UI 与无障碍合规

As a patient (especially elderly or mobile users),
I want the app to be fully usable on a 375px mobile screen with accessible UI elements,
So that I can easily use the service under stress without needing desktop access.

**Acceptance Criteria:**

**Given** 使用375px宽度视口（iPhone SE）访问首页和结果页
**When** 页面渲染
**Then** 所有核心交互区（输入框、查询按钮、医院卡片）完整可见，无需横向滚动（NFR1）

**Given** 患者使用触控设备操作
**When** 点击查询按钮或医院卡片
**Then** 所有可点击元素的触控区域 ≥ 44×44px（NFR19）

**Given** 使用色觉辅助工具检查页面
**When** 检查文字对比度
**Then** 正文对比度 ≥ 4.5:1，大字（≥18px）对比度 ≥ 3:1（NFR16）

**Given** 屏幕阅读器（或无障碍检查工具）扫描页面
**When** 检查表单元素
**Then** 症状输入框具备 `aria-label` 或关联 `<label>`（NFR18）
**And** 急症警告同时包含图标和文字，不依赖颜色单一传达危险信息（NFR17）

**Given** 页面首次加载（4G网络条件模拟）
**When** 测量 FCP
**Then** 首屏内容渲染时间 < 2秒（NFR1，Vue打包 gzip < 500KB）

**覆盖 FRs：** FR15, FR17 | **覆盖 NFRs：** NFR1, NFR9, NFR13, NFR15, NFR16, NFR17, NFR18, NFR19

---

## Epic 5：可观测性与运维加固

系统记录AI和地图接口调用日志；所有异常返回可识别的结构化错误响应；API Key安全隔离，不暴露至前端。

### Story 5.1：AI 与地图接口调用日志

As an operator,
I want structured logs for every AI and map API call including status and response time,
So that I can diagnose issues and monitor external service reliability.

**Acceptance Criteria:**

**Given** 后端处理一次完整的 `/api/analyze` 请求
**When** 请求完成（成功或失败）
**Then** 日志中包含一条包含以下字段的记录：调用服务名（DashScope）、请求耗时（ms）、响应状态（成功/超时/错误）

**Given** 后端处理一次完整的 `/api/hospitals` 请求
**When** 请求完成
**Then** 日志中包含高德API调用记录：调用服务名（AMap）、请求耗时（ms）、返回医院数量

**Given** 通义千问 API 在1分钟内失败超过3次
**When** 失败计数触发阈值
**Then** 记录 `WARN` 级别告警日志，内容包含失败次数和时间窗口（NFR20）

**Given** 运维人员查看日志
**When** 检索关键词
**Then** 日志中不含患者症状原文或位置坐标（隐私保护）

**覆盖 FRs：** FR23 | **覆盖 NFRs：** NFR20

---

### Story 5.2：全局异常处理与结构化错误响应

As a patient,
I want to receive a clear, actionable error message when something goes wrong,
So that I am never left with a blank page or a cryptic error, and I know what to do next.

**Acceptance Criteria:**

**Given** 任意后端接口发生未预期的运行时异常
**When** 异常到达 `GlobalExceptionHandler`
**Then** 返回 `500`，响应体为 `{ "code": "INTERNAL_ERROR", "message": "系统出现问题，请稍后重试", "fallback": false }`
**And** 服务继续运行，不崩溃（NFR11）

**Given** 前端 Axios 拦截器收到任意 4xx/5xx 响应
**When** 拦截器处理错误
**Then** 将错误信息写入 `queryStore.error`，组件根据 `error.code` 渲染对应提示
**And** 用户界面不显示空白页或未处理的 JS 报错（NFR13）

**Given** 开发者执行 `./gradlew test`
**When** 所有测试完成
**Then** Controller 层集成测试覆盖以下场景：400入参错误、AI超时降级、地图不可用降级

**覆盖 FRs：** FR24 | **覆盖 NFRs：** NFR11, NFR12, NFR13

