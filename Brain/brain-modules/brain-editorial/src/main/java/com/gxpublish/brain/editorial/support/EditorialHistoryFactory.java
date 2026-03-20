package com.gxpublish.brain.editorial.support;

import cn.hutool.core.collection.CollUtil;
import com.gxpublish.brain.common.core.domain.dto.RoleDTO;
import com.gxpublish.brain.common.core.domain.event.ProcessEvent;
import com.gxpublish.brain.common.core.domain.model.LoginUser;
import com.gxpublish.brain.common.core.enums.BusinessStatusEnum;
import com.gxpublish.brain.common.core.service.DeptService;
import com.gxpublish.brain.common.core.utils.StringUtils;
import com.gxpublish.brain.editorial.domain.EditorialHistory;
import com.gxpublish.brain.editorial.domain.EditorialReview;
import com.gxpublish.brain.editorial.domain.annotation.EditorialDiffField;
import com.gxpublish.brain.editorial.domain.bo.EditorialReviewBo;
import com.gxpublish.brain.editorial.domain.vo.EditorialAttachmentVo;
import com.gxpublish.brain.editorial.domain.vo.EditorialLinkVo;
import com.gxpublish.brain.editorial.enums.EditorialRoleEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 审校历史展示工厂。
 */
@Component
@RequiredArgsConstructor
public class EditorialHistoryFactory {

    private static final String APPLICANT_DISPLAY_NAME = "发起人";
    private static final Map<String, String> SCALAR_FIELD_LABELS = resolveScalarFieldLabels();

    private final DeptService deptService;

    public EditorialHistory buildCreateHistory(EditorialReview review,
                                               LoginUser loginUser,
                                               Long operatorId,
                                               String operatorName) {
        String roleName = APPLICANT_DISPLAY_NAME;
        EditorialHistory history = createBaseHistory(review.getId(), "CREATE", operatorId, operatorName, roleName);
        history.setOperateType(roleName + "创建申请" + defaultIfBlank(review.getTitle(), ""));
        history.setFieldDiff(Map.of());
        return history;
    }

    public EditorialHistory buildModifyHistory(EditorialReview oldReview,
                                               EditorialReview newReview,
                                               List<EditorialLinkVo> oldLinks,
                                               List<EditorialLinkVo> newLinks,
                                               List<EditorialAttachmentVo> oldAttachments,
                                               List<EditorialAttachmentVo> newAttachments,
                                               LoginUser loginUser,
                                               Long operatorId,
                                               String operatorName) {
        List<Map<String, Object>> items = new ArrayList<>();
        Map<Long, String> deptNameMap = resolveDeptNames(
            oldReview == null ? null : oldReview.getDeptId(),
            newReview == null ? null : newReview.getDeptId());
        appendScalarDiff(items, "title", oldReview == null ? null : oldReview.getTitle(), newReview == null ? null : newReview.getTitle());
        appendScalarDiff(items, "content", oldReview == null ? null : oldReview.getContent(), newReview == null ? null : newReview.getContent());
        appendScalarDiff(items, "deptId",
            resolveDeptName(deptNameMap, oldReview == null ? null : oldReview.getDeptId()),
            resolveDeptName(deptNameMap, newReview == null ? null : newReview.getDeptId()));
        appendAddedLinks(items, oldLinks, newLinks);
        appendAddedAttachments(items, oldAttachments, newAttachments);
        if (items.isEmpty()) {
            return null;
        }

        String roleName = resolveRoleName(loginUser, null);
        EditorialHistory history = createBaseHistory(newReview != null ? newReview.getId() : oldReview.getId(),
            "MODIFY", operatorId, operatorName, roleName);
        history.setOperateType(formatOperateSentence(operatorName, roleName, "修改了记录"));
        history.setFieldDiff(Map.of("items", items));
        return history;
    }

    public EditorialHistory buildProcessHistory(EditorialReview review,
                                                ProcessEvent processEvent,
                                                LoginUser loginUser,
                                                Long operatorId,
                                                String operatorName) {
        if (Boolean.TRUE.equals(processEvent.getSubmit())) {
            return null;
        }
        String action = resolveApprovalAction(processEvent.getStatus());
        if (action == null) {
            return null;
        }
        String roleName = resolveRoleName(loginUser, processEvent.getNodeCode());
        EditorialHistory history = createBaseHistory(review.getId(), "APPROVAL", operatorId, operatorName, roleName);
        history.setOperateType(formatOperateSentence(operatorName, roleName, action));
        Map<String, Object> fieldDiff = new LinkedHashMap<>();
        fieldDiff.put("action", resolveApprovalActionCode(processEvent.getStatus()));
        if (StringUtils.isNotBlank(processEvent.getNodeName())) {
            fieldDiff.put("nodeName", processEvent.getNodeName());
        }
        Object comment = processEvent.getParams() == null ? null : processEvent.getParams().get("message");
        if (comment != null) {
            fieldDiff.put("comment", comment);
        }
        history.setFieldDiff(fieldDiff);
        return history;
    }

