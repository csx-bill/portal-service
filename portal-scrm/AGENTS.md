# AGENTS.md — portal-scrm

本模块是 portal 平台的企业微信 SCRM（Social Customer Relationship Management）业务模块。

## 架构与关键链路

### 企微双模式（SELF / SUITE）

`xqt.wx.mode` 决定接入方式：`SELF`（企业自建应用）/ `SUITE`（服务商），对应配置段 `wx.cp.corps` / `wx.cp.tp.corps`（多企业 map，key 为应用名，首个 key 固定 `xqt-scrm`）。

- 模式相关 Bean 必须标注 `@ConditionalOnSelfMode` / `@ConditionalOnSuiteMode`（见 `core/annotation`），如回调策略、监听器、`corp/initializer/SelfCorpAuthInitializer`。
- `src/main/java/com/binarywang/...` 下的两个 AutoConfiguration **故意用 starter 原包名覆写**，用于支持多企业 map 配置，不要移动包名或删除。

### 企微回调链路

```
CallbackController(/scrm/cp/self/callback/{app})
  → WxCallbackStrategy（Self/Suite 两实现，WxJava 解密验签，先同步返回 "success"）
  → ApplicationEventPublisher 发布 Self/SuiteWxCallbackPayload
  → @Async @EventListener（Self/SuiteWxCallbackEventListener，EventType.isHandleEvent 过滤）
  → WxEventDispatcher（EventType → WxEventHandler 枚举表）
  → ChangeExternalContact/Chat/Tag、ChangeContact 等 Handler
  → 各域 SyncService 刷新本地快照表
```

新增企微事件处理：扩展 `common/enums/EventType`（含 `isHandleEvent` 白名单）→ 新建 `WxEventHandler` 实现 → 在 `WxEventDispatcher` 构造器注册映射。

### 数据同步模型

企微数据全部落为**本地快照表**（`wx_customer`、`wx_user`、`wx_department`、`wx_group_chat` 等），不依赖实时调 API 查询：

- 同步类型见 `SyncType`（`*_FULL` / `*_INCREMENT`），触发来源见 `TriggerType`（MANUAL 控制器 / CRON job / 事件回调）。
- 版本号由 `ISyncVersionService` 记录在 `wx_sync_version`，按企业全局递增（不分同步类型，全量/增量版本可比）。
- 写入统一走 Mapper XML 的 `batchUpsert`（`src/main/resources/mapper/<域>/`），本地扩展字段（如交接标记）不在 upsert 列中，同步不覆盖。
- SDK 调用与落库收口在各域 `IWx*SyncService`；Controller 与 Handler 不直接持有 `WxCpService` 调企微 API（回调策略解密、API 封装层除外）。

### 定时任务

`<域>/job/` 下 `@Component` + `@Scheduled(cron)`。模式固定：遍历 `IWxCorpAuthService.getAuthorizedCorps()` 逐企业执行，**每个企业必须包在 `TenantBroker.runAs(tenantId, ...)` 中设置租户上下文**，单企业异常捕获不中断整体（参考 `contact/job/WxContactFullSyncJob`）。

### 多租户与数据权限

- 行级多租户基于 MyBatis-Plus 插件，**租户表白名单在 portal-boot 的 `application.yml` `tenant.tables` 中维护**。新增任何 `wx_*` 表必须登记，否则该表查询不会自动追加 `tenant_id` 条件。
- 数据权限：实体标注 `@DataScopeOwner(column = "user_id")` 即纳入管辖（`datascope/ScrmDataScopeHandler` 自动按 TableInfo 解析，加表无需改 Handler）。链路：sys 角色 data_scope → 可见系统账号 → `wx_user_binding` 反推可见企微成员 → SQL 改写 `归属列 IN (...)`；未绑定成员的数据仅"全部"权限可见。

## 目录结构与编码约定

包根 `com.bill.portal.scrm`，按业务域分包，域内固定分层：

```
<domain>/
  controller/   # @RestController，路由 /scrm/<domain>，返回统一 R<T>
  service/      # 接口带 I 前缀（IWxCustomerService），同步类接口常单独 IWx*SyncService
  service/impl/ # 继承 MyBatis-Plus ServiceImpl<Mapper, Entity>
  mapper/       # MP BaseMapper；复杂 SQL（batchUpsert/统计）写 XML
  entity/       # @Data + @TableName(autoResultMap=true) + 继承 BaseEntity + @TableId(ASSIGN_ID)
  dto/ req/ vo/ # 请求/响应对象，字段加 @Schema
  job/          # 定时同步任务（见上）
```

业务域一览：`acquisition` 获客链接、`callback` 回调、`common` 同步版本/媒体上传、`contact` 通讯录（部门/成员）、`corp` 企业授权、`customer` 客户、`dashboard` 仪表盘、`datascope` 数据权限、`groupchat` 客户群、`inheritance` 在职继承（客户/群转接）、`intercept` 拦截规则、`mass` 群发、`moment` 朋友圈、`tag` 企业标签、`userbinding` 系统账号↔企微成员绑定、`welcome` 欢迎语、`core` 模式注解/常量。

具体写法（与现有代码保持一致）：

- Controller 用 `@AllArgsConstructor` + `private final` 构造注入；job/少量旧类用 `@Autowired` 字段注入。查询接口用 `@ParameterObject` 接收分页与查询 DTO，校验用 `@Valid`。
- 权限注解 `@SaCheckPermission("<domain>:<action>")`（如 `customer:markTag`）；企微回调等公开接口加 `@SaIgnore`。
- 注释、Javadoc、日志均为中文；枚举/常量集中在 `common/enums`、`common/constant`、`core/constant`。
- 简单查询用 `Wrappers.lambdaQuery()` 条件构造（条件式参数：`eq(StringUtils.hasText(...), ...)`），JSON 快照字段检索可用 `JSON_OVERLAPS(JSON_EXTRACT(extra_data, ...))`（参考 `WxCustomerServiceImpl`）。
- 乐观并发用 `syncVersion` 字段；企微原始报文整体存 `extraData`（`Jackson3TypeHandler` + `autoResultMap`）。

## 新增功能检查清单

新增一张 `wx_*` 表时逐项确认：

1. portal-boot `application.yml` → `tenant.tables` 登记；
2. 实体继承 `BaseEntity`、正确标注 `@TableName`/`@TableId`，需数据权限则加 `@DataScopeOwner`；
3. Mapper XML 放 `src/main/resources/mapper/<域>/`，namespace 与 Mapper 接口全限定名一致；
4. 涉及企微数据则补 `SyncType` 枚举 + SyncService + 必要的 Job/回调 Handler。

新增需要区分 SELF/SUITE 的 Bean 时：必须加 `@ConditionalOnSelfMode` / `@ConditionalOnSuiteMode`，不要用 `@Profile` 或直接读配置值判断。
