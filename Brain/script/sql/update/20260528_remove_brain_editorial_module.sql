/* -------------------------------------------------------------------------- */
/* Owner: Codex                                                               */
/* Task: remove deprecated brain-editorial module database artifacts           */
/* Status: Draft for manual apply only                                        */
/* Updated: 2026-05-28 00:00 +08:00                                           */
/* -------------------------------------------------------------------------- */
/*
scope
- Remove menu/permission entries that point to deleted editorial frontend pages.
- Remove editorial workflow definitions and related runtime workflow rows.
- Remove editorial-specific roles and role bindings.
- Drop deprecated brain_editorial_* business tables.

backup action before apply
- Back up sys_menu, sys_role_menu, sys_role, sys_user_role.
- Back up flow_definition, flow_node, flow_skip, flow_instance, flow_task,
  flow_his_task, flow_instance_biz_ext.
- Back up brain_editorial_review, brain_editorial_attachment,
  brain_editorial_link, brain_editorial_history if historical data must be kept.

rollback
- This is a full removal script. Rollback requires restoring from backup.
*/

SET @target_tenant := '000000';
SET @target_flow_code := 'editorial_review_flow';

DROP TEMPORARY TABLE IF EXISTS tmp_remove_editorial_definition_ids;
CREATE TEMPORARY TABLE tmp_remove_editorial_definition_ids (
  id bigint PRIMARY KEY
);

INSERT IGNORE INTO tmp_remove_editorial_definition_ids (id)
SELECT id
FROM flow_definition
WHERE flow_code = @target_flow_code;

DROP TEMPORARY TABLE IF EXISTS tmp_remove_editorial_business_ids;
CREATE TEMPORARY TABLE tmp_remove_editorial_business_ids (
  business_id varchar(64) PRIMARY KEY
);

SET @has_editorial_review_table := (
  SELECT COUNT(1)
  FROM information_schema.tables
  WHERE table_schema = DATABASE()
    AND table_name = 'brain_editorial_review'
);

SET @insert_editorial_business_ids_sql := IF(
  @has_editorial_review_table > 0,
  'INSERT IGNORE INTO tmp_remove_editorial_business_ids (business_id) SELECT CAST(id AS CHAR(64)) FROM brain_editorial_review',
  'SELECT 1'
);

PREPARE stmt_insert_editorial_business_ids FROM @insert_editorial_business_ids_sql;
EXECUTE stmt_insert_editorial_business_ids;
DEALLOCATE PREPARE stmt_insert_editorial_business_ids;

DROP TEMPORARY TABLE IF EXISTS tmp_remove_editorial_instance_ids;
CREATE TEMPORARY TABLE tmp_remove_editorial_instance_ids (
  id bigint PRIMARY KEY
);

INSERT IGNORE INTO tmp_remove_editorial_instance_ids (id)
SELECT id
FROM flow_instance
WHERE definition_id IN (SELECT id FROM tmp_remove_editorial_definition_ids)
   OR business_id IN (SELECT business_id FROM tmp_remove_editorial_business_ids);

DROP TEMPORARY TABLE IF EXISTS tmp_remove_editorial_menu_ids;
CREATE TEMPORARY TABLE tmp_remove_editorial_menu_ids (
  menu_id bigint PRIMARY KEY
);

INSERT IGNORE INTO tmp_remove_editorial_menu_ids (menu_id)
SELECT menu_id
FROM sys_menu
WHERE path LIKE 'editorial%'
   OR component LIKE 'editorial/%'
   OR perms LIKE 'editorial:%'
   OR perms LIKE 'editorial\\:%';

INSERT IGNORE INTO tmp_remove_editorial_menu_ids (menu_id)
SELECT child.menu_id
FROM sys_menu child
JOIN sys_menu parent ON parent.menu_id = child.parent_id
WHERE parent.path LIKE 'editorial%'
   OR parent.component LIKE 'editorial/%'
   OR parent.perms LIKE 'editorial:%'
   OR parent.perms LIKE 'editorial\\:%';

