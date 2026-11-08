# web-scrm

**新企通 SCRM · 企业微信客户运营平台** —— portal 平台面向企业微信客户运营的业务子系统（clientId：`web-scrm`）。

提供获客、客户运营、群运营、内容触达与风险管控等一站式能力，围绕企业微信（WeCom）展开。

## 目录说明

> **本目录**（`portal-service/data/files/local/web-scrm/`）是 web-scrm 客户端的**本地文件存储桶**，
> 存放该应用上传的文件。上传接口下发的访问地址形如
> `/{bucket}/{clientId}/{dir}/{fileName}`（桶名默认 `local`，见后端 `FileProperties.bucketName`）：
>
> - 本地开发：`/local` 前缀由后端应用直接服务（`LocalFileResourceConfig`），无 nginx；
> - 生产环境：该前缀由 nginx 截走，不进应用。
>
> 源码与配置不在此目录，见下方「代码位置」。

## 代码位置

| 部分 | 路径 |
| --- | --- |
| 前端应用 | `portal-ui/apps/web-scrm` |
| 后端模块 | `portal-service/portal-scrm` |
| 公共远程模块（登录、用户、菜单等） | `portal-ui/apps/system-common`（模块联邦 Remote） |
| 后端菜单/权限增量 SQL | `portal-service/deploy/sql/` |

## 功能模块

| 模块 | 前端视图 | 后端包 | 说明 |
| --- | --- | --- | --- |
| 工作台 | `dashboard` | `dashboard` | 客户资产与运营效果总览，数据实时统计 |
| 企业管理 | `corp` | `corp` | 企业微信授权/认证管理 |
| 通讯录 | `contact` | `contact`、`userbinding` | 企微成员通讯录；平台用户与企微成员绑定 |
| 客户管理 | `customer` | `customer` | 企微客户资产管理 |
| 获客链接 | `acquisition` | `acquisition` | 创建/编辑/删除获客链接，客户通过链接添加成员，支持定时同步任务 |
| 客户标签 | `tag` | `tag` | 企业标签管理 |
| 客户群发 | `mass` | `mass` | 客户群发任务 |
| 客群列表 | `groupchat` | `groupchat` | 客户群管理 |
| 朋友圈任务 | `moment` | `moment` | 朋友圈发表任务 |
| 欢迎语 | `welcome` | `welcome` | 添加客户欢迎语 |
| 客户继承 | `inheritance` | `inheritance` | 离职/转接客户继承 |
| 聊天敏感词 | `intercept` | `intercept` | 会话风险管控规则 |
| 企微回调 | — | `callback` | 企微事件回调：自建应用与服务商双策略，事件经 `WxEventDispatcher` 分发到各 Handler |

## 技术栈

**前端**（`portal-ui`，vben-admin v5 monorepo，pnpm + turbo）

- Vue 3 + TypeScript + Vite
- UI：`antdv-next`、`vxe-table` / `vxe-pc-ui`；状态：Pinia；国际化：zh-CN / en-US
- 模块联邦（`@originjs/vite-plugin-federation`）：web-scrm 作为**宿主（Host）**，
  消费 `system-common` 的 `remoteEntry.js`；shared 名单派生自 system-common 的
  `package.json`（单一事实来源），`antdv-next` / `pinia` / `vue` / `vue-router` 单实例运行
- 页面样式与交互约定见 `apps/web-scrm/AGENTS.md`

**后端**（`portal-service`，Maven 多模块）

- Java 21 + Spring Boot 4.0.6
- 企微 SDK：WxJava（`wx-java-cp-multi` + `wx-java-cp-tp-multi`，多应用 + 服务商多租户）
- 权限码约定:跨客户端公共功能用 `<clientId>:<action>`(如 `web-scrm:user:add`);
  web-scrm 自有业务功能用裸码 `<module>:<action>`(如 `mass:update`),前端按钮
  用 `v-access:code`。均存于 `sys_menu.auth_code`,细则见前端仓库
  `apps/web-scrm/AGENTS.md`

## 本地开发

端口约定：web-scrm `:5000`，system-common `:3200`，后端 `:8080`。

```bash
# 1. 启动后端（portal-service）
#    application.yml 见 portal-service/portal-boot

# 2. 启动 system-common（Remote，web-scrm 依赖其登录/菜单等页面）
cd portal-ui/apps/system-common
pnpm dev

# 3. 启动 web-scrm（Host）
cd portal-ui/apps/web-scrm
pnpm dev        # http://localhost:5000
pnpm typecheck  # vue-tsc 校验
pnpm build      # 生产构建
```

开发代理（`vite.config.ts`）：

- `/api` → `http://localhost:8080`（去前缀重写）
- `/local` → `http://localhost:8080`（本目录对应的文件访问前缀）

相关环境变量（`.env` / `.env.development`）：`VITE_APP_CLIENT_ID=web-scrm`、
`VITE_SYSTEM_COMMON_REMOTE_URL`（默认 `http://localhost:3200/remoteEntry.js`，生产为同域
`/system-common/remoteEntry.js`）。
