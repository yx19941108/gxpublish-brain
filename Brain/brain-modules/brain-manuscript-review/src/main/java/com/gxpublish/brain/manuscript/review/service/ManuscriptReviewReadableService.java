package com.gxpublish.brain.manuscript.review.service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.gxpublish.brain.common.core.exception.ServiceException;
import com.gxpublish.brain.common.mybatis.core.page.TableDataInfo;
import com.gxpublish.brain.manuscript.review.controller.request.ManuscriptReviewLedgerQueryRequest;
import com.gxpublish.brain.manuscript.review.controller.response.ManuscriptReviewDetailResponse;
import com.gxpublish.brain.manuscript.review.controller.response.ManuscriptReviewLedgerItemResponse;
import com.gxpublish.brain.manuscript.review.domain.entity.ManuscriptReviewAttachmentEntity;
import com.gxpublish.brain.manuscript.review.domain.entity.ManuscriptReviewExternalLinkEntity;
import com.gxpublish.brain.manuscript.review.domain.entity.ManuscriptReviewFlowConfigEntity;
import com.gxpublish.brain.manuscript.review.domain.entity.ManuscriptReviewHistoryEntity;
import com.gxpublish.brain.manuscript.review.domain.entity.ManuscriptReviewRecordEntity;
import com.gxpublish.brain.manuscript.review.domain.entity.ManuscriptReviewSystemRoleEntity;
import com.gxpublish.brain.manuscript.review.domain.entity.ManuscriptReviewSystemUserEntity;
import com.gxpublish.brain.manuscript.review.domain.entity.ManuscriptReviewSystemUserRoleEntity;
import com.gxpublish.brain.manuscript.review.domain.entity.ManuscriptReviewVideoMarkerEntity;
import com.gxpublish.brain.manuscript.review.domain.enums.ManuscriptReviewDetailAction;
import com.gxpublish.brain.manuscript.review.domain.enums.ManuscriptReviewNodeStatusEnum;
import com.gxpublish.brain.manuscript.review.domain.enums.ManuscriptReviewProcessType;
import com.gxpublish.brain.manuscript.review.domain.policy.ManuscriptReviewDetailPermissionContext;
import com.gxpublish.brain.manuscript.review.domain.policy.ManuscriptReviewDetailPermissionPolicy;
import com.gxpublish.brain.manuscript.review.domain.policy.ManuscriptReviewDetailPermissionResult;
import com.gxpublish.brain.manuscript.review.gateway.ManuscriptReviewCurrentUserGateway;
import com.gxpublish.brain.manuscript.review.mapper.ManuscriptReviewAttachmentMapper;
import com.gxpublish.brain.manuscript.review.mapper.ManuscriptReviewExternalLinkMapper;
import com.gxpublish.brain.manuscript.review.mapper.ManuscriptReviewFlowConfigMapper;
import com.gxpublish.brain.manuscript.review.mapper.ManuscriptReviewHistoryMapper;
import com.gxpublish.brain.manuscript.review.mapper.ManuscriptReviewRecordMapper;
import com.gxpublish.brain.manuscript.review.mapper.ManuscriptReviewSystemRoleMapper;
import com.gxpublish.brain.manuscript.review.mapper.ManuscriptReviewSystemUserMapper;
import com.gxpublish.brain.manuscript.review.mapper.ManuscriptReviewSystemUserRoleMapper;
import com.gxpublish.brain.manuscript.review.mapper.ManuscriptReviewVideoMarkerMapper;

@Service
public class ManuscriptReviewReadableService {

