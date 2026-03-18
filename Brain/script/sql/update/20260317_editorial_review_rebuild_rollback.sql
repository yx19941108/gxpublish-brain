-- editorial review rebuild: business ddl rollback

SET @drop_idx_review_status_exists := (
    SELECT COUNT(1)
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'brain_editorial_review'
      AND index_name = 'idx_brain_editorial_review_review_status'
);
SET @drop_idx_review_status_sql := IF(
    @drop_idx_review_status_exists = 1,
    'DROP INDEX idx_brain_editorial_review_review_status ON brain_editorial_review',
    'SELECT 1'
);
PREPARE stmt_drop_review_status FROM @drop_idx_review_status_sql;
EXECUTE stmt_drop_review_status;
DEALLOCATE PREPARE stmt_drop_review_status;

SET @drop_idx_process_type_exists := (
    SELECT COUNT(1)
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'brain_editorial_review'
      AND index_name = 'idx_brain_editorial_review_process_type'
);
SET @drop_idx_process_type_sql := IF(
    @drop_idx_process_type_exists = 1,
    'DROP INDEX idx_brain_editorial_review_process_type ON brain_editorial_review',
    'SELECT 1'
);
PREPARE stmt_drop_process_type FROM @drop_idx_process_type_sql;
EXECUTE stmt_drop_process_type;
DEALLOCATE PREPARE stmt_drop_process_type;

ALTER TABLE `brain_editorial_review`
    DROP COLUMN IF EXISTS `process_type`;

ALTER TABLE `brain_editorial_review`
    DROP COLUMN IF EXISTS `review_status`;