    private EditorialHistory createBaseHistory(Long reviewId,
                                               String eventType,
                                               Long operatorId,
                                               String operatorName,
                                               String operatorRoleName) {
        EditorialHistory history = new EditorialHistory();
        history.setReviewId(reviewId);
        history.setOperatorId(operatorId);
        history.setOperatorName(operatorName);
        history.setOperatorRoleName(operatorRoleName);
        history.setEventType(eventType);
        history.setOperateTime(new Date());
        return history;
    }

    private void appendScalarDiff(List<Map<String, Object>> items, String field, Object oldValue, Object newValue) {
        if (Objects.equals(oldValue, newValue)) {
            return;
        }
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("field", field);
        item.put("label", SCALAR_FIELD_LABELS.getOrDefault(field, field));
        item.put("oldValue", normalizeDisplayValue(oldValue));
        item.put("newValue", normalizeDisplayValue(newValue));
        item.put("displayType", "TEXT");
        items.add(item);
    }

    private void appendAddedLinks(List<Map<String, Object>> items,
                                  List<EditorialLinkVo> oldLinks,
                                  List<EditorialLinkVo> newLinks) {
        List<Map<String, Object>> addedLinks = findAddedLinkDisplays(oldLinks, newLinks);
        if (CollUtil.isEmpty(addedLinks)) {
            return;
        }
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("field", "linkList");
        item.put("label", "新增关联链接");
        item.put("oldValue", List.of());
        item.put("newValue", addedLinks);
        item.put("displayType", "LINK_LIST");
        items.add(item);
    }

    private void appendAddedAttachments(List<Map<String, Object>> items,
                                        List<EditorialAttachmentVo> oldAttachments,
                                        List<EditorialAttachmentVo> newAttachments) {
        List<Map<String, Object>> addedAttachments = findAddedAttachmentDisplays(oldAttachments, newAttachments);
        if (CollUtil.isEmpty(addedAttachments)) {
            return;
        }
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("field", "attachmentList");
        item.put("label", "新增附件");
        item.put("oldValue", List.of());
        item.put("newValue", addedAttachments);
        item.put("displayType", "ATTACHMENT_LIST");
        items.add(item);
    }

    private List<Map<String, Object>> findAddedLinkDisplays(List<EditorialLinkVo> oldLinks, List<EditorialLinkVo> newLinks) {
        Set<String> oldKeys = toLinkKeys(oldLinks);
        if (CollUtil.isEmpty(newLinks)) {
            return List.of();
        }
        return newLinks.stream()
            .filter(link -> !oldKeys.contains(toLinkKey(link)))
            .map(this::toLinkDisplay)
            .toList();
    }

    private List<Map<String, Object>> findAddedAttachmentDisplays(List<EditorialAttachmentVo> oldAttachments,
                                                                  List<EditorialAttachmentVo> newAttachments) {
        Set<String> oldKeys = toAttachmentKeys(oldAttachments);
        if (CollUtil.isEmpty(newAttachments)) {
            return List.of();
        }
        return newAttachments.stream()
            .filter(attachment -> !oldKeys.contains(toAttachmentKey(attachment)))
            .sorted(Comparator.comparing(EditorialAttachmentVo::getVersion, Comparator.nullsLast(Integer::compareTo)))
            .map(this::toAttachmentDisplay)
            .toList();
    }