    private static final String ENABLED = "1";
    private static final String ACTIVE = "0";
    private static final String REVIEW_NOT_FOUND_MESSAGE = "稿件审校流程不存在";
    private static final String CURRENT_USER_REQUIRED_MESSAGE = "当前登录用户不存在";
    private static final String VIEW_PERMISSION_DENIED_MESSAGE = "当前用户无权查看该流程";
    private static final String DEFAULT_TENANT_ID = "000000";
    private static final String LEVEL_ONE_NODE = ManuscriptReviewNodeStatusEnum.LEVEL_1.getLabel();
    private static final String LEVEL_TWO_NODE = ManuscriptReviewNodeStatusEnum.LEVEL_2.getLabel();
    private static final String LEVEL_THREE_NODE = ManuscriptReviewNodeStatusEnum.LEVEL_3.getLabel();
    private static final ZoneId BUSINESS_ZONE_ID = ZoneId.of("Asia/Shanghai");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final ManuscriptReviewRecordMapper recordMapper;
    private final ManuscriptReviewAttachmentMapper attachmentMapper;
    private final ManuscriptReviewExternalLinkMapper externalLinkMapper;
    private final ManuscriptReviewHistoryMapper historyMapper;
    private final ManuscriptReviewVideoMarkerMapper videoMarkerMapper;
    private final ManuscriptReviewFlowConfigMapper flowConfigMapper;
    private final ManuscriptReviewSystemRoleMapper roleMapper;
    private final ManuscriptReviewSystemUserRoleMapper userRoleMapper;
    private final ManuscriptReviewSystemUserMapper userMapper;
    private final ManuscriptReviewCurrentUserGateway currentUserGateway;
    private final ManuscriptReviewDetailPermissionPolicy permissionPolicy = new ManuscriptReviewDetailPermissionPolicy();

    public ManuscriptReviewReadableService(ManuscriptReviewRecordMapper recordMapper,
                                           ManuscriptReviewAttachmentMapper attachmentMapper,
                                           ManuscriptReviewExternalLinkMapper externalLinkMapper,
                                           ManuscriptReviewHistoryMapper historyMapper,
                                           ManuscriptReviewVideoMarkerMapper videoMarkerMapper,
                                           ManuscriptReviewFlowConfigMapper flowConfigMapper,
                                           ManuscriptReviewSystemRoleMapper roleMapper,
                                           ManuscriptReviewSystemUserRoleMapper userRoleMapper,
                                           ManuscriptReviewSystemUserMapper userMapper,
                                           ManuscriptReviewCurrentUserGateway currentUserGateway) {
        this.recordMapper = recordMapper;
        this.attachmentMapper = attachmentMapper;
        this.externalLinkMapper = externalLinkMapper;
        this.historyMapper = historyMapper;
        this.videoMarkerMapper = videoMarkerMapper;
        this.flowConfigMapper = flowConfigMapper;
        this.roleMapper = roleMapper;
        this.userRoleMapper = userRoleMapper;
        this.userMapper = userMapper;
        this.currentUserGateway = currentUserGateway;
    }

    public TableDataInfo<ManuscriptReviewLedgerItemResponse> listLedger(ManuscriptReviewLedgerQueryRequest request) {
        ManuscriptReviewLedgerQueryRequest safeRequest = request == null ? new ManuscriptReviewLedgerQueryRequest() : request;
        List<ManuscriptReviewHistoryEntity> historyRecords = firstNonNull(
            historyMapper.selectList(new QueryWrapper<ManuscriptReviewHistoryEntity>()),
            List.of()
        );
        Map<Long, List<ManuscriptReviewHistoryEntity>> historiesByReviewId = historyRecords
            .stream()
            .filter(history -> history.getReviewId() != null)
            .collect(Collectors.groupingBy(ManuscriptReviewHistoryEntity::getReviewId));
        List<ManuscriptReviewLedgerItemResponse> rows = recordMapper.selectList(new QueryWrapper<ManuscriptReviewRecordEntity>())
            .stream()
            .filter(record -> matchesLedgerFilter(record, safeRequest))
            .filter(record -> canViewRecord(record, historiesByReviewId.getOrDefault(record.getId(), List.of())))
            .sorted(this::compareLedgerRecord)
            .map(this::toLedgerItem)
            .toList();
        int pageNum = safePageNum(safeRequest.getPageNum());
        int pageSize = safePageSize(safeRequest.getPageSize());
        int fromIndex = Math.min((pageNum - 1) * pageSize, rows.size());
        int toIndex = Math.min(fromIndex + pageSize, rows.size());
        return new TableDataInfo<>(rows.subList(fromIndex, toIndex), rows.size());
    }

