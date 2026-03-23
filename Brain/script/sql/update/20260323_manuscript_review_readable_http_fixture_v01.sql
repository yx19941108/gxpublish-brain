/* -------------------------------------------------------------------------- */
/* dev-only manuscript_review readable HTTP fixture                           */
/* Scope: local verification only. Do not promote as a formal upgrade script. */
/* Purpose: prepare one high-sample review for readable ledger/detail checks. */
/* Apply after: 20260321_manuscript_review_ddl_v01.sql                        */
/*             20260321_manuscript_review_dml_v01.sql                         */
/*             20260323_manuscript_review_dev_login_seed_v01.sql              */
/* -------------------------------------------------------------------------- */

INSERT INTO brain_manuscript_review (
  id, tenant_id, process_type, flow_code, flow_instance_id,
  flow_status_label, current_node_label,
  manuscript_code, external_manuscript_code,
  title, content, note, media_channel_dict_code, media_channel_label,
  submitter_dept_id, submitter_dept_name, author_names,
  initiator_user_id, initiator_name,
  create_dept, create_by, create_time, update_by, update_time, remark
) VALUES (
  880000230001, '000000', 'AUDIT', 'manuscript_review_audit_flow', NULL,
  '审批中', '待二级审批',
  'SH20260323991', 'MR-HTTP-20260323-001',
  '审校 HTTP 取证高位样例稿件',
  '这是用于 readable HTTP 取证的高位样例正文，覆盖台账、详情、资源区和时间线的最小展示面。',
  'local verification only 高位样例，不作为正式业务数据。',
  NULL, '集团官网/要闻',
  NULL, '审校部', '张三、李四',
  64000, '审校发起人',
  NULL, 64000, '2026-03-23 09:00:00', 64001, '2026-03-23 09:20:00',
  'dev-only readable HTTP fixture'
);

INSERT INTO brain_manuscript_review_attachment (
  id, tenant_id, review_id,
  oss_id, file_name, file_url, file_size, mime_type, is_video, video_duration_seconds,
  enabled, disabled_by, disabled_time,
  create_dept, create_by, create_time, update_by, update_time, remark
) VALUES
  (
    880000230101, '000000', 880000230001,
    NULL, '采访提纲_v1.docx', 'https://local.example/manuscript-review/http-fixture/interview-outline-v1.docx',
    245760, 'application/vnd.openxmlformats-officedocument.wordprocessingml.document', 0, NULL,
    '1', NULL, NULL,
    NULL, 64000, '2026-03-23 09:02:00', 64000, '2026-03-23 09:02:00', '当前有效普通附件'
  ),
  (
    880000230102, '000000', 880000230001,
    NULL, '样片_v2.mp4', 'https://local.example/manuscript-review/http-fixture/sample-video-v2.mp4',
    104857600, 'video/mp4', 1, 305,
    '1', NULL, NULL,
    NULL, 64000, '2026-03-23 09:03:00', 64000, '2026-03-23 09:03:00', '当前有效视频附件'
  ),
  (
    880000230103, '000000', 880000230001,
    NULL, '旧采访提纲_v0.docx', 'https://local.example/manuscript-review/http-fixture/interview-outline-v0.docx',
    198765, 'application/vnd.openxmlformats-officedocument.wordprocessingml.document', 0, NULL,
    '0', 64000, '2026-03-23 09:18:00',
    NULL, 64000, '2026-03-23 09:01:00', 64000, '2026-03-23 09:18:00', '历史停用普通附件'
  );

INSERT INTO brain_manuscript_review_external_link (
  id, tenant_id, review_id,
  link_title, link_url,
  enabled, disabled_by, disabled_time,
  create_dept, create_by, create_time, update_by, update_time, remark
) VALUES
  (
    880000230201, '000000', 880000230001,
    '参考链接一', 'https://local.example/manuscript-review/http-fixture/reference-1',
    '1', NULL, NULL,
    NULL, 64000, '2026-03-23 09:04:00', 64000, '2026-03-23 09:04:00', '当前有效外链'
  ),
  (
    880000230202, '000000', 880000230001,
    '旧参考链接', 'https://local.example/manuscript-review/http-fixture/reference-old',
    '0', 64000, '2026-03-23 09:19:00',
    NULL, 64000, '2026-03-23 09:02:30', 64000, '2026-03-23 09:19:00', '历史停用外链'
  );

INSERT INTO brain_manuscript_review_video_marker (
  id, tenant_id, review_id, video_attachment_id,
  start_time, end_time, start_seconds, end_seconds, marker_note,
  enabled, disabled_by, disabled_time,
  create_dept, create_by, create_time, update_by, update_time, remark
) VALUES
  (
    880000230301, '000000', 880000230001, 880000230102,
    '00:00:12', '00:00:30', 12, 30, '当前有效视频标注：请复核片头字幕。',
    '1', NULL, NULL,
    NULL, 64002, '2026-03-23 09:21:00', 64002, '2026-03-23 09:21:00', '当前有效标注'
  ),
  (
    880000230302, '000000', 880000230001, 880000230102,
    '00:01:05', NULL, 65, NULL, '历史停用标注：旧版旁白位置。',
    '0', 64002, '2026-03-23 09:24:00',
    NULL, 64002, '2026-03-23 09:22:00', 64002, '2026-03-23 09:24:00', '历史停用标注'
  );

INSERT INTO brain_manuscript_review_history (
  id, tenant_id, review_id,
  action_type, action_text, actor_user_id, actor_name,
  create_time, ext_json
) VALUES
  (
    880000230401, '000000', 880000230001,
    'CREATE', '审校发起人新增了流程。', 64000, '审校发起人',
    '2026-03-23 09:00:00', NULL
  ),
  (
    880000230402, '000000', 880000230001,
    'UPLOAD_ATTACHMENT', '审校发起人上传了新附件《采访提纲_v1.docx》。', 64000, '审校发起人',
    '2026-03-23 09:02:00', NULL
  ),
  (
    880000230403, '000000', 880000230001,
    'LEVEL_ONE_APPROVED', '审校一级审批人审批通过，流程进入待二级审批。', 64001, '审校一级审批人',
    '2026-03-23 09:20:00', NULL
  );