INSERT IGNORE INTO tmp_remove_editorial_menu_ids (menu_id)
SELECT grandchild.menu_id
FROM sys_menu grandchild
JOIN sys_menu child ON child.menu_id = grandchild.parent_id
JOIN sys_menu parent ON parent.menu_id = child.parent_id
WHERE parent.path LIKE 'editorial%'
   OR parent.component LIKE 'editorial/%'
   OR parent.perms LIKE 'editorial:%'
   OR parent.perms LIKE 'editorial\\:%';

DROP TEMPORARY TABLE IF EXISTS tmp_remove_editorial_role_ids;
CREATE TEMPORARY TABLE tmp_remove_editorial_role_ids (
  role_id bigint PRIMARY KEY
);

INSERT IGNORE INTO tmp_remove_editorial_role_ids (role_id)
SELECT role_id
FROM sys_role
WHERE role_key IN (
    'editorial_review_applicant',
    'editorial_review_applicant_has_certificate',
    'editorial_first_level_approver',
    'editorial_second_level_approver',
    'editorial_third_level_approver'
  )
   OR role_key LIKE 'editorial\\_%';

DELETE FROM flow_instance_biz_ext
WHERE instance_id IN (SELECT id FROM tmp_remove_editorial_instance_ids)
   OR business_id IN (SELECT business_id FROM tmp_remove_editorial_business_ids);

DELETE FROM flow_his_task
WHERE instance_id IN (SELECT id FROM tmp_remove_editorial_instance_ids)
   OR definition_id IN (SELECT id FROM tmp_remove_editorial_definition_ids);

DELETE FROM flow_task
WHERE instance_id IN (SELECT id FROM tmp_remove_editorial_instance_ids)
   OR definition_id IN (SELECT id FROM tmp_remove_editorial_definition_ids);

DELETE FROM flow_instance
WHERE id IN (SELECT id FROM tmp_remove_editorial_instance_ids)
   OR definition_id IN (SELECT id FROM tmp_remove_editorial_definition_ids)
   OR business_id IN (SELECT business_id FROM tmp_remove_editorial_business_ids);

DELETE FROM flow_skip
WHERE definition_id IN (SELECT id FROM tmp_remove_editorial_definition_ids);

DELETE FROM flow_node
WHERE definition_id IN (SELECT id FROM tmp_remove_editorial_definition_ids);

DELETE FROM flow_definition
WHERE id IN (SELECT id FROM tmp_remove_editorial_definition_ids);

DELETE FROM sys_role_menu
WHERE menu_id IN (SELECT menu_id FROM tmp_remove_editorial_menu_ids)
   OR role_id IN (SELECT role_id FROM tmp_remove_editorial_role_ids);

DELETE FROM sys_user_role
WHERE role_id IN (SELECT role_id FROM tmp_remove_editorial_role_ids);

DELETE FROM sys_menu
WHERE menu_id IN (SELECT menu_id FROM tmp_remove_editorial_menu_ids);

DELETE FROM sys_role
WHERE role_id IN (SELECT role_id FROM tmp_remove_editorial_role_ids);

DROP TABLE IF EXISTS brain_editorial_history;
DROP TABLE IF EXISTS brain_editorial_attachment;
DROP TABLE IF EXISTS brain_editorial_link;
DROP TABLE IF EXISTS brain_editorial_review;

DROP TEMPORARY TABLE IF EXISTS tmp_remove_editorial_role_ids;
DROP TEMPORARY TABLE IF EXISTS tmp_remove_editorial_menu_ids;
DROP TEMPORARY TABLE IF EXISTS tmp_remove_editorial_instance_ids;
DROP TEMPORARY TABLE IF EXISTS tmp_remove_editorial_business_ids;
DROP TEMPORARY TABLE IF EXISTS tmp_remove_editorial_definition_ids;