    public ManuscriptReviewDetailResponse getDetail(Long reviewId) {
        ManuscriptReviewRecordEntity record = recordMapper.selectById(reviewId);
        if (record == null) {
            throw new ServiceException(REVIEW_NOT_FOUND_MESSAGE);
        }

        List<ManuscriptReviewAttachmentEntity> attachments = firstNonNull(
            attachmentMapper.selectList(new QueryWrapper<ManuscriptReviewAttachmentEntity>().eq("review_id", reviewId)),
            List.of()
        );
        List<ManuscriptReviewExternalLinkEntity> externalLinks = firstNonNull(
            externalLinkMapper.selectList(new QueryWrapper<ManuscriptReviewExternalLinkEntity>().eq("review_id", reviewId)),
            List.of()
        );
        List<ManuscriptReviewHistoryEntity> histories = firstNonNull(
            historyMapper.selectList(new QueryWrapper<ManuscriptReviewHistoryEntity>().eq("review_id", reviewId)),
            List.of()
        );
        List<ManuscriptReviewVideoMarkerEntity> videoMarkers = firstNonNull(
            videoMarkerMapper.selectList(new QueryWrapper<ManuscriptReviewVideoMarkerEntity>().eq("review_id", reviewId)),
            List.of()
        );
        if (!canViewRecord(record, histories)) {
            throw new ServiceException(VIEW_PERMISSION_DENIED_MESSAGE);
        }

        List<ManuscriptReviewAttachmentEntity> currentAttachments = attachments.stream()
            .filter(attachment -> !Boolean.TRUE.equals(attachment.getIsVideo()))
            .filter(this::isEnabled)
            .sorted(Comparator.comparing(ManuscriptReviewAttachmentEntity::getCreateTime, Comparator.nullsLast(Date::compareTo)))
            .toList();
        List<ManuscriptReviewAttachmentEntity> currentVideos = attachments.stream()
            .filter(attachment -> Boolean.TRUE.equals(attachment.getIsVideo()))
            .filter(this::isEnabled)
            .sorted(Comparator.comparing(ManuscriptReviewAttachmentEntity::getCreateTime, Comparator.nullsLast(Date::compareTo)))
            .toList();
        List<ManuscriptReviewExternalLinkEntity> currentExternalLinks = externalLinks.stream()
            .filter(this::isEnabled)
            .sorted(Comparator.comparing(ManuscriptReviewExternalLinkEntity::getCreateTime, Comparator.nullsLast(Date::compareTo)))
            .toList();
        List<ManuscriptReviewVideoMarkerEntity> currentVideoMarks = currentVideos.isEmpty()
            ? List.of()
            : videoMarkers.stream()
                .filter(this::isEnabled)
                .filter(marker -> Objects.equals(marker.getVideoAttachmentId(), currentVideos.get(0).getId()))
                .sorted(Comparator.comparing(ManuscriptReviewVideoMarkerEntity::getStartSeconds, Comparator.nullsLast(Integer::compareTo)))
                .toList();

        ManuscriptReviewDetailResponse response = new ManuscriptReviewDetailResponse();
        response.setId(record.getId());
        response.setProcessType(record.getProcessType());
        response.setProcessTypeLabel(resolveProcessTypeLabel(record.getProcessType()));
        response.setManuscriptCode(record.getManuscriptCode());
        response.setExternalManuscriptCode(record.getExternalManuscriptCode());
        response.setTitle(record.getTitle());
        response.setMediaChannel(record.getMediaChannel());
        response.setSubmitDepartment(record.getSubmitDepartment());
        response.setAuthorName(record.getAuthorName());
        response.setRemark(record.getRemarkText());
        response.setContentBody(record.getContentBody());
        response.setContentSummary(summarizeContent(record.getContentBody()));
        response.setBusinessStatus(mapBusinessStatusCode(record.getFlowStatusLabel()));
        response.setBusinessStatusLabel(record.getFlowStatusLabel());
        ManuscriptReviewNodeStatusEnum currentNodeStatus = resolveCurrentNodeStatus(record);
        response.setCurrentNodeCode(currentNodeStatus == null ? mapCurrentNodeCode(record.getCurrentNodeLabel()) : currentNodeStatus.getCode());
        response.setCurrentNodeStatus(currentNodeStatus == null ? null : currentNodeStatus.getCode());
        response.setCurrentNodeLabel(resolveDisplayCurrentNodeLabel(record));
        response.setInitiatorName(record.getInitiatorName());
        response.setFirstSubmitTime(formatDate(record.getFirstSubmitTime()));
        response.setLatestSubmitTime(formatDate(record.getLatestSubmitTime()));
        response.setUpdateTime(formatDate(firstNonNull(record.getUpdateTime(), record.getCreateTime())));
        response.setAttachmentList(currentAttachments.stream().map(this::toAttachmentItem).toList());
        response.setExternalLinkList(currentExternalLinks.stream().map(this::toExternalLinkItem).toList());
        response.setVideoList(currentVideos.stream().map(this::toVideoItem).toList());
        response.setVideoMarkList(currentVideoMarks.stream().map(this::toVideoMarkItem).toList());
        response.setTimelineItems(buildTimelineItems(histories));
        response.setPermissionMatrix(buildPermissionMatrix(record, histories));
        return response;
    }

