-- editorial review rebuild: business ddl

ALTER TABLE `brain_editorial_review`
    ADD COLUMN IF NOT EXISTS `review_status` int NOT NULL DEFAULT 0 COMMENT '审校细粒度状态：0草稿 10待一级 20待二级 30待终审 40通过 50退回 60终止 70撤销' AFTER `status`;

ALTER TABLE `brain_editorial_review`
    ADD COLUMN IF NOT EXISTS `process_type` varchar(32) NOT NULL DEFAULT 'AUDIT' COMMENT '流程类型：AUDIT/PROOFREAD' AFTER `review_status`;

UPDATE `brain_editorial_review`
SET `review_status` = CASE
    WHEN `review_status` IS NOT NULL THEN `review_status`
    WHEN `status` = 'draft' THEN 0
    WHEN `status` = 'back' THEN 50
    WHEN `status` = 'cancel' THEN 70
    WHEN `status` = 'termination' THEN 60
    WHEN `status` = 'finish' THEN 40
    ELSE 10
END;

UPDATE `brain_editorial_review`
SET `process_type` = CASE
    WHEN UPPER(TRIM(COALESCE(`process_type`, ''))) IN ('AUDIT', 'PROOFREAD') THEN UPPER(TRIM(`process_type`))
    ELSE 'AUDIT'
END;

ALTER TABLE `brain_editorial_review`
    MODIFY COLUMN `review_status` int NOT NULL DEFAULT 0 COMMENT '审校细粒度状态：0草稿 10待一级 20待二级 30待终审 40通过 50退回 60终止 70撤销';

ALTER TABLE `brain_editorial_review`
    MODIFY COLUMN `process_type` varchar(32) NOT NULL DEFAULT 'AUDIT' COMMENT '流程类型：AUDIT/PROOFREAD';

SET @idx_review_status_exists := (
    SELECT COUNT(1)
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'brain_editorial_review'
      AND index_name = 'idx_brain_editorial_review_review_status'
);
SET @idx_review_status_sql := IF(
    @idx_review_status_exists = 0,
    'CREATE INDEX idx_brain_editorial_review_review_status ON brain_editorial_review (review_status)',
    'SELECT 1'
);
PREPARE stmt_review_status FROM @idx_review_status_sql;
EXECUTE stmt_review_status;
DEALLOCATE PREPARE stmt_review_status;

SET @idx_process_type_exists := (
    SELECT COUNT(1)
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'brain_editorial_review'
      AND index_name = 'idx_brain_editorial_review_process_type'
);
SET @idx_process_type_sql := IF(
    @idx_process_type_exists = 0,
    'CREATE INDEX idx_brain_editorial_review_process_type ON brain_editorial_review (process_type)',
    'SELECT 1'
);
PREPARE stmt_process_type FROM @idx_process_type_sql;
EXECUTE stmt_process_type;
DEALLOCATE PREPARE stmt_process_type;
