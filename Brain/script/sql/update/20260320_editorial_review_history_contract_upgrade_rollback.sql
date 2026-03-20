-- rollback editorial review history contract upgrade

UPDATE `flow_node` n
    INNER JOIN `flow_definition` d ON d.`id` = n.`definition_id`
SET n.`ext` = REPLACE(n.`ext`, 'back,termination,file', 'back,termination,transfer,file')
WHERE d.`flow_code` = 'editorial_review_flow'
  AND n.`node_code` IN ('first-review-node', 'second-review-node', 'final-review-node')
  AND n.`ext` LIKE '%back,termination,file%';

ALTER TABLE `brain_editorial_history`
    DROP COLUMN IF EXISTS `operator_role_name`;

ALTER TABLE `brain_editorial_history`
    DROP COLUMN IF EXISTS `event_type`;

ALTER TABLE `brain_editorial_history`
    MODIFY COLUMN `operate_type` varchar(20) DEFAULT NULL COMMENT '操作类型: SUBMIT, APPROVE, REJECT, MODIFY';

ALTER TABLE `brain_editorial_history`
    MODIFY COLUMN `field_diff` json DEFAULT NULL COMMENT '字段差异JSON';