    public ManuscriptReviewDetailResponse.ResourceItemVO getResourceItem(Long reviewId, Long resourceId) {
        ManuscriptReviewAttachmentEntity attachment = attachmentMapper.selectById(resourceId);
        if (attachment != null && Objects.equals(attachment.getReviewId(), reviewId)) {
            return Boolean.TRUE.equals(attachment.getIsVideo()) ? toVideoItem(attachment) : toAttachmentItem(attachment);
        }
        ManuscriptReviewExternalLinkEntity externalLink = externalLinkMapper.selectById(resourceId);
        if (externalLink != null && Objects.equals(externalLink.getReviewId(), reviewId)) {
            return toExternalLinkItem(externalLink);
        }
        throw new ServiceException("资源不存在");
    }

    public ManuscriptReviewDetailResponse.VideoMarkItemVO getVideoMarkItem(Long reviewId, Long markId) {
        ManuscriptReviewVideoMarkerEntity videoMarker = videoMarkerMapper.selectById(markId);
        if (videoMarker == null || !Objects.equals(videoMarker.getReviewId(), reviewId)) {
            throw new ServiceException("视频标注不存在");
        }
        return toVideoMarkItem(videoMarker);
    }

    private boolean matchesLedgerFilter(ManuscriptReviewRecordEntity record, ManuscriptReviewLedgerQueryRequest request) {
        if (!matchesKeyword(record, request.getKeyword())) {
            return false;
        }
        if (!matchesExact(trimToNull(request.getProcessType()), trimToNull(record.getProcessType()))) {
            return false;
        }
        if (!matchesExact(trimToNull(request.getMediaChannel()), trimToNull(record.getMediaChannel()))) {
            return false;
        }
        if (!matchesExact(trimToNull(request.getBusinessStatus()), mapBusinessStatusCode(record.getFlowStatusLabel()))) {
            return false;
        }
        ManuscriptReviewNodeStatusEnum currentNodeStatus = resolveCurrentNodeStatus(record);
        if (!matchesExact(trimToNull(request.getCurrentNodeCode()),
            currentNodeStatus == null ? mapCurrentNodeCode(record.getCurrentNodeLabel()) : currentNodeStatus.getCode())) {
            return false;
        }
        return matchesStartTimeRange(record, request.getStartTimeFrom(), request.getStartTimeTo());
    }

    private boolean matchesKeyword(ManuscriptReviewRecordEntity record, String keyword) {
        String normalizedKeyword = trimToNull(keyword);
        if (normalizedKeyword == null) {
            return true;
        }
        return contains(record.getManuscriptCode(), normalizedKeyword)
            || contains(record.getExternalManuscriptCode(), normalizedKeyword)
            || contains(record.getTitle(), normalizedKeyword);
    }

    private boolean contains(String source, String keyword) {
        return source != null && source.contains(keyword);
    }

    private boolean matchesExact(String expected, String actual) {
        return expected == null || Objects.equals(expected, actual);
    }

