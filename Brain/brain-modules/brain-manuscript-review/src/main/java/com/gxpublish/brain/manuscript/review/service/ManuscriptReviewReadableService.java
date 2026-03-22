package com.gxpublish.brain.manuscript.review.service;

import java.time.Instant;
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
    private static final String DEFAULT_TENANT_ID = "000000";
    private static final String LEVEL_ONE_NODE = "待一级审批";
    private static final String LEVEL_TWO_NODE = "待二级审批";
    private static final String LEVEL_THREE_NODE = "待三级审批";
    private static final ZoneId BUSINESS_ZONE_ID = ZoneId.of("Asia/Shanghai");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final List<ManuscriptReviewDetailAction> ACTION_ORDER = List.of(
        ManuscriptReviewDetailAction.MODIFY,
        ManuscriptReviewDetailAction.RESUBMIT,
        ManuscriptReviewDetailAction.GO_APPROVE,
        ManuscriptReviewDetailAction.BACK
    );

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

    public List<ManuscriptReviewLedgerItemResponse> listLedger(ManuscriptReviewLedgerQueryRequest request) {
        QueryWrapper<ManuscriptReviewRecordEntity> wrapper = new QueryWrapper<>();
        if (request != null) {
            String keyword = trimToNull(request.getKeyword());
            if (keyword != null) {
                wrapper.and(query -> query.like("manuscript_code", keyword)
                    .or().like("external_manuscript_code", keyword)
                    .or().like("title", keyword));
            }
            String processTypeLabel = trimToNull(request.getProcessTypeLabel());
            if (processTypeLabel != null) {
                wrapper.eq("process_type", resolveProcessTypeCode(processTypeLabel));
            }
            String flowStatusLabel = trimToNull(request.getFlowStatusLabel());
            if (flowStatusLabel != null) {
                wrapper.eq("flow_status_label", flowStatusLabel);
            }
            String currentNodeLabel = trimToNull(request.getCurrentNodeLabel());
            if (currentNodeLabel != null) {
                wrapper.eq("current_node_label", currentNodeLabel);
            }
        }
        wrapper.orderByDesc("update_time").orderByDesc("create_time").orderByDesc("id");
        return recordMapper.selectList(wrapper).stream()
            .map(this::toLedgerItem)
            .toList();
    }

    public ManuscriptReviewDetailResponse getDetail(Long reviewId) {
        ManuscriptReviewRecordEntity record = recordMapper.selectById(reviewId);
        if (record == null) {
            throw new ServiceException(REVIEW_NOT_FOUND_MESSAGE);
        }

        List<ManuscriptReviewAttachmentEntity> attachments = attachmentMapper.selectList(
            new QueryWrapper<ManuscriptReviewAttachmentEntity>()
                .eq("review_id", reviewId)
                .orderByAsc("create_time")
                .orderByAsc("id")
        );
        List<ManuscriptReviewExternalLinkEntity> externalLinks = externalLinkMapper.selectList(
            new QueryWrapper<ManuscriptReviewExternalLinkEntity>()
                .eq("review_id", reviewId)
                .orderByAsc("create_time")
                .orderByAsc("id")
        );
        List<ManuscriptReviewHistoryEntity> histories = historyMapper.selectList(
            new QueryWrapper<ManuscriptReviewHistoryEntity>()
                .eq("review_id", reviewId)
                .orderByAsc("create_time")
                .orderByAsc("id")
        );
        List<ManuscriptReviewVideoMarkerEntity> videoMarkers = videoMarkerMapper.selectList(
            new QueryWrapper<ManuscriptReviewVideoMarkerEntity>()
                .eq("review_id", reviewId)
                .orderByAsc("create_time")
                .orderByAsc("id")
        );

        ManuscriptReviewDetailResponse detail = new ManuscriptReviewDetailResponse();
        detail.setReviewId(record.getId());
        detail.setSummaryCard(new ManuscriptReviewDetailResponse.SummaryCard(
            record.getFlowStatusLabel(),
            record.getCurrentNodeLabel(),
            record.getInitiatorName(),
            formatDate(firstNonNull(record.getUpdateTime(), record.getCreateTime()))
        ));
        detail.setManuscriptCard(new ManuscriptReviewDetailResponse.ManuscriptCard(
            resolveProcessTypeLabel(record.getProcessType()),
            record.getManuscriptCode(),
            record.getExternalManuscriptCode(),
            record.getTitle(),
            record.getMediaChannelLabel(),
            record.getSubmitterDeptName(),
            record.getAuthorNames(),
            record.getNote(),
            record.getContent()
        ));
        detail.setActionBar(buildActionBar(record, histories));
        detail.setTimeline(buildTimeline(histories));
        detail.setResources(buildResources(attachments, externalLinks, videoMarkers));
        return detail;
    }

    private ManuscriptReviewLedgerItemResponse toLedgerItem(ManuscriptReviewRecordEntity entity) {
        ManuscriptReviewLedgerItemResponse item = new ManuscriptReviewLedgerItemResponse();
        item.setReviewId(entity.getId());
        item.setManuscriptCode(entity.getManuscriptCode());
        item.setTitle(entity.getTitle());
        item.setProcessTypeLabel(resolveProcessTypeLabel(entity.getProcessType()));
        item.setMediaChannelLabel(entity.getMediaChannelLabel());
        item.setFlowStatusLabel(entity.getFlowStatusLabel());
        item.setCurrentNodeLabel(entity.getCurrentNodeLabel());
        item.setInitiatorName(entity.getInitiatorName());
        item.setUpdateTime(formatDate(firstNonNull(entity.getUpdateTime(), entity.getCreateTime())));
        return item;
    }

    private ManuscriptReviewDetailResponse.ActionBar buildActionBar(ManuscriptReviewRecordEntity record,
                                                                    List<ManuscriptReviewHistoryEntity> histories) {
        Set<Long> historyParticipantUserIds = histories.stream()
            .map(ManuscriptReviewHistoryEntity::getActorUserId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
        ManuscriptReviewDetailPermissionResult permissionResult = permissionPolicy.resolve(
            ManuscriptReviewDetailPermissionContext.builder()
                .currentUserId(currentUserGateway.getCurrentUserId())
                .initiatorUserId(record.getInitiatorUserId())
                .currentApproverUserIds(resolveCurrentApproverUserIds(record))
                .historyParticipantUserIds(historyParticipantUserIds)
                .returnedToInitiator(isReturnedToInitiator(record))
                .build()
        );
        List<String> actions = ACTION_ORDER.stream()
            .filter(permissionResult.getAllowedActions()::contains)
            .map(this::toActionLabel)
            .toList();
        return new ManuscriptReviewDetailResponse.ActionBar(permissionResult.canModify(), actions);
    }

    private List<ManuscriptReviewDetailResponse.TimelineItem> buildTimeline(List<ManuscriptReviewHistoryEntity> histories) {
        return histories.stream()
            .sorted(Comparator.comparing(ManuscriptReviewHistoryEntity::getCreateTime, Comparator.nullsLast(Date::compareTo)))
            .map(history -> new ManuscriptReviewDetailResponse.TimelineItem(
                formatDate(history.getCreateTime()),
                normalizeText(history.getActionText())
            ))
            .toList();
    }

    private ManuscriptReviewDetailResponse.ResourceSection buildResources(List<ManuscriptReviewAttachmentEntity> attachments,
                                                                          List<ManuscriptReviewExternalLinkEntity> externalLinks,
                                                                          List<ManuscriptReviewVideoMarkerEntity> videoMarkers) {
        Map<Long, List<ManuscriptReviewVideoMarkerEntity>> currentMarkersByVideo = videoMarkers.stream()
            .filter(this::isEnabled)
            .collect(Collectors.groupingBy(ManuscriptReviewVideoMarkerEntity::getVideoAttachmentId));

        List<ManuscriptReviewDetailResponse.AttachmentItem> currentAttachments = attachments.stream()
            .filter(attachment -> !Boolean.TRUE.equals(attachment.getIsVideo()))
            .filter(this::isEnabled)
            .map(attachment -> new ManuscriptReviewDetailResponse.AttachmentItem(
                attachment.getFileName(),
                attachment.getFileUrl(),
                attachment.getFileSize(),
                attachment.getMimeType(),
                null,
                attachment.getRemark()
            ))
            .toList();

        List<ManuscriptReviewDetailResponse.VideoItem> currentVideos = attachments.stream()
            .filter(attachment -> Boolean.TRUE.equals(attachment.getIsVideo()))
            .filter(this::isEnabled)
            .map(video -> new ManuscriptReviewDetailResponse.VideoItem(
                video.getFileName(),
                video.getFileUrl(),
                video.getFileSize(),
                video.getMimeType(),
                formatDuration(video.getVideoDurationSeconds()),
                currentMarkersByVideo.getOrDefault(video.getId(), Collections.emptyList()).stream()
                    .sorted(Comparator.comparing(ManuscriptReviewVideoMarkerEntity::getCreateTime, Comparator.nullsLast(Date::compareTo)))
                    .map(marker -> new ManuscriptReviewDetailResponse.VideoMarkerItem(
                        marker.getStartTime(),
                        marker.getEndTime(),
                        marker.getMarkerNote(),
                        formatDate(marker.getCreateTime()),
                        null
                    ))
                    .toList()
            ))
            .toList();

        List<ManuscriptReviewDetailResponse.AttachmentItem> historyAttachments = attachments.stream()
            .filter(attachment -> !Boolean.TRUE.equals(attachment.getIsVideo()))
            .filter(attachment -> !isEnabled(attachment))
            .map(attachment -> new ManuscriptReviewDetailResponse.AttachmentItem(
                attachment.getFileName(),
                attachment.getFileUrl(),
                attachment.getFileSize(),
                attachment.getMimeType(),
                formatDate(attachment.getDisabledTime()),
                attachment.getRemark()
            ))
            .toList();

        List<ManuscriptReviewDetailResponse.ExternalLinkItem> currentExternalLinks = externalLinks.stream()
            .filter(this::isEnabled)
            .map(link -> new ManuscriptReviewDetailResponse.ExternalLinkItem(
                link.getLinkTitle(),
                link.getLinkUrl(),
                null,
                link.getRemark()
            ))
            .toList();

        List<ManuscriptReviewDetailResponse.ExternalLinkItem> historyExternalLinks = externalLinks.stream()
            .filter(link -> !isEnabled(link))
            .map(link -> new ManuscriptReviewDetailResponse.ExternalLinkItem(
                link.getLinkTitle(),
                link.getLinkUrl(),
                formatDate(link.getDisabledTime()),
                link.getRemark()
            ))
            .toList();

        List<ManuscriptReviewDetailResponse.VideoMarkerItem> historyVideoMarkers = videoMarkers.stream()
            .filter(marker -> !isEnabled(marker))
            .map(marker -> new ManuscriptReviewDetailResponse.VideoMarkerItem(
                marker.getStartTime(),
                marker.getEndTime(),
                marker.getMarkerNote(),
                formatDate(marker.getCreateTime()),
                formatDate(marker.getDisabledTime())
            ))
            .toList();

        return new ManuscriptReviewDetailResponse.ResourceSection(
            currentAttachments,
            currentExternalLinks,
            currentVideos,
            historyAttachments,
            historyExternalLinks,
            historyVideoMarkers
        );
    }

    private boolean isReturnedToInitiator(ManuscriptReviewRecordEntity record) {
        return "已退回".equals(record.getFlowStatusLabel()) || "待发起人处理".equals(record.getCurrentNodeLabel());
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
                .eq("del_flag", ACTIVE)
        ).stream()
            .map(ManuscriptReviewSystemRoleEntity::getRoleId)
            .filter(Objects::nonNull)
            .distinct()
            .toList();
        if (roleIds.isEmpty()) {
            return Collections.emptySet();
        }

        List<Long> userIds = userRoleMapper.selectList(
            new QueryWrapper<ManuscriptReviewSystemUserRoleEntity>()
                .in("role_id", roleIds)
        ).stream()
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
                .eq("del_flag", ACTIVE)
        ).stream()
            .map(ManuscriptReviewSystemUserEntity::getUserId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
    }

    private String resolveCurrentNodeRoleKey(ManuscriptReviewRecordEntity record) {
        String currentNodeLabel = trimToNull(record.getCurrentNodeLabel());
        if (currentNodeLabel == null) {
            return null;
        }
        ManuscriptReviewFlowConfigEntity flowConfig = flowConfigMapper.selectOne(
            new QueryWrapper<ManuscriptReviewFlowConfigEntity>()
                .eq("tenant_id", normalizeTenantId(record.getTenantId()))
                .eq("process_type", record.getProcessType())
                .eq("status", ACTIVE)
                .orderByDesc("id")
                .last("limit 1")
        );
        if (flowConfig == null) {
            return null;
        }
        return switch (currentNodeLabel) {
            case LEVEL_ONE_NODE -> trimToNull(flowConfig.getLevelOneRoleKey());
            case LEVEL_TWO_NODE -> trimToNull(flowConfig.getLevelTwoRoleKey());
            case LEVEL_THREE_NODE -> trimToNull(flowConfig.getLevelThreeRoleKey());
            default -> null;
        };
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

    private String resolveProcessTypeCode(String processTypeLabel) {
        for (ManuscriptReviewProcessType value : ManuscriptReviewProcessType.values()) {
            if (value.getDisplayName().equals(processTypeLabel)) {
                return value.name();
            }
        }
        return processTypeLabel;
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

    private String toActionLabel(ManuscriptReviewDetailAction action) {
        Map<ManuscriptReviewDetailAction, String> labels = new EnumMap<>(ManuscriptReviewDetailAction.class);
        labels.put(ManuscriptReviewDetailAction.MODIFY, "修改");
        labels.put(ManuscriptReviewDetailAction.RESUBMIT, "再次提交");
        labels.put(ManuscriptReviewDetailAction.GO_APPROVE, "去审批");
        labels.put(ManuscriptReviewDetailAction.BACK, "返回");
        return labels.get(action);
    }

    private String formatDate(Date date) {
        if (date == null) {
            return null;
        }
        Instant instant = Instant.ofEpochMilli(date.getTime());
        return TIME_FORMATTER.format(instant.atZone(BUSINESS_ZONE_ID));
    }

    private String formatDuration(Integer seconds) {
        if (seconds == null || seconds < 0) {
            return null;
        }
        int hour = seconds / 3600;
        int minute = (seconds % 3600) / 60;
        int second = seconds % 60;
        return String.format("%02d:%02d:%02d", hour, minute, second);
    }

    private String normalizeText(String text) {
        if (text == null) {
            return null;
        }
        return text.replaceAll("\\s+", " ").trim();
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String normalizeTenantId(String tenantId) {
        String normalized = trimToNull(tenantId);
        return normalized == null ? DEFAULT_TENANT_ID : normalized;
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
