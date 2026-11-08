/*
 Navicat Premium Dump SQL

 Source Server         : localhost-mysql8-3306
 Source Server Type    : MySQL
 Source Server Version : 80042 (8.0.42)
 Source Host           : localhost:3306
 Source Schema         : portal-service

 Target Server Type    : MySQL
 Target Server Version : 80042 (8.0.42)
 File Encoding         : 65001

 Date: 08/11/2026 21:32:45
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for scheduler_task
-- ----------------------------
DROP TABLE IF EXISTS `scheduler_task`;
CREATE TABLE `scheduler_task`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `task_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '任务名称',
  `handler_class` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '执行器全限定类名（路由到对应 TaskExecutor Bean）',
  `execute_type` tinyint NOT NULL COMMENT '调度类型：0-立即 1-仅一次 2-每天 3-每周 4-每月 5-Cron',
  `cron_expression` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'Cron表达式（execute_type=5时必填）',
  `scheduled_time` datetime NOT NULL COMMENT '下次执行时间',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：1-启用 2-执行中 3-已完成 4-禁用',
  `task_data` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '业务参数（JSON格式，透传给执行器）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '修改人',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` tinyint(1) NULL DEFAULT 0 COMMENT '删除标记,1:已删除,0:正常',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_status_time`(`status` ASC, `scheduled_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '调度任务表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of scheduler_task
-- ----------------------------

-- ----------------------------
-- Table structure for sys_dept
-- ----------------------------
DROP TABLE IF EXISTS `sys_dept`;
CREATE TABLE `sys_dept`  (
  `id` bigint NOT NULL COMMENT '主键ID',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '部门名称',
  `sort_order` int NULL DEFAULT 0 COMMENT '排序',
  `parent_id` bigint NULL DEFAULT NULL COMMENT '父级部门ID',
  `tenant_id` bigint NULL DEFAULT NULL COMMENT '租户ID',
  `del_flag` tinyint(1) NULL DEFAULT 0 COMMENT '删除标记,1:已删除,0:正常',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '修改人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统部门表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_dept
-- ----------------------------

-- ----------------------------
-- Table structure for sys_dict
-- ----------------------------
DROP TABLE IF EXISTS `sys_dict`;
CREATE TABLE `sys_dict`  (
  `id` bigint NOT NULL COMMENT '编号',
  `dict_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '字典类型',
  `description` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '描述',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `remarks` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注信息',
  `system_flag` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '系统标志',
  `del_flag` tinyint NULL DEFAULT 0 COMMENT '删除标志',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '所属租户',
  `client_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '客户端ID',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `sys_dict_del_flag`(`del_flag` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '字典表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_dict
-- ----------------------------
INSERT INTO `sys_dict` VALUES (9000000000000020001, 'customer_channel', '客户渠道', NULL, NULL, '2026-10-24 04:20:08', '2026-10-24 04:20:08', NULL, '0', 0, 1, 'web-scrm');

-- ----------------------------
-- Table structure for sys_dict_item
-- ----------------------------
DROP TABLE IF EXISTS `sys_dict_item`;
CREATE TABLE `sys_dict_item`  (
  `id` bigint NOT NULL COMMENT '编号',
  `dict_id` bigint NOT NULL COMMENT '字典ID',
  `item_value` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '字典项值',
  `label` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '字典项名称',
  `dict_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '字典类型',
  `description` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '字典项描述',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '排序（升序）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `remarks` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注信息',
  `del_flag` tinyint NULL DEFAULT 0 COMMENT '删除标志',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '所属租户',
  `client_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '客户端ID',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `sys_dict_value`(`item_value` ASC) USING BTREE,
  INDEX `sys_dict_label`(`label` ASC) USING BTREE,
  INDEX `sys_dict_item_del_flag`(`del_flag` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '字典项' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_dict_item
-- ----------------------------

-- ----------------------------
-- Table structure for sys_file
-- ----------------------------
DROP TABLE IF EXISTS `sys_file`;
CREATE TABLE `sys_file`  (
  `id` bigint NOT NULL COMMENT '编号',
  `group_id` bigint NULL DEFAULT NULL COMMENT '文件组',
  `file_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '文件名',
  `bucket_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '文件存储桶名称',
  `dir` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '文件夹名称',
  `original` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '原始文件名',
  `type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '文件类型',
  `file_size` bigint NULL DEFAULT NULL COMMENT '文件大小',
  `url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '文件访问地址',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '创建人',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '修改人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '上传时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `del_flag` tinyint NULL DEFAULT 0 COMMENT '删除标志',
  `tenant_id` bigint NULL DEFAULT NULL COMMENT '租户ID',
  `client_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '客户端ID',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '文件管理表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_file
-- ----------------------------

-- ----------------------------
-- Table structure for sys_file_group
-- ----------------------------
DROP TABLE IF EXISTS `sys_file_group`;
CREATE TABLE `sys_file_group`  (
  `id` bigint UNSIGNED NOT NULL COMMENT '主键ID',
  `name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '分类名称',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `del_flag` tinyint NULL DEFAULT 0 COMMENT '删除标记',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '修改人',
  `tenant_id` bigint NULL DEFAULT NULL COMMENT '租户ID',
  `parent_id` bigint NULL DEFAULT NULL COMMENT '父ID',
  `client_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '客户端ID',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '文件分类表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_file_group
-- ----------------------------

-- ----------------------------
-- Table structure for sys_i18n
-- ----------------------------
DROP TABLE IF EXISTS `sys_i18n`;
CREATE TABLE `sys_i18n`  (
  `id` bigint NOT NULL COMMENT '主键ID',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'name',
  `zh` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '中文',
  `en` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '英文',
  `client_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '客户端ID',
  `del_flag` tinyint(1) NULL DEFAULT 0 COMMENT '删除标记,1:已删除,0:正常',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '修改人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统国际化表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_i18n
-- ----------------------------

-- ----------------------------
-- Table structure for sys_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_menu`;
CREATE TABLE `sys_menu`  (
  `id` bigint NOT NULL COMMENT '主键ID',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '角色名称',
  `auth_code` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '权限标识',
  `path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '路由路径',
  `component` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '组件',
  `parent_id` bigint NULL DEFAULT 0 COMMENT '父菜单ID',
  `sort_order` int NULL DEFAULT NULL COMMENT '排序值，越小越靠前',
  `status` tinyint(1) NULL DEFAULT NULL COMMENT '状态: 1=启用，0=禁用',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注信息',
  `meta` json NULL COMMENT '元数据（JSON）',
  `menu_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '菜单类型: catalog=目录, menu=菜单, button=按钮, embedded=内嵌, link=外链',
  `active_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '激活路径（用于菜单高亮）',
  `client_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '客户端ID',
  `del_flag` tinyint(1) NULL DEFAULT 0 COMMENT '删除标记,1:已删除,0:正常',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '修改人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统菜单表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_menu
-- ----------------------------
INSERT INTO `sys_menu` VALUES (1900000000000000001, 'Corp', NULL, '/corp', '/corp/auth/index', 0, 1, 1, NULL, '{\"icon\": \"lucide:building-2\", \"title\": \"corp.title\"}', 'menu', NULL, 'web-scrm', 0, NULL, NULL, '2026-10-11 07:00:38', '2026-10-11 07:18:55');
INSERT INTO `sys_menu` VALUES (1910000000000000001, 'System', NULL, '/system', NULL, 0, 5, 1, NULL, '{\"icon\": \"lucide:settings\", \"title\": \"systemCommon.system.title\"}', 'catalog', NULL, 'web-scrm', 0, NULL, NULL, '2026-10-11 07:05:49', '2026-10-11 11:41:12');
INSERT INTO `sys_menu` VALUES (1910000000000000002, 'Menu', NULL, '/system/menu', '/system_common/MenuManagePage', 1910000000000000001, 0, 1, NULL, '{\"icon\": \"lucide:menu\", \"title\": \"systemCommon.system.menu.title\"}', 'menu', NULL, 'web-scrm', 0, NULL, NULL, '2026-10-11 07:05:49', NULL);
INSERT INTO `sys_menu` VALUES (1910000000000000003, 'Role', NULL, '/system/role', '/system_common/RoleManagePage', 1910000000000000001, 0, 1, NULL, '{\"icon\": \"mdi:account-group\", \"title\": \"systemCommon.system.role.title\"}', 'menu', NULL, 'web-scrm', 0, NULL, NULL, '2026-10-11 07:05:49', NULL);
INSERT INTO `sys_menu` VALUES (1910000000000000004, 'I18n', NULL, '/system/i18n', '/system_common/I18nManagePage', 1910000000000000001, 0, 1, NULL, '{\"icon\": \"lucide:languages\", \"title\": \"systemCommon.system.i18n.title\"}', 'menu', NULL, 'web-scrm', 0, NULL, NULL, '2026-10-11 07:05:49', NULL);
INSERT INTO `sys_menu` VALUES (1910000000000000005, 'Dict', NULL, '/system/dict', '/system_common/DictManagePage', 1910000000000000001, 0, 1, NULL, '{\"icon\": \"lucide:book-marked\", \"title\": \"systemCommon.system.dict.title\"}', 'menu', NULL, 'web-scrm', 0, NULL, NULL, '2026-10-11 07:05:49', NULL);
INSERT INTO `sys_menu` VALUES (1910000000000000006, 'File', NULL, '/system/file', '/system_common/FileManagePage', 1910000000000000001, 0, 1, NULL, '{\"icon\": \"lucide:folder-open\", \"title\": \"systemCommon.system.file.title\"}', 'menu', NULL, 'web-scrm', 0, NULL, NULL, '2026-10-11 07:05:49', NULL);
INSERT INTO `sys_menu` VALUES (1910000000000000007, 'Contacts', NULL, '/contact', '/contact/index', 0, 2, 1, NULL, '{\"icon\": \"lucide:users\", \"title\": \"contact.title\"}', 'menu', NULL, 'web-scrm', 0, NULL, NULL, '2026-10-11 07:36:10', NULL);
INSERT INTO `sys_menu` VALUES (1910000000000000008, 'Customer', NULL, '/customer', NULL, 0, 3, 1, NULL, '{\"icon\": \"lucide:users-round\", \"title\": \"customer.title\"}', 'catalog', NULL, 'web-scrm', 0, NULL, NULL, '2026-10-11 08:55:09', NULL);
INSERT INTO `sys_menu` VALUES (1910000000000000009, 'CustomerList', NULL, '/customer/list', '/customer/index', 1910000000000000008, 0, 1, NULL, '{\"icon\": \"lucide:list\", \"title\": \"customer.list\"}', 'menu', NULL, 'web-scrm', 0, NULL, NULL, '2026-10-11 08:55:09', NULL);
INSERT INTO `sys_menu` VALUES (1910000000000000010, 'CorpTag', NULL, '/customer/tag', '/tag/index', 1910000000000000008, 1, 1, NULL, '{\"icon\": \"lucide:tags\", \"title\": \"tag.title\"}', 'menu', NULL, 'web-scrm', 0, NULL, NULL, '2026-10-11 10:06:30', NULL);
INSERT INTO `sys_menu` VALUES (1910000000000000011, 'GroupChat', NULL, '/customer/groupchat', '/groupchat/index', 1910000000000000008, 2, 1, NULL, '{\"icon\": \"lucide:messages-square\", \"title\": \"groupchat.title\"}', 'menu', NULL, 'web-scrm', 0, NULL, NULL, '2026-10-11 10:33:11', NULL);
INSERT INTO `sys_menu` VALUES (1910000000000000012, 'Conversion', NULL, '/conversion', NULL, 0, 4, 1, NULL, '{\"icon\": \"lucide:trending-up\", \"title\": \"conversion.title\"}', 'catalog', NULL, 'web-scrm', 0, NULL, NULL, '2026-10-11 11:41:12', NULL);
INSERT INTO `sys_menu` VALUES (1910000000000000013, 'Mass', NULL, '/conversion/mass', '/mass/index', 1910000000000000012, 0, 1, NULL, '{\"icon\": \"lucide:send\", \"title\": \"conversion.mass\"}', 'menu', NULL, 'web-scrm', 0, NULL, NULL, '2026-10-11 11:41:12', NULL);
INSERT INTO `sys_menu` VALUES (1910000000000000014, 'Moment', NULL, '/conversion/moment', '/moment/index', 1910000000000000012, 1, 1, NULL, '{\"icon\": \"lucide:camera\", \"title\": \"conversion.moment\"}', 'menu', NULL, 'web-scrm', 0, NULL, NULL, '2026-10-15 16:38:15', '2026-10-15 16:38:15');
INSERT INTO `sys_menu` VALUES (1910000000000000015, 'Welcome', NULL, '/conversion/welcome', '/welcome/index', 1910000000000000012, 2, 1, NULL, '{\"icon\": \"lucide:message-circle\", \"title\": \"conversion.welcome\"}', 'menu', NULL, 'web-scrm', 0, NULL, NULL, '2026-10-16 14:10:11', '2026-10-16 14:10:11');
INSERT INTO `sys_menu` VALUES (1910000000000000016, 'Inheritance', NULL, '/customer/inheritance', '/inheritance/index', 1910000000000000008, 3, 1, NULL, '{\"icon\": \"lucide:arrow-left-right\", \"title\": \"inheritance.title\"}', 'menu', NULL, 'web-scrm', 0, NULL, NULL, '2026-10-19 14:15:26', NULL);
INSERT INTO `sys_menu` VALUES (1910000000000000017, 'Acquisition', NULL, '/customer/acquisition', '/acquisition/index', 1910000000000000012, 4, 1, NULL, '{\"icon\": \"lucide:user-plus\", \"title\": \"acquisition.title\"}', 'menu', NULL, 'web-scrm', 0, NULL, NULL, '2026-10-21 16:33:31', '2026-10-22 21:45:32');
INSERT INTO `sys_menu` VALUES (1910000000000000030, 'User', NULL, '/system/user', '/system_common/UserManagePage', 1910000000000000001, 2, 1, NULL, '{\"icon\": \"lucide:user-cog\", \"title\": \"systemCommon.system.user.title\"}', 'menu', NULL, 'web-scrm', 0, NULL, NULL, '2026-10-30 13:39:23', NULL);
INSERT INTO `sys_menu` VALUES (1910000000000000040, 'Intercept', NULL, '/conversion/intercept', '/intercept/index', 1910000000000000012, 5, 1, NULL, '{\"icon\": \"lucide:shield-alert\", \"title\": \"intercept.title\"}', 'menu', NULL, 'web-scrm', 0, NULL, NULL, '2026-11-01 04:14:58', NULL);
INSERT INTO `sys_menu` VALUES (1910000000000000070, 'Client', NULL, '/system/client', '/system_common/ClientManagePage', 1910000000000000001, 7, 1, NULL, '{\"icon\": \"lucide:key-round\", \"title\": \"systemCommon.system.client.title\"}', 'menu', NULL, 'web-scrm', 0, NULL, NULL, '2026-11-07 11:36:33', NULL);
INSERT INTO `sys_menu` VALUES (1910000000000000071, 'Tenant', NULL, '/system/tenant', '/system_common/TenantManagePage', 1910000000000000001, 8, 1, NULL, '{\"icon\": \"lucide:building-2\", \"title\": \"systemCommon.system.tenant.title\"}', 'menu', NULL, 'web-scrm', 0, NULL, NULL, '2026-11-07 11:36:33', NULL);
INSERT INTO `sys_menu` VALUES (1910000000000000201, '新增', 'web-scrm:menu:add', NULL, NULL, 1910000000000000002, 1, 1, NULL, NULL, 'button', NULL, 'web-scrm', 0, NULL, NULL, '2026-11-04 13:33:10', NULL);
INSERT INTO `sys_menu` VALUES (1910000000000000202, '修改', 'web-scrm:menu:update', NULL, NULL, 1910000000000000002, 2, 1, NULL, NULL, 'button', NULL, 'web-scrm', 0, NULL, NULL, '2026-11-04 13:33:10', NULL);
INSERT INTO `sys_menu` VALUES (1910000000000000203, '删除', 'web-scrm:menu:delete', NULL, NULL, 1910000000000000002, 3, 1, NULL, NULL, 'button', NULL, 'web-scrm', 0, NULL, NULL, '2026-11-04 13:33:10', NULL);
INSERT INTO `sys_menu` VALUES (1910000000000000211, '新增', 'web-scrm:role:add', NULL, NULL, 1910000000000000003, 1, 1, NULL, NULL, 'button', NULL, 'web-scrm', 0, NULL, NULL, '2026-11-04 13:33:10', NULL);
INSERT INTO `sys_menu` VALUES (1910000000000000212, '修改', 'web-scrm:role:update', NULL, NULL, 1910000000000000003, 2, 1, NULL, NULL, 'button', NULL, 'web-scrm', 0, NULL, NULL, '2026-11-04 13:33:10', NULL);
INSERT INTO `sys_menu` VALUES (1910000000000000213, '删除', 'web-scrm:role:delete', NULL, NULL, 1910000000000000003, 3, 1, NULL, NULL, 'button', NULL, 'web-scrm', 0, NULL, NULL, '2026-11-04 13:33:10', NULL);
INSERT INTO `sys_menu` VALUES (1910000000000000221, '新增', 'web-scrm:i18n:add', NULL, NULL, 1910000000000000004, 1, 1, NULL, NULL, 'button', NULL, 'web-scrm', 0, NULL, NULL, '2026-11-04 13:33:10', NULL);
INSERT INTO `sys_menu` VALUES (1910000000000000222, '修改', 'web-scrm:i18n:update', NULL, NULL, 1910000000000000004, 2, 1, NULL, NULL, 'button', NULL, 'web-scrm', 0, NULL, NULL, '2026-11-04 13:33:10', NULL);
INSERT INTO `sys_menu` VALUES (1910000000000000223, '删除', 'web-scrm:i18n:delete', NULL, NULL, 1910000000000000004, 3, 1, NULL, NULL, 'button', NULL, 'web-scrm', 0, NULL, NULL, '2026-11-04 13:33:10', NULL);
INSERT INTO `sys_menu` VALUES (1910000000000000231, '新增', 'web-scrm:dict:add', NULL, NULL, 1910000000000000005, 1, 1, NULL, NULL, 'button', NULL, 'web-scrm', 0, NULL, NULL, '2026-11-04 13:33:10', NULL);
INSERT INTO `sys_menu` VALUES (1910000000000000232, '修改', 'web-scrm:dict:update', NULL, NULL, 1910000000000000005, 2, 1, NULL, NULL, 'button', NULL, 'web-scrm', 0, NULL, NULL, '2026-11-04 13:33:10', NULL);
INSERT INTO `sys_menu` VALUES (1910000000000000233, '删除', 'web-scrm:dict:delete', NULL, NULL, 1910000000000000005, 3, 1, NULL, NULL, 'button', NULL, 'web-scrm', 0, NULL, NULL, '2026-11-04 13:33:10', NULL);
INSERT INTO `sys_menu` VALUES (1910000000000000241, '上传', 'web-scrm:file:upload', NULL, NULL, 1910000000000000006, 1, 1, NULL, NULL, 'button', NULL, 'web-scrm', 0, NULL, NULL, '2026-11-04 13:33:10', NULL);
INSERT INTO `sys_menu` VALUES (1910000000000000242, '新增', 'web-scrm:file:add', NULL, NULL, 1910000000000000006, 2, 1, NULL, NULL, 'button', NULL, 'web-scrm', 0, NULL, NULL, '2026-11-04 13:33:10', NULL);
INSERT INTO `sys_menu` VALUES (1910000000000000243, '修改', 'web-scrm:file:update', NULL, NULL, 1910000000000000006, 3, 1, NULL, NULL, 'button', NULL, 'web-scrm', 0, NULL, NULL, '2026-11-04 13:33:10', NULL);
INSERT INTO `sys_menu` VALUES (1910000000000000244, '删除', 'web-scrm:file:delete', NULL, NULL, 1910000000000000006, 4, 1, NULL, NULL, 'button', NULL, 'web-scrm', 0, NULL, NULL, '2026-11-04 13:33:10', NULL);
INSERT INTO `sys_menu` VALUES (1910000000000000251, '新增', 'web-scrm:user:add', NULL, NULL, 1910000000000000030, 1, 1, NULL, NULL, 'button', NULL, 'web-scrm', 0, NULL, NULL, '2026-11-04 13:33:10', NULL);
INSERT INTO `sys_menu` VALUES (1910000000000000252, '修改', 'web-scrm:user:update', NULL, NULL, 1910000000000000030, 2, 1, NULL, NULL, 'button', NULL, 'web-scrm', 0, NULL, NULL, '2026-11-04 13:33:10', NULL);
INSERT INTO `sys_menu` VALUES (1910000000000000253, '删除', 'web-scrm:user:delete', NULL, NULL, 1910000000000000030, 3, 1, NULL, NULL, 'button', NULL, 'web-scrm', 0, NULL, NULL, '2026-11-04 13:33:10', NULL);
INSERT INTO `sys_menu` VALUES (1910000000000000261, '新增', 'web-scrm:client:add', NULL, NULL, 1910000000000000070, 1, 1, NULL, NULL, 'button', NULL, 'web-scrm', 0, NULL, NULL, '2026-11-07 11:53:57', NULL);
INSERT INTO `sys_menu` VALUES (1910000000000000262, '修改', 'web-scrm:client:update', NULL, NULL, 1910000000000000070, 2, 1, NULL, NULL, 'button', NULL, 'web-scrm', 0, NULL, NULL, '2026-11-07 11:53:57', NULL);
INSERT INTO `sys_menu` VALUES (1910000000000000263, '删除', 'web-scrm:client:delete', NULL, NULL, 1910000000000000070, 3, 1, NULL, NULL, 'button', NULL, 'web-scrm', 0, NULL, NULL, '2026-11-07 11:53:57', NULL);
INSERT INTO `sys_menu` VALUES (1910000000000000271, '新增', 'web-scrm:tenant:add', NULL, NULL, 1910000000000000071, 1, 1, NULL, NULL, 'button', NULL, 'web-scrm', 0, NULL, NULL, '2026-11-07 11:53:57', NULL);
INSERT INTO `sys_menu` VALUES (1910000000000000272, '修改', 'web-scrm:tenant:update', NULL, NULL, 1910000000000000071, 2, 1, NULL, NULL, 'button', NULL, 'web-scrm', 0, NULL, NULL, '2026-11-07 11:53:57', NULL);
INSERT INTO `sys_menu` VALUES (1910000000000000273, '删除', 'web-scrm:tenant:delete', NULL, NULL, 1910000000000000071, 3, 1, NULL, NULL, 'button', NULL, 'web-scrm', 0, NULL, NULL, '2026-11-07 11:53:57', NULL);
INSERT INTO `sys_menu` VALUES (1910000000000000281, '部门新增', 'web-scrm:dept:add', NULL, NULL, 1910000000000000030, 4, 1, NULL, NULL, 'button', NULL, 'web-scrm', 0, NULL, NULL, '2026-11-07 12:00:48', NULL);
INSERT INTO `sys_menu` VALUES (1910000000000000282, '部门修改', 'web-scrm:dept:update', NULL, NULL, 1910000000000000030, 5, 1, NULL, NULL, 'button', NULL, 'web-scrm', 0, NULL, NULL, '2026-11-07 12:00:48', NULL);
INSERT INTO `sys_menu` VALUES (1910000000000000283, '部门删除', 'web-scrm:dept:delete', NULL, NULL, 1910000000000000030, 6, 1, NULL, NULL, 'button', NULL, 'web-scrm', 0, NULL, NULL, '2026-11-07 12:00:48', NULL);
INSERT INTO `sys_menu` VALUES (1910000000000000301, '同步通讯录', 'contact:sync', NULL, NULL, 1910000000000000007, 1, 1, NULL, NULL, 'button', NULL, 'web-scrm', 0, NULL, NULL, NULL, NULL);
INSERT INTO `sys_menu` VALUES (1910000000000000302, '绑定账号', 'userbinding:bind', NULL, NULL, 1910000000000000007, 2, 1, NULL, NULL, 'button', NULL, 'web-scrm', 0, NULL, NULL, NULL, NULL);
INSERT INTO `sys_menu` VALUES (1910000000000000303, '解绑账号', 'userbinding:unbind', NULL, NULL, 1910000000000000007, 3, 1, NULL, NULL, 'button', NULL, 'web-scrm', 0, NULL, NULL, NULL, NULL);
INSERT INTO `sys_menu` VALUES (1910000000000000311, '同步客户', 'customer:sync', NULL, NULL, 1910000000000000009, 1, 1, NULL, NULL, 'button', NULL, 'web-scrm', 0, NULL, NULL, NULL, NULL);
INSERT INTO `sys_menu` VALUES (1910000000000000312, '编辑标签', 'customer:markTag', NULL, NULL, 1910000000000000009, 2, 1, NULL, NULL, 'button', NULL, 'web-scrm', 0, NULL, NULL, NULL, NULL);
INSERT INTO `sys_menu` VALUES (1910000000000000313, '修改备注', 'customer:remark', NULL, NULL, 1910000000000000009, 3, 1, NULL, NULL, 'button', NULL, 'web-scrm', 0, NULL, NULL, NULL, NULL);
INSERT INTO `sys_menu` VALUES (1910000000000000321, '新增标签组', 'tag:add', NULL, NULL, 1910000000000000010, 1, 1, NULL, NULL, 'button', NULL, 'web-scrm', 0, NULL, NULL, NULL, NULL);
INSERT INTO `sys_menu` VALUES (1910000000000000322, '修改标签组', 'tag:update', NULL, NULL, 1910000000000000010, 2, 1, NULL, NULL, 'button', NULL, 'web-scrm', 0, NULL, NULL, NULL, NULL);
INSERT INTO `sys_menu` VALUES (1910000000000000323, '删除标签组', 'tag:delete', NULL, NULL, 1910000000000000010, 3, 1, NULL, NULL, 'button', NULL, 'web-scrm', 0, NULL, NULL, NULL, NULL);
INSERT INTO `sys_menu` VALUES (1910000000000000324, '同步标签', 'tag:sync', NULL, NULL, 1910000000000000010, 4, 1, NULL, NULL, 'button', NULL, 'web-scrm', 0, NULL, NULL, NULL, NULL);
INSERT INTO `sys_menu` VALUES (1910000000000000331, '同步客群', 'groupchat:sync', NULL, NULL, 1910000000000000011, 1, 1, NULL, NULL, 'button', NULL, 'web-scrm', 0, NULL, NULL, NULL, NULL);
INSERT INTO `sys_menu` VALUES (1910000000000000341, '新增', 'mass:add', NULL, NULL, 1910000000000000013, 1, 1, NULL, NULL, 'button', NULL, 'web-scrm', 0, NULL, NULL, NULL, NULL);
INSERT INTO `sys_menu` VALUES (1910000000000000342, '修改', 'mass:update', NULL, NULL, 1910000000000000013, 2, 1, NULL, NULL, 'button', NULL, 'web-scrm', 0, NULL, NULL, NULL, NULL);
INSERT INTO `sys_menu` VALUES (1910000000000000343, '删除', 'mass:delete', NULL, NULL, 1910000000000000013, 3, 1, NULL, NULL, 'button', NULL, 'web-scrm', 0, NULL, NULL, NULL, NULL);
INSERT INTO `sys_menu` VALUES (1910000000000000344, '同步结果', 'mass:sync', NULL, NULL, 1910000000000000013, 4, 1, NULL, NULL, 'button', NULL, 'web-scrm', 0, NULL, NULL, NULL, NULL);
INSERT INTO `sys_menu` VALUES (1910000000000000351, '新增', 'moment:add', NULL, NULL, 1910000000000000014, 1, 1, NULL, NULL, 'button', NULL, 'web-scrm', 0, NULL, NULL, NULL, NULL);
INSERT INTO `sys_menu` VALUES (1910000000000000352, '修改', 'moment:update', NULL, NULL, 1910000000000000014, 2, 1, NULL, NULL, 'button', NULL, 'web-scrm', 0, NULL, NULL, NULL, NULL);
INSERT INTO `sys_menu` VALUES (1910000000000000353, '删除', 'moment:delete', NULL, NULL, 1910000000000000014, 3, 1, NULL, NULL, 'button', NULL, 'web-scrm', 0, NULL, NULL, NULL, NULL);
INSERT INTO `sys_menu` VALUES (1910000000000000354, '同步结果', 'moment:sync', NULL, NULL, 1910000000000000014, 4, 1, NULL, NULL, 'button', NULL, 'web-scrm', 0, NULL, NULL, NULL, NULL);
INSERT INTO `sys_menu` VALUES (1910000000000000361, '新增', 'welcome:add', NULL, NULL, 1910000000000000015, 1, 1, NULL, NULL, 'button', NULL, 'web-scrm', 0, NULL, NULL, NULL, NULL);
INSERT INTO `sys_menu` VALUES (1910000000000000362, '修改', 'welcome:update', NULL, NULL, 1910000000000000015, 2, 1, NULL, NULL, 'button', NULL, 'web-scrm', 0, NULL, NULL, NULL, NULL);
INSERT INTO `sys_menu` VALUES (1910000000000000363, '删除', 'welcome:delete', NULL, NULL, 1910000000000000015, 3, 1, NULL, NULL, 'button', NULL, 'web-scrm', 0, NULL, NULL, NULL, NULL);
INSERT INTO `sys_menu` VALUES (1910000000000000371, '客户转移', 'inheritance:transfer', NULL, NULL, 1910000000000000016, 1, 1, NULL, NULL, 'button', NULL, 'web-scrm', 0, NULL, NULL, NULL, NULL);
INSERT INTO `sys_menu` VALUES (1910000000000000381, '新增链接', 'acquisition:add', NULL, NULL, 1910000000000000017, 1, 1, NULL, NULL, 'button', NULL, 'web-scrm', 0, NULL, NULL, NULL, NULL);
INSERT INTO `sys_menu` VALUES (1910000000000000382, '修改链接', 'acquisition:update', NULL, NULL, 1910000000000000017, 2, 1, NULL, NULL, 'button', NULL, 'web-scrm', 0, NULL, NULL, NULL, NULL);
INSERT INTO `sys_menu` VALUES (1910000000000000383, '删除链接', 'acquisition:delete', NULL, NULL, 1910000000000000017, 3, 1, NULL, NULL, 'button', NULL, 'web-scrm', 0, NULL, NULL, NULL, NULL);
INSERT INTO `sys_menu` VALUES (1910000000000000384, '同步获客链接', 'acquisition:sync', NULL, NULL, 1910000000000000017, 4, 1, NULL, NULL, 'button', NULL, 'web-scrm', 0, NULL, NULL, NULL, NULL);
INSERT INTO `sys_menu` VALUES (1910000000000000391, '新增规则', 'intercept:add', NULL, NULL, 1910000000000000040, 1, 1, NULL, NULL, 'button', NULL, 'web-scrm', 0, NULL, NULL, NULL, NULL);
INSERT INTO `sys_menu` VALUES (1910000000000000392, '修改规则', 'intercept:update', NULL, NULL, 1910000000000000040, 2, 1, NULL, NULL, 'button', NULL, 'web-scrm', 0, NULL, NULL, NULL, NULL);
INSERT INTO `sys_menu` VALUES (1910000000000000393, '删除规则', 'intercept:delete', NULL, NULL, 1910000000000000040, 3, 1, NULL, NULL, 'button', NULL, 'web-scrm', 0, NULL, NULL, NULL, NULL);

-- ----------------------------
-- Table structure for sys_oauth_client
-- ----------------------------
DROP TABLE IF EXISTS `sys_oauth_client`;
CREATE TABLE `sys_oauth_client`  (
  `id` bigint NOT NULL COMMENT '主键ID',
  `client_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '客户端ID',
  `client_secret` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '客户端秘钥',
  `allow_grant_types` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '授权类型',
  `contract_scopes` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '授权范围',
  `allow_redirect_uris` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '允许授权的所有URL',
  `status` tinyint NULL DEFAULT 0 COMMENT '状态: 1=启用，0=禁用',
  `del_flag` tinyint(1) NOT NULL DEFAULT 0 COMMENT '删除标记,1:已删除,0:正常',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '修改人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '终端信息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_oauth_client
-- ----------------------------
INSERT INTO `sys_oauth_client` VALUES (1, 'web-scrm', 'web-scrm', 'password', NULL, NULL, 1, 0, NULL, NULL, NULL, NULL);

-- ----------------------------
-- Table structure for sys_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role`  (
  `id` bigint NOT NULL COMMENT '主键ID',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '角色名称',
  `status` tinyint(1) NULL DEFAULT NULL COMMENT '状态: 1=启用，0=禁用',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注信息',
  `data_scope` tinyint UNSIGNED NOT NULL DEFAULT 1 COMMENT '数据权限: 1=全部, 2=自定义, 3=本级及子级, 4=本级, 5=本人',
  `client_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '客户端ID',
  `tenant_id` bigint NULL DEFAULT NULL COMMENT '租户ID',
  `del_flag` tinyint(1) NULL DEFAULT 0 COMMENT '删除标记,1:已删除,0:正常',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '修改人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统角色表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_role
-- ----------------------------
INSERT INTO `sys_role` VALUES (1, '超级管理员', 1, '内置超级管理员：拥有全部权限（勿删）', 1, NULL, 1, 0, NULL, NULL, '2026-11-04 14:47:34', NULL);

-- ----------------------------
-- Table structure for sys_role_dept
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_dept`;
CREATE TABLE `sys_role_dept`  (
  `id` bigint NOT NULL COMMENT '主键ID',
  `role_id` bigint NULL DEFAULT NULL COMMENT '角色ID',
  `dept_id` bigint NULL DEFAULT NULL COMMENT '部门ID',
  `tenant_id` bigint NULL DEFAULT NULL COMMENT '租户ID',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '角色-部门关联(自定义数据权限)' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_role_dept
-- ----------------------------

-- ----------------------------
-- Table structure for sys_role_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_menu`;
CREATE TABLE `sys_role_menu`  (
  `id` bigint NOT NULL COMMENT '主键ID',
  `role_id` bigint NULL DEFAULT NULL COMMENT '角色ID',
  `menu_id` bigint NULL DEFAULT NULL COMMENT '菜单ID',
  `tenant_id` bigint NULL DEFAULT NULL COMMENT '租户ID',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统角色菜单关联表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_role_menu
-- ----------------------------

-- ----------------------------
-- Table structure for sys_tenant
-- ----------------------------
DROP TABLE IF EXISTS `sys_tenant`;
CREATE TABLE `sys_tenant`  (
  `id` bigint NOT NULL COMMENT '主键ID',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '租户名称',
  `code` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '租户编码',
  `status` tinyint(1) NULL DEFAULT NULL COMMENT '状态: 1=启用，0=禁用',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注信息',
  `del_flag` tinyint(1) NULL DEFAULT 0 COMMENT '删除标记,1:已删除,0:正常',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '修改人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统租户表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_tenant
-- ----------------------------
INSERT INTO `sys_tenant` VALUES (1, '系统租户', '001', 1, NULL, 0, NULL, NULL, NULL, '2026-08-02 17:32:18');

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `id` bigint NOT NULL COMMENT '主键ID',
  `username` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户名',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '密码',
  `salt` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '盐值',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '姓名',
  `mobile` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '手机号',
  `email` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '邮箱',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '头像',
  `gender` tinyint(1) NULL DEFAULT NULL COMMENT '性别：0-未知 1-男性 2-女性',
  `status` tinyint(1) NULL DEFAULT NULL COMMENT '状态: 1=启用，0=禁用',
  `del_flag` tinyint(1) NULL DEFAULT 0 COMMENT '删除标记,1:已删除,0:正常',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '修改人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统用户表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO `sys_user` VALUES (1, 'admin', '$2a$10$oKVXUe.ZWyEbsjCCLXCOh.wmJfwqWhnwfzZP/1uoOveyvQYLcvqjC', NULL, 'admin', NULL, NULL, NULL, NULL, 1, 0, NULL, NULL, NULL, '2026-11-02 21:25:04');

-- ----------------------------
-- Table structure for sys_user_dept
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_dept`;
CREATE TABLE `sys_user_dept`  (
  `id` bigint NOT NULL COMMENT '主键ID',
  `user_id` bigint NULL DEFAULT NULL COMMENT '用户ID',
  `dept_id` bigint NULL DEFAULT NULL COMMENT '角色ID',
  `tenant_id` bigint NULL DEFAULT NULL COMMENT '租户ID',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统用户部门表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_user_dept
-- ----------------------------

-- ----------------------------
-- Table structure for sys_user_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role`  (
  `id` bigint NOT NULL COMMENT '主键ID',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `user_id` bigint NOT NULL COMMENT '系统用户ID(sys_user.id)',
  `role_id` bigint NOT NULL COMMENT '角色ID(sys_role.id)',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_tenant_user_role`(`tenant_id` ASC, `user_id` ASC, `role_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户-角色关联' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_user_role
-- ----------------------------
INSERT INTO `sys_user_role` VALUES (1, 1, 1, 1, NULL, NULL);

-- ----------------------------
-- Table structure for sys_user_tenant
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_tenant`;
CREATE TABLE `sys_user_tenant`  (
  `id` bigint NOT NULL COMMENT '主键ID',
  `user_id` bigint NULL DEFAULT NULL COMMENT '用户ID',
  `tenant_id` bigint NULL DEFAULT NULL COMMENT '租户ID',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统用户租户关联表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_user_tenant
-- ----------------------------
INSERT INTO `sys_user_tenant` VALUES (2083524353193431042, 1, 1, NULL, '2026-08-01 20:04:50');

-- ----------------------------
-- Table structure for wx_acquisition_link
-- ----------------------------
DROP TABLE IF EXISTS `wx_acquisition_link`;
CREATE TABLE `wx_acquisition_link`  (
  `id` bigint NOT NULL COMMENT '主键ID',
  `corp_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '企业ID',
  `link_id` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '获客链接ID（企微返回）',
  `link_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链接名称',
  `url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链接URL',
  `skip_verify` tinyint(1) NULL DEFAULT 1 COMMENT '是否跳过验证',
  `mark_source` tinyint(1) NULL DEFAULT 1 COMMENT '是否标记来源',
  `customer_channel` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '客户渠道（本地标记，sys_dict customer_channel）',
  `range_user_list` json NULL COMMENT '使用范围-成员ID列表(JSON)',
  `range_department_list` json NULL COMMENT '使用范围-部门ID列表(JSON)',
  `priority_type` int NULL DEFAULT NULL COMMENT '优先级类型: 1-全员优先, 2-指定成员',
  `priority_userid_list` json NULL COMMENT '优先级成员ID列表(JSON)',
  `sync_version` bigint NULL DEFAULT 0 COMMENT '同步版本号',
  `tenant_id` bigint NULL DEFAULT NULL COMMENT '租户ID',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '修改人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `del_flag` int NULL DEFAULT 0 COMMENT '删除标记(0正常/1已删除)',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_link_id`(`link_id` ASC, `corp_id` ASC, `tenant_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '获客链接表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of wx_acquisition_link
-- ----------------------------

-- ----------------------------
-- Table structure for wx_corp_auth
-- ----------------------------
DROP TABLE IF EXISTS `wx_corp_auth`;
CREATE TABLE `wx_corp_auth`  (
  `id` bigint NOT NULL COMMENT '主键ID',
  `corp_id` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '企业ID (企微唯一标识)',
  `corp_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '企业名称',
  `agent_id` bigint NOT NULL COMMENT '企业应用id',
  `agent_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '企业应用名称',
  `square_logo_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '企业应用方形头像',
  `corp_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'SUITE' COMMENT '应用类型: SELF-自建应用, SUITE-代开发服务商',
  `auth_status` tinyint(1) NOT NULL DEFAULT 1 COMMENT '授权状态: 0-已取消/过期, 1-正常授权',
  `app` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '应用',
  `permanent_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '代开发应用的secret',
  `del_flag` tinyint(1) NULL DEFAULT 0 COMMENT '删除标记,1:已删除,0:正常',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '修改人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `tenant_id` bigint NULL DEFAULT NULL COMMENT '租户id',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_tenant_id_corp_id`(`tenant_id` ASC, `corp_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '企微企业授权管理表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of wx_corp_auth
-- ----------------------------

-- ----------------------------
-- Table structure for wx_corp_tag
-- ----------------------------
DROP TABLE IF EXISTS `wx_corp_tag`;
CREATE TABLE `wx_corp_tag`  (
  `id` bigint NOT NULL COMMENT '主键ID',
  `corp_id` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '企业ID (企微唯一标识)',
  `tag_id` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '标签id',
  `tag_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '标签名称',
  `group_id` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '标签组id',
  `order` bigint NULL DEFAULT NULL COMMENT '标签次序值。order值大的排序靠前',
  `sync_version` bigint NULL DEFAULT NULL COMMENT '同步版本号',
  `del_flag` tinyint(1) NULL DEFAULT 0 COMMENT '删除标记,1:已删除,0:正常',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '修改人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `tenant_id` bigint NULL DEFAULT NULL COMMENT '租户id',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_tenant_id_corp_id_group_id_tag_id`(`tenant_id` ASC, `corp_id` ASC, `group_id` ASC, `tag_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '企微客户标签表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of wx_corp_tag
-- ----------------------------

-- ----------------------------
-- Table structure for wx_corp_tag_group
-- ----------------------------
DROP TABLE IF EXISTS `wx_corp_tag_group`;
CREATE TABLE `wx_corp_tag_group`  (
  `id` bigint NOT NULL COMMENT '主键ID',
  `corp_id` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '企业ID (企微唯一标识)',
  `group_id` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '标签组id',
  `group_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '标签组名称',
  `order` bigint NULL DEFAULT NULL COMMENT '标签组次序值。order值大的排序靠前',
  `extra_data` json NULL COMMENT '企微标签组原始完整数据快照',
  `sync_version` bigint NULL DEFAULT NULL COMMENT '同步版本号',
  `del_flag` tinyint(1) NULL DEFAULT 0 COMMENT '删除标记,1:已删除,0:正常',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '修改人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `tenant_id` bigint NULL DEFAULT NULL COMMENT '租户id',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_tenant_id_corp_id_group_id`(`tenant_id` ASC, `corp_id` ASC, `group_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '企微客户标签组表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of wx_corp_tag_group
-- ----------------------------

-- ----------------------------
-- Table structure for wx_customer
-- ----------------------------
DROP TABLE IF EXISTS `wx_customer`;
CREATE TABLE `wx_customer`  (
  `id` bigint NOT NULL COMMENT '主键ID',
  `corp_id` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '企业ID',
  `external_user_id` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '外部联系人的userid',
  `name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '外部联系人的名称',
  `avatar` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '外部联系人头像',
  `type` tinyint NULL DEFAULT NULL COMMENT '外部联系人的类型，1表示该外部联系人是微信用户，2表示该外部联系人是企业微信用户',
  `gender` tinyint NULL DEFAULT NULL COMMENT '性别 0-未知 1-男性 2-女性',
  `union_id` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '外部联系人在微信开放平台的唯一身份标识（微信unionid）',
  `position` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '外部联系人的职位',
  `corp_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '外部联系人所在企业的简称',
  `corp_full_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '外部联系人所在企业的主体名称',
  `user_id` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '添加了此外部联系人的企业成员userid',
  `extra_data` json NULL COMMENT '企微客户原始完整数据快照',
  `sync_version` bigint NULL DEFAULT NULL COMMENT '同步版本号',
  `del_flag` tinyint(1) NULL DEFAULT 0 COMMENT '删除标记,1:已删除,0:正常',
  `lost_flag` tinyint(1) NULL DEFAULT 0 COMMENT '是否流失:0-正常,1-流失(好友关系解除)',
  `lost_by` tinyint NULL DEFAULT NULL COMMENT '流失发起方:1-客户主动删除,2-员工删除,3-接替失败,4-在职继承转接解除',
  `lost_reason` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '流失原因',
  `lost_time` datetime NULL DEFAULT NULL COMMENT '流失时间',
  `handover_to_userid` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '客户交接目标成员userid（交接/继承场景，流失行记录交接去向）',
  `handover_time` datetime NULL DEFAULT NULL COMMENT '客户交接时间（发起/完成交接时写入）',
  `follow_create_time` datetime NULL DEFAULT NULL COMMENT '该成员添加此外部联系人的时间',
  `follow_state` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '企业自定义的state参数（联系我/获客链接渠道标识）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '修改人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `tenant_id` bigint NULL DEFAULT NULL COMMENT '租户id',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_tenant_corp_external_user_id`(`tenant_id` ASC, `corp_id` ASC, `external_user_id` ASC, `user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '企微客户表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of wx_customer
-- ----------------------------

-- ----------------------------
-- Table structure for wx_department
-- ----------------------------
DROP TABLE IF EXISTS `wx_department`;
CREATE TABLE `wx_department`  (
  `id` bigint NOT NULL COMMENT '主键ID',
  `corp_id` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '企业ID (代开发模式对应授权企业的corpId，自建模式可存自身corpId或默认值)',
  `dept_id` bigint NOT NULL COMMENT '企微部门ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '部门名称',
  `name_en` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '英文名称',
  `parent_id` bigint NULL DEFAULT 0 COMMENT '企微父部门ID',
  `order` bigint NULL DEFAULT NULL COMMENT 'order值大的排序靠前',
  `department_leader` json NULL COMMENT '部门负责人的UserID',
  `sync_version` bigint NOT NULL DEFAULT 0 COMMENT '同步版本号',
  `del_flag` tinyint(1) NOT NULL DEFAULT 0 COMMENT '删除标记,1:已删除,0:正常',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '修改人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `tenant_id` bigint NULL DEFAULT NULL COMMENT '租户id',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_tenant_id_corp_dept_id`(`tenant_id` ASC, `corp_id` ASC, `dept_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '企微部门表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of wx_department
-- ----------------------------

-- ----------------------------
-- Table structure for wx_group_chat
-- ----------------------------
DROP TABLE IF EXISTS `wx_group_chat`;
CREATE TABLE `wx_group_chat`  (
  `id` bigint NOT NULL COMMENT '主键ID',
  `corp_id` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '企业ID (企微唯一标识)',
  `chat_id` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '客户群ID',
  `chat_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '群名称',
  `group_create_time` datetime NULL DEFAULT NULL COMMENT '群创建时间（企微群详情返回）',
  `owner` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '群主ID',
  `notice` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '群公告',
  `status` tinyint(1) NULL DEFAULT NULL COMMENT '客户群跟进状态。\r\n0 - 跟进人正常\r\n1 - 跟进人离职\r\n2 - 离职继承中\r\n3 - 离职继承完成',
  `member_num` int NULL DEFAULT 0 COMMENT '群成员总数',
  `member_version` bigint NULL DEFAULT NULL COMMENT '群成员版本号（update 回调 CurMemVer，事件一致性校验）',
  `dismiss_flag` tinyint(1) NULL DEFAULT 0 COMMENT '是否已解散:0-否,1-已解散（dismiss 事件标记）',
  `dismiss_time` datetime NULL DEFAULT NULL COMMENT '解散时间',
  `admin_list` json NULL COMMENT '群管理员userid',
  `sync_version` bigint NULL DEFAULT NULL COMMENT '同步版本号',
  `del_flag` tinyint(1) NULL DEFAULT 0 COMMENT '删除标记,1:已删除,0:正常',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '修改人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `tenant_id` bigint NULL DEFAULT NULL COMMENT '租户id',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_tenant_id_corp_id_chat_id`(`tenant_id` ASC, `corp_id` ASC, `chat_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '企微客户群主表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of wx_group_chat
-- ----------------------------

-- ----------------------------
-- Table structure for wx_group_chat_member
-- ----------------------------
DROP TABLE IF EXISTS `wx_group_chat_member`;
CREATE TABLE `wx_group_chat_member`  (
  `id` bigint NOT NULL COMMENT '主键ID',
  `corp_id` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '企业ID (企微唯一标识)',
  `chat_id` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '客户群ID',
  `user_id` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '群成员id',
  `type` tinyint(1) NULL DEFAULT NULL COMMENT '成员类型。\r\n1 - 企业成员\r\n2 - 外部联系人',
  `union_id` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '外部联系人在微信开放平台的唯一身份标识（微信unionid）',
  `join_time` datetime NULL DEFAULT NULL COMMENT '入群时间',
  `join_scene` tinyint(1) NULL DEFAULT NULL COMMENT '入群方式。\r\n1 - 由群成员邀请入群（直接邀请入群）\r\n2 - 由群成员邀请入群（通过邀请链接入群）\r\n3 - 通过扫描群二维码入群',
  `invitor_user_id` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '邀请者的userid',
  `group_nickname` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '在群里的昵称',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '名字',
  `sync_version` bigint NULL DEFAULT NULL COMMENT '同步版本号',
  `del_flag` tinyint(1) NULL DEFAULT 0 COMMENT '删除标记,1:已删除,0:正常',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '修改人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `tenant_id` bigint NULL DEFAULT NULL COMMENT '租户id',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_tenant_id_corp_id_chat_id_type_user_id`(`tenant_id` ASC, `corp_id` ASC, `chat_id` ASC, `type` ASC, `user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '企微客户群成员表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of wx_group_chat_member
-- ----------------------------

-- ----------------------------
-- Table structure for wx_group_chat_member_log
-- ----------------------------
DROP TABLE IF EXISTS `wx_group_chat_member_log`;
CREATE TABLE `wx_group_chat_member_log`  (
  `id` bigint NOT NULL COMMENT '主键ID',
  `corp_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '企业ID',
  `chat_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '客户群ID',
  `userid` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '成员userid',
  `name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '成员名称',
  `change_type` tinyint NULL DEFAULT NULL COMMENT '变动类型:1-入群,2-退群',
  `change_time` datetime NULL DEFAULT NULL COMMENT '变动时间',
  `sync_version` bigint NULL DEFAULT NULL COMMENT '同步版本号',
  `tenant_id` bigint NULL DEFAULT NULL COMMENT '租户ID',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_chat_change`(`chat_id` ASC, `change_time` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '群成员变动记录' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of wx_group_chat_member_log
-- ----------------------------

-- ----------------------------
-- Table structure for wx_mass_content
-- ----------------------------
DROP TABLE IF EXISTS `wx_mass_content`;
CREATE TABLE `wx_mass_content`  (
  `id` bigint NOT NULL COMMENT '主键ID',
  `task_id` bigint NULL DEFAULT NULL COMMENT '任务ID',
  `msg_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '消息类型：text-文本，image-图片，video-视频，file-文件，link-链接，miniprogram-小程序',
  `text_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '文本内容（msg_type = text 时使用）',
  `link_title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链接标题',
  `link_desc` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链接描述',
  `link_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链接跳转URL',
  `mini_title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '小程序卡片标题',
  `mini_page` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '小程序页面路径',
  `mini_appid` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '小程序appid',
  `sort` int NULL DEFAULT NULL COMMENT '附件排序（从小到大，最多9个）',
  `filename` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '文件名',
  `url` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '资源地址',
  `del_flag` tinyint(1) NULL DEFAULT 0 COMMENT '删除标记,1:已删除,0:正常',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '修改人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `tenant_id` bigint NULL DEFAULT NULL COMMENT '租户id',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '企微群发内容表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of wx_mass_content
-- ----------------------------

-- ----------------------------
-- Table structure for wx_mass_content_log
-- ----------------------------
DROP TABLE IF EXISTS `wx_mass_content_log`;
CREATE TABLE `wx_mass_content_log`  (
  `id` bigint NOT NULL COMMENT '主键ID',
  `invoke_id` bigint NULL DEFAULT NULL COMMENT '调用ID',
  `task_id` bigint NULL DEFAULT NULL COMMENT '任务ID',
  `msg_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '消息类型：text-文本，image-图片，video-视频，file-文件，link-链接，miniprogram-小程序',
  `text_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '文本内容（msg_type = text 时使用）',
  `link_title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链接标题',
  `link_desc` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链接描述',
  `link_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链接跳转URL',
  `mini_title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '小程序卡片标题',
  `mini_page` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '小程序页面路径',
  `mini_appid` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '小程序appid',
  `sort` int NULL DEFAULT NULL COMMENT '附件排序（从小到大，最多9个）',
  `filename` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '文件名',
  `url` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '资源地址',
  `del_flag` tinyint(1) NULL DEFAULT 0 COMMENT '删除标记,1:已删除,0:正常',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '修改人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `tenant_id` bigint NULL DEFAULT NULL COMMENT '租户id',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '企微群发内容表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of wx_mass_content_log
-- ----------------------------

-- ----------------------------
-- Table structure for wx_mass_invoke_log
-- ----------------------------
DROP TABLE IF EXISTS `wx_mass_invoke_log`;
CREATE TABLE `wx_mass_invoke_log`  (
  `id` bigint NOT NULL COMMENT '主键ID',
  `corp_id` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '企业ID (企微唯一标识)',
  `task_id` bigint NULL DEFAULT NULL COMMENT '任务id',
  `trigger_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '触发类型',
  `task_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '群发任务名称',
  `chat_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '群发类型：single-客户，group-客户群',
  `sender` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '指定发送成员userid',
  `allow_select` tinyint(1) NULL DEFAULT 0 COMMENT '是否允许成员二次选择客户（0-否，1-是）',
  `tag_filter` json NULL COMMENT '客户标签过滤条件（企业微信标签ID集合）',
  `status` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '调用状态：SUCCESS-成功，FAIL-失败',
  `msgid` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '群发消息ID',
  `extra_data` json NULL COMMENT '原始完整数据快照',
  `errmsg` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '错误信息',
  `del_flag` tinyint(1) NULL DEFAULT 0 COMMENT '删除标记,1:已删除,0:正常',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '修改人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `tenant_id` bigint NULL DEFAULT NULL COMMENT '租户id',
  `batch_no` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '批次号（同一批次触发的任务共享）',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_tenant_id_corp_id`(`tenant_id` ASC, `corp_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '企微群发调用记录表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of wx_mass_invoke_log
-- ----------------------------

-- ----------------------------
-- Table structure for wx_mass_target
-- ----------------------------
DROP TABLE IF EXISTS `wx_mass_target`;
CREATE TABLE `wx_mass_target`  (
  `id` bigint NOT NULL COMMENT '主键ID',
  `task_id` bigint NULL DEFAULT NULL COMMENT '任务ID',
  `chat_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '群发类型：single-客户，group-客户群',
  `target_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '客户external_userid 或 客户群chat_id',
  `del_flag` tinyint(1) NULL DEFAULT 0 COMMENT '删除标记,1:已删除,0:正常',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '修改人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `tenant_id` bigint NULL DEFAULT NULL COMMENT '租户id',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '企微群发目标表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of wx_mass_target
-- ----------------------------

-- ----------------------------
-- Table structure for wx_mass_target_log
-- ----------------------------
DROP TABLE IF EXISTS `wx_mass_target_log`;
CREATE TABLE `wx_mass_target_log`  (
  `id` bigint NOT NULL COMMENT '主键ID',
  `task_id` bigint NULL DEFAULT NULL COMMENT '任务ID',
  `invoke_id` bigint NULL DEFAULT NULL COMMENT '调用记录ID',
  `chat_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '群发类型：single-客户，group-客户群',
  `target_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '客户external_userid 或 客户群chat_id',
  `user_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '企微账号ID',
  `status` tinyint NULL DEFAULT NULL COMMENT '发送状态：0-未发送 1-已发送 2-因客户不是好友导致发送失败 3-因客户已经收到其他群发消息导致发送失败',
  `send_time` datetime NULL DEFAULT NULL COMMENT '发送时间',
  `del_flag` tinyint(1) NULL DEFAULT 0 COMMENT '删除标记,1:已删除,0:正常',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '修改人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `tenant_id` bigint NULL DEFAULT NULL COMMENT '租户id',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_idx`(`tenant_id` ASC, `task_id` ASC, `invoke_id` ASC, `user_id` ASC, `target_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '企微群发目标明细表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of wx_mass_target_log
-- ----------------------------

-- ----------------------------
-- Table structure for wx_mass_task
-- ----------------------------
DROP TABLE IF EXISTS `wx_mass_task`;
CREATE TABLE `wx_mass_task`  (
  `id` bigint NOT NULL COMMENT '主键ID',
  `corp_id` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '企业ID (企微唯一标识)',
  `task_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '群发任务名称',
  `chat_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '群发类型：single-客户，group-客户群',
  `sender_list` json NULL COMMENT '指定发送成员userid集合',
  `allow_select` tinyint(1) NULL DEFAULT 0 COMMENT '是否允许成员二次选择客户（0-否，1-是）',
  `tag_filter` json NULL COMMENT '客户标签过滤条件（企业微信标签ID集合）',
  `status` tinyint NULL DEFAULT 1 COMMENT '任务状态：0-禁用，1-启用，2-执行中，3已完成',
  `scheduled_time` datetime NULL DEFAULT NULL COMMENT '预计执行时间',
  `execute_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '执行类型：IMMEDIATE-立即执行，ONCE-仅一次，DAILY-每天，WEEKLY-每周，MONTHLY-每月，CRON-自定义Cron',
  `cron_expression` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'Cron表达式（仅 execute_type=5 时有值）',
  `del_flag` tinyint(1) NULL DEFAULT 0 COMMENT '删除标记,1:已删除,0:正常',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '修改人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `tenant_id` bigint NULL DEFAULT NULL COMMENT '租户id',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_tenant_id_corp_id`(`tenant_id` ASC, `corp_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '企微群发任务表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of wx_mass_task
-- ----------------------------

-- ----------------------------
-- Table structure for wx_moment_content
-- ----------------------------
DROP TABLE IF EXISTS `wx_moment_content`;
CREATE TABLE `wx_moment_content`  (
  `id` bigint NOT NULL COMMENT '主键ID',
  `task_id` bigint NULL DEFAULT NULL COMMENT '任务ID',
  `msg_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '消息类型：text-文本，image-图片，video-视频，link-链接',
  `text_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '文本内容（msg_type = text 时使用）',
  `link_title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链接标题',
  `link_desc` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链接描述',
  `link_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链接跳转URL',
  `sort` int NULL DEFAULT NULL COMMENT '附件排序（从小到大，最多9个）',
  `filename` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '文件名',
  `url` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '资源地址',
  `del_flag` tinyint(1) NULL DEFAULT 0 COMMENT '删除标记,1:已删除,0:正常',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '修改人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `tenant_id` bigint NULL DEFAULT NULL COMMENT '租户id',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '企微朋友圈内容表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of wx_moment_content
-- ----------------------------

-- ----------------------------
-- Table structure for wx_moment_content_log
-- ----------------------------
DROP TABLE IF EXISTS `wx_moment_content_log`;
CREATE TABLE `wx_moment_content_log`  (
  `id` bigint NOT NULL COMMENT '主键ID',
  `invoke_id` bigint NULL DEFAULT NULL COMMENT '调用ID',
  `task_id` bigint NULL DEFAULT NULL COMMENT '任务ID',
  `msg_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '消息类型：text-文本，image-图片，video-视频，link-链接',
  `text_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '文本内容（msg_type = text 时使用）',
  `link_title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链接标题',
  `link_desc` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链接描述',
  `link_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链接跳转URL',
  `sort` int NULL DEFAULT NULL COMMENT '附件排序（从小到大，最多9个）',
  `filename` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '文件名',
  `url` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '资源地址',
  `del_flag` tinyint(1) NULL DEFAULT 0 COMMENT '删除标记,1:已删除,0:正常',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '修改人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `tenant_id` bigint NULL DEFAULT NULL COMMENT '租户id',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '企微朋友圈内容表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of wx_moment_content_log
-- ----------------------------

-- ----------------------------
-- Table structure for wx_moment_invoke_log
-- ----------------------------
DROP TABLE IF EXISTS `wx_moment_invoke_log`;
CREATE TABLE `wx_moment_invoke_log`  (
  `id` bigint NOT NULL COMMENT '主键ID',
  `corp_id` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '企业ID (企微唯一标识)',
  `task_id` bigint NULL DEFAULT NULL COMMENT '任务id',
  `trigger_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '触发类型',
  `task_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '群发任务名称',
  `visible_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '发表范围类型: ALL(全部成员) / USER(指定成员) / DEPARTMENT(指定部门) / TAG(指定客户标签)',
  `visible_user_list` json NULL COMMENT '指定成员ID列表(JSON)，visible_type=USER时使用',
  `visible_dept_list` json NULL COMMENT '指定部门ID列表(JSON)，visible_type=DEPARTMENT时使用',
  `visible_tag_list` json NULL COMMENT '指定标签ID列表(JSON)，visible_type=TAG时使用',
  `jobid` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '异步任务id',
  `status` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '调用状态：SUCCESS-成功，FAIL-失败',
  `moment_status` tinyint NULL DEFAULT NULL COMMENT '朋友圈状态:1表示开始创建任务，2表示正在创建任务中，3表示创建任务已完成',
  `moment_id` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '朋友圈id',
  `extra_data` json NULL COMMENT '原始完整数据快照',
  `errmsg` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '错误信息',
  `del_flag` tinyint(1) NULL DEFAULT 0 COMMENT '删除标记,1:已删除,0:正常',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '修改人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `tenant_id` bigint NULL DEFAULT NULL COMMENT '租户id',
  `batch_no` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '批次号（同一批次触发的任务共享）',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_tenant_id_corp_id`(`tenant_id` ASC, `corp_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '企微朋友圈调用记录表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of wx_moment_invoke_log
-- ----------------------------

-- ----------------------------
-- Table structure for wx_moment_send_log
-- ----------------------------
DROP TABLE IF EXISTS `wx_moment_send_log`;
CREATE TABLE `wx_moment_send_log`  (
  `id` bigint NOT NULL COMMENT '主键ID',
  `corp_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '企业ID',
  `task_id` bigint NOT NULL COMMENT '任务ID',
  `invoke_id` bigint NOT NULL COMMENT '调用记录ID',
  `user_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '企微成员userid',
  `publish_status` tinyint NOT NULL DEFAULT 0 COMMENT '发表状态：0-未发表，1-已发表',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_tenant_invoke_user`(`tenant_id` ASC, `invoke_id` ASC, `user_id` ASC) USING BTREE,
  INDEX `idx_task`(`task_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '朋友圈成员发表记录' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of wx_moment_send_log
-- ----------------------------

-- ----------------------------
-- Table structure for wx_moment_task
-- ----------------------------
DROP TABLE IF EXISTS `wx_moment_task`;
CREATE TABLE `wx_moment_task`  (
  `id` bigint NOT NULL COMMENT '主键ID',
  `corp_id` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '企业ID (企微唯一标识)',
  `task_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '群发任务名称',
  `visible_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '发表范围类型: ALL(全部成员) / USER(指定成员) / DEPARTMENT(指定部门) / TAG(指定客户标签)',
  `visible_user_list` json NULL COMMENT '指定成员ID列表(JSON)，visible_type=USER时使用',
  `visible_dept_list` json NULL COMMENT '指定部门ID列表(JSON)，visible_type=DEPARTMENT时使用',
  `visible_tag_list` json NULL COMMENT '指定标签ID列表(JSON)，visible_type=TAG时使用',
  `status` tinyint NULL DEFAULT 1 COMMENT '任务状态：0-禁用，1-启用，2-执行中，3已完成',
  `scheduled_time` datetime NULL DEFAULT NULL COMMENT '预计执行时间',
  `execute_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '执行类型：IMMEDIATE-立即执行，ONCE-仅一次，DAILY-每天，WEEKLY-每周，MONTHLY-每月，CRON-自定义Cron',
  `cron_expression` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'Cron表达式（仅 execute_type=5 时有值）',
  `del_flag` tinyint(1) NULL DEFAULT 0 COMMENT '删除标记,1:已删除,0:正常',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '修改人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `tenant_id` bigint NULL DEFAULT NULL COMMENT '租户id',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_tenant_id_corp_id`(`tenant_id` ASC, `corp_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '企微朋友圈任务表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of wx_moment_task
-- ----------------------------

-- ----------------------------
-- Table structure for wx_sync_version
-- ----------------------------
DROP TABLE IF EXISTS `wx_sync_version`;
CREATE TABLE `wx_sync_version`  (
  `id` bigint NOT NULL COMMENT '主键ID',
  `corp_id` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '企业ID',
  `sync_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '同步类型: FULL, INCREMENT',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'RUNNING' COMMENT '状态: RUNNING, SUCCESS, FAIL',
  `trigger_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '触发: CRON, MANUAL, CALLBACK',
  `start_time` datetime NULL DEFAULT NULL COMMENT '同步开始时间',
  `end_time` datetime NULL DEFAULT NULL COMMENT '同步结束时间',
  `version_no` bigint NULL DEFAULT NULL COMMENT '版本号',
  `remark` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '备注',
  `del_flag` tinyint(1) NULL DEFAULT 0 COMMENT '删除标记,1:已删除,0:正常',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '修改人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `tenant_id` bigint NULL DEFAULT NULL COMMENT '租户id',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `uk_tenant_corp_sync_version`(`tenant_id` ASC, `corp_id` ASC, `sync_type` ASC, `version_no` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '同步版本记录表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of wx_sync_version
-- ----------------------------

-- ----------------------------
-- Table structure for wx_user
-- ----------------------------
DROP TABLE IF EXISTS `wx_user`;
CREATE TABLE `wx_user`  (
  `id` bigint NOT NULL COMMENT '主键ID',
  `corp_id` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '企业ID',
  `user_id` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '企微账号ID',
  `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '姓名',
  `mobile` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '手机号',
  `depart_ids` json NULL COMMENT '部门id列表',
  `main_department` bigint NULL DEFAULT 1 COMMENT '主部门ID',
  `position` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '职务信息',
  `email` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '邮箱',
  `avatar` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '头像',
  `status` int NULL DEFAULT NULL COMMENT '激活状态: 1=已激活，2=已禁用，4=未激活，5=退出企业',
  `open_user_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'openUserId',
  `gender` int NULL DEFAULT NULL COMMENT '性别。0表示未定义，1表示男性，2表示女性。',
  `sync_version` bigint NOT NULL DEFAULT 0 COMMENT '同步版本号',
  `extra_data` json NULL COMMENT '企微用户原始完整数据快照(JSON)',
  `del_flag` tinyint(1) NOT NULL DEFAULT 0 COMMENT '删除标记,1:已删除,0:正常',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '修改人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `tenant_id` bigint NULL DEFAULT NULL COMMENT '租户id',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_corp_user_id`(`tenant_id` ASC, `corp_id` ASC, `user_id` ASC) USING BTREE,
  INDEX `idx_corp_id`(`corp_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '企微用户表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of wx_user
-- ----------------------------

-- ----------------------------
-- Table structure for wx_user_binding
-- ----------------------------
DROP TABLE IF EXISTS `wx_user_binding`;
CREATE TABLE `wx_user_binding`  (
  `id` bigint NOT NULL COMMENT '主键ID',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `sys_user_id` bigint NOT NULL COMMENT '系统用户ID(sys_user.id)',
  `corp_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '企业ID(随绑定的企微成员写入)',
  `wx_userid` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '企微成员ID(wx_user.userid)',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_tenant_user_corp`(`tenant_id` ASC, `sys_user_id` ASC, `corp_id` ASC) USING BTREE,
  UNIQUE INDEX `uk_tenant_corp_wxuser`(`tenant_id` ASC, `corp_id` ASC, `wx_userid` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统用户-企微成员绑定' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of wx_user_binding
-- ----------------------------

-- ----------------------------
-- Table structure for wx_welcome_msg
-- ----------------------------
DROP TABLE IF EXISTS `wx_welcome_msg`;
CREATE TABLE `wx_welcome_msg`  (
  `id` bigint NOT NULL COMMENT '主键ID',
  `corp_id` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '企业ID',
  `del_flag` tinyint(1) NULL DEFAULT 0 COMMENT '删除标记,1:已删除,0:正常',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '修改人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `tenant_id` bigint NULL DEFAULT NULL COMMENT '租户id',
  `name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '欢迎语标题',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_corp_id`(`corp_id` ASC) USING BTREE,
  INDEX `idx_del_flag`(`del_flag` ASC) USING BTREE,
  INDEX `idx_tenant_id`(`tenant_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '企微欢迎语（容器：标题/企业，内容在 wx_welcome_msg_rule）' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of wx_welcome_msg
-- ----------------------------

-- ----------------------------
-- Table structure for wx_welcome_msg_bind
-- ----------------------------
DROP TABLE IF EXISTS `wx_welcome_msg_bind`;
CREATE TABLE `wx_welcome_msg_bind`  (
  `id` bigint NOT NULL COMMENT '主键ID',
  `corp_id` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '企业ID',
  `del_flag` tinyint(1) NULL DEFAULT 0 COMMENT '删除标记,1:已删除,0:正常',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '修改人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `tenant_id` bigint NULL DEFAULT NULL COMMENT '租户id',
  `target_type` tinyint NOT NULL COMMENT '绑定类型：1=员工 2=部门',
  `target_id` bigint NOT NULL COMMENT '员工ID或部门ID',
  `welcome_msg_id` bigint NOT NULL COMMENT '主欢迎语ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_target_msg`(`corp_id` ASC, `target_type` ASC, `target_id` ASC, `welcome_msg_id` ASC) USING BTREE,
  INDEX `idx_corp_id`(`corp_id` ASC) USING BTREE,
  INDEX `idx_welcome_msg_id`(`welcome_msg_id` ASC) USING BTREE,
  INDEX `idx_del_flag`(`del_flag` ASC) USING BTREE,
  INDEX `idx_tenant_id`(`tenant_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '企微欢迎语绑定表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of wx_welcome_msg_bind
-- ----------------------------

-- ----------------------------
-- Table structure for wx_welcome_msg_rule
-- ----------------------------
DROP TABLE IF EXISTS `wx_welcome_msg_rule`;
CREATE TABLE `wx_welcome_msg_rule`  (
  `id` bigint NOT NULL COMMENT '主键ID',
  `corp_id` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '企业ID',
  `welcome_msg_id` bigint NOT NULL COMMENT '所属欢迎语ID（wx_welcome_msg.id）',
  `name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '规则名称',
  `content` json NULL COMMENT '规则内容，含文本/图片/视频/链接',
  `effective_at` json NULL COMMENT '生效星期，如[1,2,3,4,5]，1=周一',
  `start_time` time NULL DEFAULT NULL COMMENT '生效开始时间',
  `end_time` time NULL DEFAULT NULL COMMENT '生效结束时间',
  `media_id` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '企微临时素材media_id',
  `media_expires_at` datetime NULL DEFAULT NULL COMMENT 'media_id过期时间',
  `media_url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '本地/云存储地址',
  `sort` int NULL DEFAULT 0 COMMENT '排序（匹配顺序）',
  `del_flag` tinyint(1) NULL DEFAULT 0 COMMENT '删除标记,1:已删除,0:正常',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '修改人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `tenant_id` bigint NULL DEFAULT NULL COMMENT '租户id',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_welcome_msg_id`(`welcome_msg_id` ASC) USING BTREE,
  INDEX `idx_corp_id`(`corp_id` ASC) USING BTREE,
  INDEX `idx_del_flag`(`del_flag` ASC) USING BTREE,
  INDEX `idx_tenant_id`(`tenant_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '企微欢迎语分时规则' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of wx_welcome_msg_rule
-- ----------------------------

SET FOREIGN_KEY_CHECKS = 1;