    private Set<String> toLinkKeys(List<EditorialLinkVo> links) {
        if (CollUtil.isEmpty(links)) {
            return Set.of();
        }
        return links.stream()
            .map(this::toLinkKey)
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private Set<String> toAttachmentKeys(List<EditorialAttachmentVo> attachments) {
        if (CollUtil.isEmpty(attachments)) {
            return Set.of();
        }
        return attachments.stream()
            .map(this::toAttachmentKey)
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private String toLinkKey(EditorialLinkVo link) {
        return normalizeText(link == null ? null : link.getUrl()) + "|" + normalizeText(link == null ? null : link.getDescription());
    }

    private String toAttachmentKey(EditorialAttachmentVo attachment) {
        if (attachment == null) {
            return "";
        }
        if (StringUtils.isNotBlank(attachment.getOssId())) {
            return attachment.getOssId();
        }
        return normalizeText(attachment.getFileName()) + "|" + normalizeText(attachment.getFileUrl());
    }

    private Map<String, Object> toLinkDisplay(EditorialLinkVo link) {
        Map<String, Object> display = new LinkedHashMap<>();
        display.put("description", defaultIfBlank(link.getDescription(), link.getUrl()));
        display.put("url", link.getUrl());
        return display;
    }

    private Map<String, Object> toAttachmentDisplay(EditorialAttachmentVo attachment) {
        Map<String, Object> display = new LinkedHashMap<>();
        display.put("fileName", attachment.getFileName());
        display.put("fileUrl", attachment.getFileUrl());
        display.put("ossId", attachment.getOssId());
        display.put("fileSize", attachment.getFileSize());
        display.put("version", attachment.getVersion());
        return display;
    }

    private Map<Long, String> resolveDeptNames(Long oldDeptId, Long newDeptId) {
        List<Long> deptIds = new ArrayList<>();
        if (oldDeptId != null) {
            deptIds.add(oldDeptId);
        }
        if (newDeptId != null && !Objects.equals(newDeptId, oldDeptId)) {
            deptIds.add(newDeptId);
        }
        if (deptIds.isEmpty()) {
            return Map.of();
        }
        Map<Long, String> deptNameMap = deptService.selectDeptNamesByIds(deptIds);
        return deptNameMap == null ? Map.of() : deptNameMap;
    }

    private String resolveDeptName(Map<Long, String> deptNameMap, Long deptId) {
        if (deptId == null || deptNameMap == null || deptNameMap.isEmpty()) {
            return null;
        }
        return deptNameMap.get(deptId);
    }

    private String resolveRoleName(LoginUser loginUser, String nodeCode) {
        if (loginUser == null || CollUtil.isEmpty(loginUser.getRoles())) {
            return null;
        }
        String preferredRoleKey = switch (normalizeText(nodeCode)) {
            case EditorialReviewWorkflowDefinition.FIRST_REVIEW_NODE_CODE -> EditorialRoleEnum.FIRST_APPROVER.getRoleKey();
            case EditorialReviewWorkflowDefinition.SECOND_REVIEW_NODE_CODE -> EditorialRoleEnum.SECOND_APPROVER.getRoleKey();
            case EditorialReviewWorkflowDefinition.FINAL_REVIEW_NODE_CODE -> EditorialRoleEnum.FINAL_APPROVER.getRoleKey();
            default -> null;
        };
        if (StringUtils.isNotBlank(preferredRoleKey)) {
            for (RoleDTO role : loginUser.getRoles()) {
                if (role != null && preferredRoleKey.equals(role.getRoleKey())) {
                    return defaultIfBlank(role.getRoleName(), preferredRoleKey);
                }
            }
        }
        for (RoleDTO role : loginUser.getRoles()) {
            if (role == null) {
                continue;
            }
            if (StringUtils.isNotBlank(role.getRoleName())) {
                return role.getRoleName();
            }
            if (StringUtils.isNotBlank(role.getRoleKey())) {
                return resolveFallbackRoleName(role.getRoleKey());
            }
        }
        return null;
    }

    private String resolveFallbackRoleName(String roleKey) {
        if (Objects.equals(roleKey, EditorialRoleEnum.APPLICANT.getRoleKey())
            || Objects.equals(roleKey, EditorialRoleEnum.APPLICANT_CER.getRoleKey())) {
            return "发起人";
        }
        if (Objects.equals(roleKey, EditorialRoleEnum.FIRST_APPROVER.getRoleKey())) {
            return "一级审批人";
        }
        if (Objects.equals(roleKey, EditorialRoleEnum.SECOND_APPROVER.getRoleKey())) {
            return "二级审批人";
        }
        if (Objects.equals(roleKey, EditorialRoleEnum.FINAL_APPROVER.getRoleKey())) {
            return "终审人";
        }
        return roleKey;
    }

    private String formatOperateSentence(String operatorName, String roleName, String action) {
        return defaultIfBlank(operatorName, "未知操作人") + "（" + defaultIfBlank(roleName, "未知角色") + "）" + action;
    }

    private String resolveApprovalAction(String status) {
        if (BusinessStatusEnum.CANCEL.getStatus().equals(status)) {
            return "撤销了审批";
        }
        if (BusinessStatusEnum.BACK.getStatus().equals(status)) {
            return "退回了审批";
        }
        if (BusinessStatusEnum.TERMINATION.getStatus().equals(status)) {
            return "终止了审批";
        }
        if (BusinessStatusEnum.WAITING.getStatus().equals(status) || BusinessStatusEnum.FINISH.getStatus().equals(status)) {
            return "审批通过了审批";
        }
        return null;
    }

    private String resolveApprovalActionCode(String status) {
        if (BusinessStatusEnum.CANCEL.getStatus().equals(status)) {
            return "CANCEL";
        }
        if (BusinessStatusEnum.BACK.getStatus().equals(status)) {
            return "BACK";
        }
        if (BusinessStatusEnum.TERMINATION.getStatus().equals(status)) {
            return "TERMINATE";
        }
        if (BusinessStatusEnum.WAITING.getStatus().equals(status) || BusinessStatusEnum.FINISH.getStatus().equals(status)) {
            return "APPROVE";
        }
        return "UNKNOWN";
    }

    private Object normalizeDisplayValue(Object value) {
        return value == null ? "" : value;
    }

    private String normalizeText(String value) {
        return defaultIfBlank(StringUtils.trim(value), "").toLowerCase();
    }

    private String defaultIfBlank(String value, String defaultValue) {
        return StringUtils.isBlank(value) ? defaultValue : value;
    }

    private static Map<String, String> resolveScalarFieldLabels() {
        Map<String, String> labels = new LinkedHashMap<>();
        for (Field field : EditorialReviewBo.class.getDeclaredFields()) {
            EditorialDiffField annotation = field.getAnnotation(EditorialDiffField.class);
            if (annotation != null) {
                labels.put(field.getName(), annotation.value());
            }
        }
        return labels;
    }
}
