<div align="center">
  <h1>Portal 后端（portal-service）</h1>
</div>

企业门户平台后端，采用 `Java 21`、`Spring Boot 4`、`Maven` 多模块单体架构。系统管理、认证授权（OAuth2）等构成平台底座.业务能力以独立模块的形式接入,全平台基于多租户实现数据隔离。

## 仓库地址

- 后端 portal-service（本仓库）：<https://github.com/csx-bill/portal-service>
- 前端 portal-ui：<https://github.com/csx-bill/portal-ui>

## 技术栈

| 类别 | 选型 |
| --- | --- |
| 基础框架 | Java 21、Spring Boot 4.0.6、Maven 多模块 |
| 认证授权 | Sa-Token（登录态）+ Sa-Token OAuth2（授权服务） |
| 持久层 | MyBatis-Plus（多租户/数据权限拦截器）、MySQL 8、p6spy（SQL 打印） |
| 缓存 | Redis（Sa-Token 会话、企微凭据存储） |
| 接口文档 | Knife4j（OpenAPI3，中文文档） |
| 企微集成 | WxJava（`wx-java-cp-multi`，多应用账号） |
| 文件存储 | 本地存储 / S3（兼容 MinIO、AWS S3、腾讯 COS、阿里 OSS） |
| 工具库 | Hutool、Lombok、内置分布式调度引擎 |

## 模块结构

```
portal-service
├── portal-common                  # 公共聚合模块
│   ├── portal-common-bom          # 全局依赖版本控制（BOM）
│   └── portal-common-core         # 公共核心：统一响应/异常、多租户、数据权限、文件存储、调度引擎、安全组件
├── portal-auth                    # 认证授权中心：Sa-Token + OAuth2 授权服务、OAuth 客户端管理
├── portal-system                  # 系统模块
│   ├── portal-system-api          # 系统 API（对外 DTO 与接口契约，供其他模块依赖）
│   └── portal-system-biz          # 系统业务实现
├── portal-scrm                    # 业务模块：企业微信 SCRM
└── portal-boot                    # 单体启动模块：聚合全部业务模块，唯一可运行入口
```

依赖方向：`portal-boot → portal-auth / portal-system-biz / portal-scrm → portal-system-api / portal-common-core`。

## 核心功能

### 认证授权（portal-auth）

- Sa-Token 登录态：`Authorization: Bearer <token>`，token 有效期 30 天，支持多端并发登录（见 `sa-token` 配置）。
- 内置 OAuth2 授权服务：`/oauth2/authorize`、`/oauth2/token`、授权确认页，自定义密码模式授权处理器。
- OAuth 客户端管理接口，客户端信息存于 `sys_oauth_client` 表。

### 系统管理（portal-system）

用户、角色、菜单、部门、租户、字典、文件（元数据 + 文件分组）、国际化文案（中文默认 + `en_US`，消息资源基名 `system-messages`）。

### 企业微信 SCRM（portal-scrm）

通讯录、客户、客户群管理，群发助手、朋友圈、欢迎语、获客链接、客户标签、聊天敏感词、客户继承（在职/离职转接）、工作台看板、企微回调事件处理。

- 支持 `SUITE`（服务商代开发）/ `SELF`（企业自建应用）两种模式，由 `xqt.wx.mode` 一键切换。
- 内置定时同步任务：通讯录、客户、客户群、获客链接全量同步、转接对账等。

### 平台能力（portal-common-core）

- **多租户**：MyBatis-Plus 行级隔离，请求头 `X-Tenant-Id`，租户表清单在 `application.yml` 的 `tenant.tables` 中维护。
- **数据权限**：DataScope 拦截器体系，按部门/本人等范围过滤数据。
- **客户端隔离**：请求头 `Client-Id`，配合 Sa-Token 权限校验。
- **文件存储**：`file.upload-type` 切换 `local` / `s3`；local 模式生产环境可由 Nginx 直服（样例见 `deploy/nginx-upload.conf.sample`）。
- **调度引擎**：基于 `scheduler_task` 表的轻量调度，支持立即执行、一次性、每天/每周/每月、Cron 表达式。
- **通用组件**：统一响应 `R`、全局异常处理、Bean 校验、Jackson 序列化（Long 转字符串防精度丢失）、CORS 等。

## 快速开始

### 环境要求

- JDK 21+
- Maven 3.8+
- MySQL 8.x
- Redis

### 1. 初始化数据库

创建名为 `portal-service` 的数据库，导入 [`deploy/portal-service.sql`](deploy/portal-service.sql)（43 张表 + 初始数据，内置账号 `admin`，密码为 BCrypt 密文存储）。

### 2. 调整配置

编辑 [`portal-boot/src/main/resources/application.yml`](portal-boot/src/main/resources/application.yml)：

- 数据源与 Redis 连接（默认 `127.0.0.1`）。
- 企微配置（`wx.cp.*`）与模式开关 `xqt.wx.mode`，按需填写。
- 文件存储 `file.upload-type`：本地开发用 `local`（落盘 `./data/files`）。

### 3. 构建与启动

```bash
mvn clean package -DskipTests

java -jar portal-boot/target/portal-boot-4.1.0.jar
# 或在 IDE 中直接运行 com.bill.portal.PortalBootApplication
```

### 4. 访问

| 入口 | 地址 |
| --- | --- |
| 服务 | http://localhost:8080 |
| 接口文档（Knife4j） | http://localhost:8080/doc.html |

## 请求头约定

| Header | 说明 |
| --- | --- |
| `Authorization` | 登录令牌，形如 `Bearer <token>` |
| `X-Tenant-Id` | 租户 ID（多租户隔离） |
| `Client-Id` | OAuth 客户端标识 |

免认证路径在 `security.ignore-urls` 配置，当前为 `/oauth2/**`（登录与 OAuth2 入口）与 `/scrm/cp/self/**`（企微回调，腾讯服务器匿名调用）。

## 部署

- 数据库脚本：[`deploy/portal-service.sql`](deploy/portal-service.sql)。
- 文件直服：local 存储的生产形态参考 [`deploy/nginx-upload.conf.sample`](deploy/nginx-upload.conf.sample)，通过 `file` 配置的 public-base-url 切换「应用转发 / Nginx 直连」两种形态。
- API 文档默认开启（`knife4j.production: false`），生产环境建议置为 `true` 关闭文档入口。
