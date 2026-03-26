-- Owner: Codex
-- Task: manuscript_review WP6-G1 node status and history sorting fix
-- Status: Draft
-- Updated: 2026-03-26 14:20 +08:00

-- 说明：
-- 1. 新增 current_node_status，避免读写侧直接依赖 workflow 原始节点名（一级审批/二级审批/三级审批）。
-- 2. 新增 history.sorted，稳定 CREATE -> SKIP_LEVEL_1 及同秒时间线顺序。
-- 3. 兼容既有数据：回填状态码，并把 current_node_label 规范为冻结业务文案。

ALTER TABLE `brain_manuscript_review`
    ADD COLUMN IF NOT EXISTS `current_node_status` varchar(40) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '当前节点状态码（LEVEL_1/LEVEL_2/LEVEL_3/RETURN_TO_INITIATOR/FLOW_FINISHED/FLOW_CANCELED/FLOW_REJECTED）' AFTER `flow_status_label`;

UPDATE `brain_manuscript_review`
SET
    `current_node_status` = CASE
        WHEN `current_node_label` IN ('待一级审批', '一级审批') THEN 'LEVEL_1'
        WHEN `current_node_label` IN ('待二级审批', '二级审批') THEN 'LEVEL_2'
        WHEN `current_node_label` IN ('待三级审批', '三级审批') THEN 'LEVEL_3'
        WHEN `current_node_label` = '待发起人处理' THEN 'RETURN_TO_INITIATOR'
        WHEN `current_node_label` = '流程完成' THEN 'FLOW_FINISHED'
        WHEN `current_node_label` = '流程已取消' THEN 'FLOW_CANCELED'
        WHEN `current_node_label` = '流程已驳回' THEN 'FLOW_REJECTED'
        ELSE `current_node_status`
    END,
    `current_node_label` = CASE
        WHEN `current_node_label` IN ('待一级审批', '一级审批') THEN '待一级审批'
        WHEN `current_node_label` IN ('待二级审批', '二级审批') THEN '待二级审批'
        WHEN `current_node_label` IN ('待三级审批', '三级审批') THEN '待三级审批'
        ELSE `current_node_label`
    END
WHERE `current_node_status` IS NULL
   OR `current_node_status` = ''
   OR `current_node_label` IN ('一级审批', '二级审批', '三级审批');

ALTER TABLE `brain_manuscript_review_history`
    ADD COLUMN IF NOT EXISTS `sorted` int DEFAULT NULL COMMENT '同一流程内的稳定排序号' AFTER `create_time`;

SET @mr_review_id := NULL;
SET @mr_sorted := 0;

UPDATE `brain_manuscript_review_history` target
JOIN (
    SELECT ordered.`id`,
           @mr_sorted := IF(@mr_review_id = ordered.`review_id`, @mr_sorted + 1, 1) AS next_sorted,
           @mr_review_id := ordered.`review_id` AS review_marker
    FROM (
        SELECT `id`,
               `review_id`,
               `create_time`,
               CASE `action_type`
                   WHEN 'CREATE' THEN 10
                   WHEN 'SKIP_LEVEL_1' THEN 20
                   ELSE 100
               END AS action_priority
        FROM `brain_manuscript_review_history`
        ORDER BY `review_id`, `create_time`, action_priority, `id`
    ) ordered
) seq
    ON seq.`id` = target.`id`
SET target.`sorted` = seq.next_sorted
WHERE target.`sorted` IS NULL;

ALTER TABLE `brain_manuscript_review_history`
    ADD INDEX `idx_tenant_review_sorted` (`tenant_id`, `review_id`, `sorted`, `create_time`);