    private boolean matchesStartTimeRange(ManuscriptReviewRecordEntity record, String startTimeFrom, String startTimeTo) {
        Date startTime = firstNonNull(record.getFirstSubmitTime(), record.getCreateTime());
        if (startTime == null) {
            return trimToNull(startTimeFrom) == null && trimToNull(startTimeTo) == null;
        }
        LocalDateTime actual = toLocalDateTime(startTime);
        LocalDateTime from = parseDateTime(startTimeFrom);
        LocalDateTime to = parseDateTime(startTimeTo);
        return (from == null || !actual.isBefore(from)) && (to == null || !actual.isAfter(to));
    }

    private int compareLedgerRecord(ManuscriptReviewRecordEntity left, ManuscriptReviewRecordEntity right) {
        return Comparator
            .comparing((ManuscriptReviewRecordEntity record) -> firstNonNull(record.getUpdateTime(), record.getCreateTime()),
                Comparator.nullsLast(Date::compareTo))
            .thenComparing(ManuscriptReviewRecordEntity::getCreateTime, Comparator.nullsLast(Date::compareTo))
            .thenComparing(ManuscriptReviewRecordEntity::getId, Comparator.nullsLast(Long::compareTo))
            .reversed()
            .compare(left, right);
    }

    private ManuscriptReviewLedgerItemResponse toLedgerItem(ManuscriptReviewRecordEntity record) {
        ManuscriptReviewLedgerItemResponse response = new ManuscriptReviewLedgerItemResponse();
        ManuscriptReviewNodeStatusEnum currentNodeStatus = resolveCurrentNodeStatus(record);
        response.setId(record.getId());
        response.setProcessType(record.getProcessType());
        response.setProcessTypeLabel(resolveProcessTypeLabel(record.getProcessType()));
        response.setManuscriptCode(record.getManuscriptCode());
        response.setTitle(record.getTitle());
        response.setMediaChannel(record.getMediaChannel());
        response.setBusinessStatus(mapBusinessStatusCode(record.getFlowStatusLabel()));
        response.setBusinessStatusLabel(record.getFlowStatusLabel());
        response.setCurrentNodeCode(currentNodeStatus == null ? mapCurrentNodeCode(record.getCurrentNodeLabel()) : currentNodeStatus.getCode());
        response.setCurrentNodeLabel(resolveDisplayCurrentNodeLabel(record));
        response.setInitiatorName(record.getInitiatorName());
        response.setUpdateTime(formatDate(firstNonNull(record.getUpdateTime(), record.getCreateTime())));
        return response;
    }

    private List<ManuscriptReviewDetailResponse.TimelineItemVO> buildTimelineItems(List<ManuscriptReviewHistoryEntity> histories) {
        return histories.stream()
            .sorted(this::compareTimelineHistory)
            .map(history -> new ManuscriptReviewDetailResponse.TimelineItemVO(
                formatDate(history.getCreateTime()),
                "WORKFLOW",
                "流程",
                trimToNull(history.getActionType()),
                normalizeText(history.getActionText()),
                trimToNull(history.getActorName()),
                null,
                null,
                null,
                null))
            .toList();
    }

