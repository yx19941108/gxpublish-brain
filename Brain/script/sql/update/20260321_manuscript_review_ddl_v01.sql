/*
manuscript_review - DDL v01

affected tables
- brain_manuscript_review_serial
- brain_manuscript_review
- brain_manuscript_review_flow_config
- brain_manuscript_review_attachment
- brain_manuscript_review_external_link
- brain_manuscript_review_video_marker
- brain_manuscript_review_history

backup action (before apply)
- Backup platform tables that will be touched by DML script (sys_menu/sys_dict_type/sys_dict_data/sys_role/sys_user/sys_user_role/sys_role_menu) using your standard method:
  - logical dump (recommended), or
  - create shadow tables with date suffix (example: create table sys_menu_bak_20260321 as select * from sys_menu)
- Confirm no conflicting IDs will be used by the DML script (menu_id/dict_id/dict_code/role_id/user_id ranges).

apply order
1) 20260321_manuscript_review_ddl_v01.sql
2) 20260321_manuscript_review_dml_v01.sql

rollback order
1) 20260321_manuscript_review_rollback_v01.sql

NOTE: 本轮仅起草，未 apply。
*/

/* -------------------------------------------------------------------------- */
/* 1) 按日按类型流水号表（独立）                                                    */
/* -------------------------------------------------------------------------- */
drop table if exists `brain_manuscript_review_serial`;
CREATE TABLE `brain_manuscript_review_serial` (
  `id` bigint NOT NULL COMMENT '主键',
  `tenant_id` varchar(20) COLLATE utf8mb4_bin NOT NULL DEFAULT '000000' COMMENT '租户编号',
  `process_type` varchar(20) COLLATE utf8mb4_bin NOT NULL COMMENT '流程类型（AUDIT/PROOFREAD）',
  `biz_date` char(8) COLLATE utf8mb4_bin NOT NULL COMMENT '业务日期（yyyyMMdd）',
  `current_serial` int NOT NULL DEFAULT '0' COMMENT '当日当前流水号（从0开始，next=+1）',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_type_date` (`tenant_id`,`process_type`,`biz_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='审校-按日按类型流水号表（一期无草稿/删除语义）';

/* -------------------------------------------------------------------------- */
/* 2) 审校主记录表（一期无草稿/删除/回收站语义）                                     */
/* -------------------------------------------------------------------------- */
drop table if exists `brain_manuscript_review`;
CREATE TABLE `brain_manuscript_review` (
  `id` bigint NOT NULL COMMENT '主键',
  `tenant_id` varchar(20) COLLATE utf8mb4_bin NOT NULL DEFAULT '000000' COMMENT '租户编号',

  `process_type` varchar(20) COLLATE utf8mb4_bin NOT NULL COMMENT '流程类型（AUDIT/PROOFREAD）',
  `flow_code` varchar(40) COLLATE utf8mb4_bin NOT NULL COMMENT '流程编码（默认 manuscript_review_audit_flow / manuscript_review_proofread_flow）',
  `flow_instance_id` bigint DEFAULT NULL COMMENT '流程实例ID（flow_instance.id）',

  `flow_status_label` varchar(20) COLLATE utf8mb4_bin NOT NULL COMMENT '流程状态（审批中/已退回/已完成/已取消/已驳回）',
  `current_node_status` varchar(40) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '当前节点状态码（LEVEL_1/LEVEL_2/LEVEL_3/RETURN_TO_INITIATOR/FLOW_FINISHED/FLOW_CANCELED/FLOW_REJECTED）',
  `current_node_label` varchar(30) COLLATE utf8mb4_bin NOT NULL COMMENT '当前节点（待一级审批/待二级审批/待三级审批/待发起人处理/流程完成/流程已取消/流程已驳回）',

  `manuscript_code` varchar(30) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '系统稿件号（submitAndFlowStart 成功后写入；SH|JD + yyyyMMdd + 三位流水号）',
  `external_manuscript_code` varchar(64) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '外部稿件编号（在审校模块内按租户唯一，允许为空）',

  `title` varchar(200) COLLATE utf8mb4_bin NOT NULL COMMENT '标题',
  `media_channel` varchar(100) COLLATE utf8mb4_bin NOT NULL COMMENT '媒体/栏目（一期保持单字段写入）',
  `submit_department` varchar(100) COLLATE utf8mb4_bin NOT NULL COMMENT '报送部门（新增保存必填，修改保存只读回传）',
  `author_name` varchar(200) COLLATE utf8mb4_bin NOT NULL COMMENT '作者（仅允许中文顿号分隔）',
  `remark_text` varchar(1000) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '说明（写侧业务字段，最大1000）',
  `content_body` text COLLATE utf8mb4_bin NOT NULL COMMENT '正文（写侧业务字段，最大20000）',
  `first_submit_time` datetime DEFAULT NULL COMMENT '首次提交时间（首次发起成功写入，后续不覆盖）',
  `latest_submit_time` datetime DEFAULT NULL COMMENT '最近提交时间（每次提交或再次提交成功时刷新）',

  `content` text COLLATE utf8mb4_bin DEFAULT NULL COMMENT '旧读侧残留正文列；保留兼容，不再作为新写侧依据',
  `note` varchar(1000) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '旧读侧残留说明列；保留兼容，不再作为新写侧依据',
  `media_channel_dict_code` bigint DEFAULT NULL COMMENT '媒体/栏目预设字典主键（sys_dict_data.dict_code；手填为空）',
  `media_channel_label` varchar(100) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '旧读侧残留媒体/栏目展示列；保留兼容，不再作为新写侧依据',
  `submitter_dept_id` bigint DEFAULT NULL COMMENT '报送部门ID',
  `submitter_dept_name` varchar(100) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '旧读侧残留报送部门展示列；保留兼容，不再作为新写侧依据',
  `author_names` varchar(200) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '旧读侧残留作者列；保留兼容，不再作为新写侧依据',

  `initiator_user_id` bigint NOT NULL COMMENT '发起人用户ID',
  `initiator_name` varchar(30) COLLATE utf8mb4_bin NOT NULL COMMENT '发起人姓名（展示用）',

  `create_dept` bigint DEFAULT NULL COMMENT '创建部门',
  `create_by` bigint DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_manuscript_code` (`tenant_id`,`manuscript_code`),
  UNIQUE KEY `uk_tenant_external_code` (`tenant_id`,`external_manuscript_code`),
  KEY `idx_tenant_update_time` (`tenant_id`,`update_time`),
  KEY `idx_tenant_media_channel` (`tenant_id`,`media_channel_dict_code`),
  KEY `idx_tenant_initiator` (`tenant_id`,`initiator_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='审校-主记录（一期无草稿/删除语义）';

/* -------------------------------------------------------------------------- */
/* 3) 审批链映射配置（按租户隔离，roleKey 作为业务引用，不使用数据库主键）                 */
/* -------------------------------------------------------------------------- */
drop table if exists `brain_manuscript_review_flow_config`;
CREATE TABLE `brain_manuscript_review_flow_config` (
  `id` bigint NOT NULL COMMENT '主键',
  `tenant_id` varchar(20) COLLATE utf8mb4_bin NOT NULL DEFAULT '000000' COMMENT '租户编号',
  `process_type` varchar(20) COLLATE utf8mb4_bin NOT NULL COMMENT '流程类型（AUDIT/PROOFREAD）',
  `flow_code` varchar(40) COLLATE utf8mb4_bin NOT NULL COMMENT '流程编码',
  `level_one_role_key` varchar(100) COLLATE utf8mb4_bin NOT NULL COMMENT '一级审批角色 roleKey',
  `level_two_role_key` varchar(100) COLLATE utf8mb4_bin NOT NULL COMMENT '二级审批角色 roleKey',
  `level_three_role_key` varchar(100) COLLATE utf8mb4_bin NOT NULL COMMENT '三级审批角色 roleKey',
  `status` char(1) COLLATE utf8mb4_bin NOT NULL DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `create_dept` bigint DEFAULT NULL COMMENT '创建部门',
  `create_by` bigint DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_type` (`tenant_id`,`process_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='审校-审批链映射配置（roleKey 作为业务引用）';

/* -------------------------------------------------------------------------- */
/* 4) 附件（含视频）资源表：追加历史 + 当前可停用                                     */
/* -------------------------------------------------------------------------- */
drop table if exists `brain_manuscript_review_attachment`;
CREATE TABLE `brain_manuscript_review_attachment` (
  `id` bigint NOT NULL COMMENT '主键',
  `tenant_id` varchar(20) COLLATE utf8mb4_bin NOT NULL DEFAULT '000000' COMMENT '租户编号',
  `review_id` bigint NOT NULL COMMENT '审校主记录ID',

  `oss_id` bigint DEFAULT NULL COMMENT '平台OSS文件ID（sys_oss.oss_id，如使用平台OSS）',
  `file_name` varchar(255) COLLATE utf8mb4_bin NOT NULL COMMENT '文件名',
  `file_url` varchar(500) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '文件URL（如直接保存URL）',
  `file_size` bigint DEFAULT NULL COMMENT '文件大小（字节）',
  `mime_type` varchar(100) COLLATE utf8mb4_bin DEFAULT NULL COMMENT 'MIME类型',
  `is_video` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否视频（0否 1是）',
  `video_duration_seconds` int DEFAULT NULL COMMENT '视频时长（秒），未识别为NULL',

  `enabled` char(1) COLLATE utf8mb4_bin NOT NULL DEFAULT '1' COMMENT '是否有效（1有效 0停用）',
  `disabled_by` bigint DEFAULT NULL COMMENT '停用人',
  `disabled_time` datetime DEFAULT NULL COMMENT '停用时间',

  `create_dept` bigint DEFAULT NULL COMMENT '创建部门',
  `create_by` bigint DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `idx_tenant_review_enabled` (`tenant_id`,`review_id`,`enabled`),
  KEY `idx_tenant_review_video` (`tenant_id`,`review_id`,`is_video`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='审校-附件资源（追加历史 + 当前可停用）';

/* -------------------------------------------------------------------------- */
/* 5) 外部链接资源表：追加历史 + 当前可停用（同一流程内 URL 不重复）                         */
/* -------------------------------------------------------------------------- */
drop table if exists `brain_manuscript_review_external_link`;
CREATE TABLE `brain_manuscript_review_external_link` (
  `id` bigint NOT NULL COMMENT '主键',
  `tenant_id` varchar(20) COLLATE utf8mb4_bin NOT NULL DEFAULT '000000' COMMENT '租户编号',
  `review_id` bigint NOT NULL COMMENT '审校主记录ID',

  `link_title` varchar(200) COLLATE utf8mb4_bin NOT NULL COMMENT '链接标题',
  `link_url` varchar(500) COLLATE utf8mb4_bin NOT NULL COMMENT 'URL（仅允许http/https由应用层校验）',

  `enabled` char(1) COLLATE utf8mb4_bin NOT NULL DEFAULT '1' COMMENT '是否有效（1有效 0停用）',
  `disabled_by` bigint DEFAULT NULL COMMENT '停用人',
  `disabled_time` datetime DEFAULT NULL COMMENT '停用时间',

  `create_dept` bigint DEFAULT NULL COMMENT '创建部门',
  `create_by` bigint DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_review_url` (`tenant_id`,`review_id`,`link_url`),
  KEY `idx_tenant_review_enabled` (`tenant_id`,`review_id`,`enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='审校-外链资源（追加历史 + 当前可停用）';

/* -------------------------------------------------------------------------- */
/* 6) 视频时间标注：追加历史 + 当前可停用（统一保存/展示为 HH:mm:ss）                         */
/* -------------------------------------------------------------------------- */
drop table if exists `brain_manuscript_review_video_marker`;
CREATE TABLE `brain_manuscript_review_video_marker` (
  `id` bigint NOT NULL COMMENT '主键',
  `tenant_id` varchar(20) COLLATE utf8mb4_bin NOT NULL DEFAULT '000000' COMMENT '租户编号',
  `review_id` bigint NOT NULL COMMENT '审校主记录ID',
  `video_attachment_id` bigint NOT NULL COMMENT '关联视频附件ID（brain_manuscript_review_attachment.id）',

  `start_time` varchar(8) COLLATE utf8mb4_bin NOT NULL COMMENT '开始时间（HH:mm:ss）',
  `end_time` varchar(8) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '结束时间（HH:mm:ss，可空）',
  `start_seconds` int NOT NULL COMMENT '开始时间（秒，用于排序/跳转）',
  `end_seconds` int DEFAULT NULL COMMENT '结束时间（秒，可空）',
  `marker_note` varchar(500) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '备注',

  `enabled` char(1) COLLATE utf8mb4_bin NOT NULL DEFAULT '1' COMMENT '是否有效（1有效 0停用）',
  `disabled_by` bigint DEFAULT NULL COMMENT '停用人',
  `disabled_time` datetime DEFAULT NULL COMMENT '停用时间',

  `create_dept` bigint DEFAULT NULL COMMENT '创建部门',
  `create_by` bigint DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `idx_tenant_video_enabled` (`tenant_id`,`video_attachment_id`,`enabled`),
  KEY `idx_tenant_review_enabled` (`tenant_id`,`review_id`,`enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='审校-视频时间标注（追加历史 + 当前可停用）';

/* -------------------------------------------------------------------------- */
/* 7) 统一时间线历史：自然语言文案优先（不在此表对外暴露技术码值）                              */
/* -------------------------------------------------------------------------- */
drop table if exists `brain_manuscript_review_history`;
CREATE TABLE `brain_manuscript_review_history` (
  `id` bigint NOT NULL COMMENT '主键',
  `tenant_id` varchar(20) COLLATE utf8mb4_bin NOT NULL DEFAULT '000000' COMMENT '租户编号',
  `review_id` bigint NOT NULL COMMENT '审校主记录ID',

  `action_type` varchar(50) COLLATE utf8mb4_bin NOT NULL COMMENT '动作类型（内部枚举，不对外直接展示）',
  `action_text` varchar(1000) COLLATE utf8mb4_bin NOT NULL COMMENT '动作文案（自然语言，面向业务展示）',
  `actor_user_id` bigint DEFAULT NULL COMMENT '执行人用户ID（系统动作可为NULL）',
  `actor_name` varchar(30) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '执行人姓名（展示用）',

  `create_time` datetime NOT NULL COMMENT '发生时间',
  `sorted` int DEFAULT NULL COMMENT '同一流程内的稳定排序号',
  `ext_json` text COLLATE utf8mb4_bin COMMENT '扩展信息（JSON，可选）',
  PRIMARY KEY (`id`),
  KEY `idx_tenant_review_time` (`tenant_id`,`review_id`,`create_time`),
  KEY `idx_tenant_review_sorted` (`tenant_id`,`review_id`,`sorted`,`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='审校-统一时间线历史（自然语言）';
