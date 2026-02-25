-- ----------------------------
-- 编辑部审校模块表结构
-- ----------------------------

-- 1. 审校申请主表
CREATE TABLE IF NOT EXISTS `brain_editorial_review` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `title` varchar(255) NOT NULL COMMENT '申请标题',
  `content` longtext COMMENT '申请内容',
  `dept_id` bigint(20) DEFAULT NULL COMMENT '部门ID',
  `user_id` bigint(20) DEFAULT NULL COMMENT '发起人ID',
  `status` varchar(50) DEFAULT 'DRAFT' COMMENT '业务状态: DRAFT, WAITING, APPROVED, REJECTED',
  `apply_code` varchar(64) DEFAULT NULL COMMENT '流程实例关联码',
  `current_attachment_id` bigint(20) DEFAULT NULL COMMENT '当前附件ID',
  `create_dept` bigint(20) DEFAULT NULL COMMENT '创建部门',
  `create_by` bigint(20) DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` bigint(20) DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='编辑部审校申请表';

-- 2. 附件版本表
CREATE TABLE IF NOT EXISTS `brain_editorial_attachment` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `review_id` bigint(20) NOT NULL COMMENT '关联申请ID',
  `file_name` varchar(255) DEFAULT NULL COMMENT '文件名',
  `oss_id` varchar(64) DEFAULT NULL COMMENT 'OSS资源ID',
  `file_url` varchar(500) DEFAULT NULL COMMENT '文件地址',
  `file_size` bigint(20) DEFAULT NULL COMMENT '文件大小',
  `version` int(11) DEFAULT 1 COMMENT '版本号',
  `uploader_id` bigint(20) DEFAULT NULL COMMENT '上传人ID',
  `create_time` datetime DEFAULT NULL COMMENT '上传时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审校附件版本表';

-- 3. 关联链接表
CREATE TABLE IF NOT EXISTS `brain_editorial_link` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `review_id` bigint(20) NOT NULL COMMENT '关联申请ID',
  `url` varchar(500) DEFAULT NULL COMMENT '链接地址',
  `description` varchar(255) DEFAULT NULL COMMENT '描述',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审校关联链接表';

-- 4. 修改历史表
CREATE TABLE IF NOT EXISTS `brain_editorial_history` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `review_id` bigint(20) NOT NULL COMMENT '关联申请ID',
  `operator_id` bigint(20) DEFAULT NULL COMMENT '操作人ID',
  `operator_name` varchar(50) DEFAULT NULL COMMENT '操作人姓名',
  `operate_time` datetime DEFAULT NULL COMMENT '操作时间',
  `operate_type` varchar(20) DEFAULT NULL COMMENT '操作类型: SUBMIT, APPROVE, REJECT, MODIFY',
  `field_diff` json DEFAULT NULL COMMENT '字段差异JSON',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审校修改历史表';