    private ManuscriptReviewDetailResponse.PermissionMatrixVO buildPermissionMatrix(ManuscriptReviewRecordEntity record,
                                                                                    List<ManuscriptReviewHistoryEntity> histories) {
        Set<Long> historyParticipantUserIds = histories.stream()
            .map(ManuscriptReviewHistoryEntity::getActorUserId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
        Long currentUserId = requireCurrentUserId();
        ManuscriptReviewDetailPermissionResult permissionResult = permissionPolicy.resolve(
            ManuscriptReviewDetailPermissionContext.builder()
                .currentUserId(currentUserId)
                .initiatorUserId(record.getInitiatorUserId())
                .currentApproverUserIds(resolveCurrentApproverUserIds(record))
                .historyParticipantUserIds(historyParticipantUserIds)
                .returnedToInitiator(isReturnedToInitiator(record))
                .build()
        );
        boolean isInitiator = Objects.equals(currentUserId, record.getInitiatorUserId());
        boolean isCurrentApprover = permissionResult.getAllowedActions().contains(ManuscriptReviewDetailAction.GO_APPROVE);
        boolean isHistoryParticipant = historyParticipantUserIds.contains(currentUserId);
        boolean canView = isInitiator || isCurrentApprover || isHistoryParticipant;
        String businessStatus = mapBusinessStatusCode(record.getFlowStatusLabel());
        boolean canGotoApproval = permissionResult.getAllowedActions().contains(ManuscriptReviewDetailAction.GO_APPROVE);
        boolean canResubmit = permissionResult.getAllowedActions().contains(ManuscriptReviewDetailAction.RESUBMIT);
        boolean canCancel = isInitiator && !isCurrentApprover && "WAITING".equals(businessStatus);
        String buttonReason = resolveButtonReason(businessStatus, canView, isHistoryParticipant);
        return new ManuscriptReviewDetailResponse.PermissionMatrixVO(
            isInitiator,
            isCurrentApprover,
            isHistoryParticipant,
            canView,
            permissionResult.canModify(),
            canResubmit,
            canCancel,
            canGotoApproval,
            buttonReason);
    }

    private ManuscriptReviewDetailResponse.ResourceItemVO toAttachmentItem(ManuscriptReviewAttachmentEntity attachment) {
        return new ManuscriptReviewDetailResponse.ResourceItemVO(
            attachment.getId(),
            "ATTACHMENT",
            "附件",
            attachment.getFileName(),
            null,
            formatDate(attachment.getCreateTime()),
            attachment.getFileUrl());
    }

    private ManuscriptReviewDetailResponse.ResourceItemVO toVideoItem(ManuscriptReviewAttachmentEntity video) {
        return new ManuscriptReviewDetailResponse.ResourceItemVO(
            video.getId(),
            "VIDEO",
            "视频",
            video.getFileName(),
            null,
            formatDate(video.getCreateTime()),
            video.getFileUrl());
    }

    private ManuscriptReviewDetailResponse.ResourceItemVO toExternalLinkItem(ManuscriptReviewExternalLinkEntity externalLink) {
        return new ManuscriptReviewDetailResponse.ResourceItemVO(
            externalLink.getId(),
            "EXTERNAL_LINK",
            "外链",
            externalLink.getLinkTitle(),
            externalLink.getLinkUrl(),
            formatDate(externalLink.getCreateTime()),
            null);
    }

    private ManuscriptReviewDetailResponse.VideoMarkItemVO toVideoMarkItem(ManuscriptReviewVideoMarkerEntity marker) {
        return new ManuscriptReviewDetailResponse.VideoMarkItemVO(
            marker.getId(),
            marker.getStartTime(),
            marker.getEndTime(),
            marker.getMarkerNote());
    }

    private boolean canViewRecord(ManuscriptReviewRecordEntity record, List<ManuscriptReviewHistoryEntity> histories) {
        Long currentUserId = requireCurrentUserId();
        if (Objects.equals(currentUserId, record.getInitiatorUserId())) {
            return true;
        }
        if (resolveCurrentApproverUserIds(record).contains(currentUserId)) {
            return true;
        }
        return histories.stream()
            .map(ManuscriptReviewHistoryEntity::getActorUserId)
            .filter(Objects::nonNull)
            .anyMatch(currentUserId::equals);
    }

    private boolean isReturnedToInitiator(ManuscriptReviewRecordEntity record) {
        return "已退回".equals(record.getFlowStatusLabel())
            || resolveCurrentNodeStatus(record) == ManuscriptReviewNodeStatusEnum.RETURN_TO_INITIATOR;
    }

    private Set<Long> resolveCurrentApproverUserIds(ManuscriptReviewRecordEntity record) {
        String roleKey = resolveCurrentNodeRoleKey(record);
        if (roleKey == null) {
            return Collections.emptySet();
        }
        String tenantId = normalizeTenantId(record.getTenantId());
        List<Long> roleIds = roleMapper.selectList(
            new QueryWrapper<ManuscriptReviewSystemRoleEntity>()
                .eq("tenant_id", tenantId)
                .eq("role_key", roleKey)
                .eq("status", ACTIVE)
                .eq("del_flag", ACTIVE)).stream()
            .map(ManuscriptReviewSystemRoleEntity::getRoleId)
            .filter(Objects::nonNull)
            .distinct()
            .toList();
        if (roleIds.isEmpty()) {
            return Collections.emptySet();
        }
        List<Long> userIds = userRoleMapper.selectList(new QueryWrapper<ManuscriptReviewSystemUserRoleEntity>().in("role_id", roleIds))
            .stream()
            .map(ManuscriptReviewSystemUserRoleEntity::getUserId)
            .filter(Objects::nonNull)
            .distinct()
            .toList();
        if (userIds.isEmpty()) {
            return Collections.emptySet();
        }
        return userMapper.selectList(
            new QueryWrapper<ManuscriptReviewSystemUserEntity>()
                .in("user_id", userIds)
                .eq("tenant_id", tenantId)
                .eq("status", ACTIVE)
                .eq("del_flag", ACTIVE)).stream()
            .map(ManuscriptReviewSystemUserEntity::getUserId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
    }

    private String resolveCurrentNodeRoleKey(ManuscriptReviewRecordEntity record) {
        ManuscriptReviewNodeStatusEnum currentNodeStatus = resolveCurrentNodeStatus(record);
        if (currentNodeStatus == null) {
            return null;
        }
        ManuscriptReviewFlowConfigEntity flowConfig = flowConfigMapper.selectOne(
            new QueryWrapper<ManuscriptReviewFlowConfigEntity>()
                .eq("tenant_id", normalizeTenantId(record.getTenantId()))
                .eq("process_type", record.getProcessType())
                .eq("status", ACTIVE)
                .orderByDesc("id")
                .last("limit 1"));
        if (flowConfig == null) {
            return null;
        }
        return switch (currentNodeStatus) {
            case LEVEL_1 -> trimToNull(flowConfig.getLevelOneRoleKey());
            case LEVEL_2 -> trimToNull(flowConfig.getLevelTwoRoleKey());
            case LEVEL_3 -> trimToNull(flowConfig.getLevelThreeRoleKey());
            default -> null;
        };
    }

    private String summarizeContent(String contentBody) {
        String normalized = trimToNull(contentBody);
        if (normalized == null) {
            return null;
        }
        return normalized.length() <= 200 ? normalized : normalized.substring(0, 200);
    }

    private String resolveButtonReason(String businessStatus, boolean canView, boolean isHistoryParticipant) {
        if (!canView) {
            return "当前用户无权查看该流程";
        }
        if ("REJECT".equals(businessStatus)) {
            return "流程已驳回，不可继续操作";
        }
        if ("CANCEL".equals(businessStatus)) {
            return "流程已取消，不可继续操作";
        }
        if ("FINISH".equals(businessStatus)) {
            return "流程已完成，不可继续操作";
        }
        if (isHistoryParticipant) {
            return "当前用户仅可查看历史参与记录";
        }
        return null;
    }

    private String mapBusinessStatusCode(String businessStatusLabel) {
        if (businessStatusLabel == null) {
            return null;
        }
        return switch (businessStatusLabel) {
            case "审批中" -> "WAITING";
            case "已退回" -> "BACK";
            case "已完成", "流程完成" -> "FINISH";
            case "已取消", "流程已取消" -> "CANCEL";
            case "已驳回", "流程已驳回" -> "REJECT";
            default -> null;
        };
    }

    private String mapCurrentNodeCode(String currentNodeLabel) {
        ManuscriptReviewNodeStatusEnum currentNodeStatus = ManuscriptReviewNodeStatusEnum.fromAnyLabel(currentNodeLabel);
        return currentNodeStatus == null ? null : currentNodeStatus.getCode();
    }

    private String resolveProcessTypeLabel(String processType) {
        if (processType == null) {
            return null;
        }
        for (ManuscriptReviewProcessType value : ManuscriptReviewProcessType.values()) {
            if (value.name().equals(processType)) {
                return value.getDisplayName();
            }
        }
        return processType;
    }

    private ManuscriptReviewNodeStatusEnum resolveCurrentNodeStatus(ManuscriptReviewRecordEntity record) {
        if (record == null) {
            return null;
        }
        ManuscriptReviewNodeStatusEnum byCode = ManuscriptReviewNodeStatusEnum.fromCode(trimToNull(record.getCurrentNodeStatus()));
        return byCode != null ? byCode : ManuscriptReviewNodeStatusEnum.fromAnyLabel(trimToNull(record.getCurrentNodeLabel()));
    }

    private String resolveDisplayCurrentNodeLabel(ManuscriptReviewRecordEntity record) {
        ManuscriptReviewNodeStatusEnum currentNodeStatus = resolveCurrentNodeStatus(record);
        return currentNodeStatus == null ? record.getCurrentNodeLabel() : currentNodeStatus.getLabel();
    }

    private int compareTimelineHistory(ManuscriptReviewHistoryEntity left, ManuscriptReviewHistoryEntity right) {
        int sortedCompare = Comparator
            .comparing(ManuscriptReviewHistoryEntity::getSorted, Comparator.nullsLast(Integer::compareTo))
            .compare(left, right);
        if (sortedCompare != 0) {
            return sortedCompare;
        }
        int createTimeCompare = Comparator
            .comparing(ManuscriptReviewHistoryEntity::getCreateTime, Comparator.nullsLast(Date::compareTo))
            .compare(left, right);
        if (createTimeCompare != 0) {
            return createTimeCompare;
        }
        int actionPriorityCompare = Integer.compare(resolveTimelineActionPriority(left), resolveTimelineActionPriority(right));
        if (actionPriorityCompare != 0) {
            return actionPriorityCompare;
        }
        return Comparator
            .comparing(ManuscriptReviewHistoryEntity::getId, Comparator.nullsLast(Long::compareTo))
            .compare(left, right);
    }

    private int resolveTimelineActionPriority(ManuscriptReviewHistoryEntity history) {
        String actionType = trimToNull(history == null ? null : history.getActionType());
        if ("CREATE".equals(actionType)) {
            return 10;
        }
        if ("SKIP_LEVEL_1".equals(actionType)) {
            return 20;
        }
        return 100;
    }

    private int safePageNum(Integer pageNum) {
        return pageNum == null || pageNum <= 0 ? 1 : pageNum;
    }

    private int safePageSize(Integer pageSize) {
        return pageSize == null || pageSize <= 0 ? Integer.MAX_VALUE : pageSize;
    }

    private boolean isEnabled(ManuscriptReviewAttachmentEntity attachment) {
        return ENABLED.equals(attachment.getEnabled());
    }

    private boolean isEnabled(ManuscriptReviewExternalLinkEntity externalLink) {
        return ENABLED.equals(externalLink.getEnabled());
    }

    private boolean isEnabled(ManuscriptReviewVideoMarkerEntity videoMarker) {
        return ENABLED.equals(videoMarker.getEnabled());
    }

    private LocalDateTime parseDateTime(String value) {
        String normalized = trimToNull(value);
        return normalized == null ? null : LocalDateTime.parse(normalized, TIME_FORMATTER);
    }

    private LocalDateTime toLocalDateTime(Date date) {
        return Instant.ofEpochMilli(date.getTime()).atZone(BUSINESS_ZONE_ID).toLocalDateTime();
    }

    private String formatDate(Date date) {
        if (date == null) {
            return null;
        }
        return TIME_FORMATTER.format(Instant.ofEpochMilli(date.getTime()).atZone(BUSINESS_ZONE_ID));
    }

    private String normalizeText(String text) {
        if (text == null) {
            return null;
        }
        return text.replaceAll("\\s+", " ").trim();
    }

    private String normalizeTenantId(String tenantId) {
        String normalized = trimToNull(tenantId);
        return normalized == null ? DEFAULT_TENANT_ID : normalized;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private Long requireCurrentUserId() {
        Long currentUserId = currentUserGateway.getCurrentUserId();
        if (currentUserId == null) {
            throw new ServiceException(CURRENT_USER_REQUIRED_MESSAGE);
        }
        return currentUserId;
    }

    @SafeVarargs
    private final <T> T firstNonNull(T... values) {
        for (T value : values) {
            if (value != null) {
                return value;
            }
        }
        return null;
    }
}
