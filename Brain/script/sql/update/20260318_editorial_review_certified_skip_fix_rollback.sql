-- Rollback gateway-based certified applicant routing to the previous applicant-node split layout.

SET @target_tenant := '099142';
SET @flow_code := 'editorial_review_flow';
SET @route_node_code := 'certified-route-node';

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
       @definition_id AS definition_id;

SELECT id,
       node_code,
       node_type,
       coordinate
FROM flow_node
WHERE definition_id = @definition_id
  AND node_code IN ('applicant-node', @route_node_code, 'first-review-node', 'second-review-node')
ORDER BY id;

SELECT id,
       now_node_code,
       next_node_code,
       next_node_type,
       skip_condition
FROM flow_skip
WHERE definition_id = @definition_id
  AND (now_node_code = 'applicant-node' OR now_node_code = @route_node_code)
ORDER BY id;

DELETE FROM flow_skip
WHERE definition_id = @definition_id
  AND now_node_code = @route_node_code
  AND next_node_code = 'second-review-node';

SELECT ROW_COUNT() AS route_to_second_deleted_rows;

UPDATE flow_skip
SET now_node_code = 'applicant-node',
    now_node_type = 1,
    next_node_code = 'second-review-node',
    next_node_type = 1,
    skip_name = NULL,
    skip_condition = 'eq@@isCertified|true',
    coordinate = '300,240;540,240'
WHERE definition_id = @definition_id
  AND now_node_code = @route_node_code
  AND next_node_code = 'first-review-node';

SELECT ROW_COUNT() AS applicant_to_second_restored_rows;

UPDATE flow_skip
SET next_node_code = 'first-review-node',
    next_node_type = 1,
    skip_name = NULL,
    skip_condition = 'ne@@isCertified|true',
    coordinate = '300,160;360,160'
WHERE definition_id = @definition_id
  AND now_node_code = 'applicant-node'
  AND next_node_code = @route_node_code;

SELECT ROW_COUNT() AS applicant_to_first_restored_rows;

DELETE FROM flow_node
WHERE definition_id = @definition_id
  AND node_code = @route_node_code;

SELECT ROW_COUNT() AS route_node_deleted_rows;

SELECT id,
       node_code,
       node_type,
       coordinate
FROM flow_node
WHERE definition_id = @definition_id
  AND node_code IN ('applicant-node', @route_node_code, 'first-review-node', 'second-review-node')
ORDER BY id;

SELECT id,
       now_node_code,
       next_node_code,
       next_node_type,
       skip_condition
FROM flow_skip
WHERE definition_id = @definition_id
  AND (now_node_code = 'applicant-node' OR now_node_code = @route_node_code)
ORDER BY id;
