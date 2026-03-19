-- Roll back the 20260319 certified gateway routing fix to the prior direct-edge layout.

SET @target_tenant := '099142';
SET @flow_code := 'editorial_review_flow';
SET @route_node_code := 'certified-route-node';
SET @operator := 'codex';
SET @now_time := NOW();

SET @definition_id := (
    SELECT id
    FROM flow_definition
    WHERE tenant_id = @target_tenant
      AND flow_code = @flow_code
      AND del_flag = '0'
    ORDER BY id DESC
    LIMIT 1
);

SELECT @target_tenant AS target_tenant,
       @flow_code AS flow_code,
       @definition_id AS definition_id,
       @route_node_code AS route_node_code;

UPDATE flow_skip
SET now_node_code = 'applicant-node',
    now_node_type = 1,
    next_node_code = 'first-review-node',
    next_node_type = 1,
    skip_name = NULL,
    skip_condition = 'ne@@isCertified|true',
    coordinate = '300,160;360,160',
    update_time = @now_time,
    update_by = @operator
WHERE definition_id = @definition_id
  AND now_node_code = 'applicant-node'
  AND next_node_code = @route_node_code;

SELECT ROW_COUNT() AS applicant_to_first_rows;

UPDATE flow_skip
SET now_node_code = 'applicant-node',
    now_node_type = 1,
    next_node_code = 'second-review-node',
    next_node_type = 1,
    skip_name = NULL,
    skip_condition = 'eq@@isCertified|true',
    coordinate = '300,240;540,240',
    update_time = @now_time,
    update_by = @operator
WHERE definition_id = @definition_id
  AND now_node_code = @route_node_code
  AND next_node_code = 'first-review-node';

SELECT ROW_COUNT() AS applicant_to_second_rows;

DELETE FROM flow_skip
WHERE definition_id = @definition_id
  AND now_node_code = @route_node_code
  AND next_node_code = 'second-review-node';

SELECT ROW_COUNT() AS deleted_route_second_rows;

DELETE FROM flow_node
WHERE definition_id = @definition_id
  AND node_code = @route_node_code;

SELECT ROW_COUNT() AS deleted_route_node_rows;

SELECT id,
       node_code,
       node_type,
       coordinate
FROM flow_node
WHERE definition_id = @definition_id
  AND node_code IN ('applicant-node', 'first-review-node', 'second-review-node', @route_node_code)
ORDER BY id;

SELECT id,
       now_node_code,
       next_node_code,
       skip_condition,
       coordinate
FROM flow_skip
WHERE definition_id = @definition_id
  AND (
        now_node_code = 'applicant-node'
        OR now_node_code = @route_node_code
      )
ORDER BY id;
