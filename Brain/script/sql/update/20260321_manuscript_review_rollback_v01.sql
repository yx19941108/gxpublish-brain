/*
manuscript_review - Rollback v01

affected tables
- sys_user_role
- sys_user
- sys_role_menu
- sys_role
- sys_menu
- sys_dict_data
- sys_dict_type
- brain_manuscript_review_flow_config
- brain_manuscript_review_history
- brain_manuscript_review_video_marker
- brain_manuscript_review_external_link
- brain_manuscript_review_attachment
- brain_manuscript_review
- brain_manuscript_review_serial

backup action (before apply / before rollback)
- If you already applied DDL/DML, back up manuscript_review business tables first (logical dump recommended).
- If you are rolling back seeds, keep a copy of impacted sys_* rows for audit.

apply order (original)
1) 20260321_manuscript_review_ddl_v01.sql
2) 20260321_manuscript_review_dml_v01.sql

rollback order
1) 20260321_manuscript_review_rollback_v01.sql

NOTE: 本轮仅起草，未 apply。若你已 apply，请在回滚前确认没有业务数据需要保留。
This rollback still fully covers the WP6-B1 write-side columns because the affected manuscript_review tables are dropped as a whole.
*/

/* NOTE: do not rely on session variables (MySQL MCP may use non-sticky connections). */

/* -------------------------------------------------------------------------- */
/* 1) 回滚 DML（先关系表，再主体表）                                                   */
/* -------------------------------------------------------------------------- */
DELETE FROM sys_user_role WHERE user_id IN (64000, 64001, 64002, 64003, 64004)
  OR role_id IN (63000, 63001, 63002, 63003, 63004);

DELETE FROM sys_role_menu
WHERE role_id IN (63000, 63001, 63002, 63003, 63004)
  AND (
    /* module menus + module action perms */
    menu_id IN (60000, 60001, 60002, 60003, 60004, 60005, 60006, 60007)
    /* workflow task menus */
    OR menu_id IN (11618, 11619, 11629, 11632)
    /* read-only cross-module perms */
    OR menu_id IN (1017, 1026, 1600, 1601, 1602)
  );

DELETE FROM sys_user WHERE user_id IN (64000, 64001, 64002, 64003, 64004) AND tenant_id = '000000';

DELETE FROM sys_role WHERE role_id IN (63000, 63001, 63002, 63003, 63004) AND tenant_id = '000000';

DELETE FROM sys_menu WHERE menu_id IN (60000, 60001, 60002, 60003, 60004, 60005, 60006, 60007);

DELETE FROM sys_dict_data
WHERE tenant_id = '000000'
  AND dict_code IN (
    62000, 62001,
    62010, 62011, 62012, 62013, 62014,
    62020, 62021, 62022, 62023, 62024, 62025, 62026,
    62030, 62031, 62032, 62033
  );

DELETE FROM sys_dict_type
WHERE tenant_id = '000000'
  AND dict_id IN (61000, 61001, 61002, 61003)
  AND dict_type IN (
    'manuscript_review_process_type',
    'manuscript_review_flow_status',
    'manuscript_review_current_node',
    'manuscript_review_media_channel'
  );

DELETE FROM brain_manuscript_review_flow_config
WHERE tenant_id = '000000'
  AND id IN (65000, 65001)
  AND process_type IN ('AUDIT', 'PROOFREAD')
  AND flow_code IN ('manuscript_review_audit_flow', 'manuscript_review_proofread_flow');

/* -------------------------------------------------------------------------- */
/* 2) 回滚 DDL（按依赖逆序 drop；历史/资源先于主表）                                      */
/* -------------------------------------------------------------------------- */
DROP TABLE IF EXISTS `brain_manuscript_review_history`;
DROP TABLE IF EXISTS `brain_manuscript_review_video_marker`;
DROP TABLE IF EXISTS `brain_manuscript_review_external_link`;
DROP TABLE IF EXISTS `brain_manuscript_review_attachment`;
DROP TABLE IF EXISTS `brain_manuscript_review_flow_config`;
DROP TABLE IF EXISTS `brain_manuscript_review`;
DROP TABLE IF EXISTS `brain_manuscript_review_serial`;
