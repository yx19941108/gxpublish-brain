-- editorial review history contract upgrade
-- 1) remove transfer button from editorial flow nodes
-- 2) upgrade brain_editorial_history to carry event_type and operator_role_name

ALTER TABLE `brain_editorial_history`
    ADD COLUMN IF NOT EXISTS `operator_role_name` varchar(100) DEFAULT NULL COMMENT '操作人角色名' AFTER `operator_name`;

ALTER TABLE `brain_editorial_history`
    ADD COLUMN IF NOT EXISTS `event_type` varchar(32) NOT NULL DEFAULT 'APPROVAL' COMMENT '事件类型: CREATE, MODIFY, APPROVAL' AFTER `operate_time`;

ALTER TABLE `brain_editorial_history`
    MODIFY COLUMN `operate_type` varchar(255) DEFAULT NULL COMMENT '操作展示文案';

ALTER TABLE `brain_editorial_history`
    MODIFY COLUMN `field_diff` json DEFAULT NULL COMMENT '展示型历史明细JSON';

UPDATE `brain_editorial_history`
SET `event_type` = CASE
    WHEN `operate_type` = 'MODIFY' THEN 'MODIFY'
    ELSE 'APPROVAL'
END
WHERE `event_type` IS NULL OR `event_type` = '';

UPDATE `flow_node` n
    INNER JOIN `flow_definition` d ON d.`id` = n.`definition_id`
SET n.`ext` = REPLACE(n.`ext`, 'back,termination,transfer,file', 'back,termination,file')
WHERE d.`flow_code` = 'editorial_review_flow'
  AND n.`node_code` IN ('first-review-node', 'second-review-node', 'final-review-node')
  AND n.`ext` LIKE '%transfer%';
