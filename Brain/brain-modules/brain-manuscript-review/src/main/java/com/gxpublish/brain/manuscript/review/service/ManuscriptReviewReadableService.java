package com.gxpublish.brain.manuscript.review.service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import cn.hutool.crypto.SecureUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.gxpublish.brain.common.core.exception.ServiceException;
import com.gxpublish.brain.common.mybatis.core.page.PageQuery;
import com.gxpublish.brain.common.mybatis.core.page.TableDataInfo;
import com.gxpublish.brain.common.oss.factory.OssFactory;
import com.gxpublish.brain.manuscript.review.controller.request.ManuscriptReviewLedgerQueryRequest;
import com.gxpublish.brain.manuscript.review.controller.response.ManuscriptReviewDetailResponse;
import com.gxpublish.brain.manuscript.review.controller.response.ManuscriptReviewLedgerItemResponse;
import com.gxpublish.brain.manuscript.review.controller.response.ManuscriptReviewPreviewTicketResponse;
import com.gxpublish.brain.manuscript.review.domain.entity.ManuscriptReviewAttachmentEntity;
import com.gxpublish.brain.manuscript.review.domain.entity.ManuscriptReviewExternalLinkEntity;
import com.gxpublish.brain.manuscript.review.domain.entity.ManuscriptReviewFlowConfigEntity;
import com.gxpublish.brain.manuscript.review.domain.entity.ManuscriptReviewHistoryEntity;
import com.gxpublish.brain.manuscript.review.domain.entity.ManuscriptReviewRecordEntity;
import com.gxpublish.brain.manuscript.review.domain.entity.ManuscriptReviewSystemRoleEntity;
import com.gxpublish.brain.manuscript.review.domain.entity.ManuscriptReviewSystemUserEntity;
import com.gxpublish.brain.manuscript.review.domain.entity.ManuscriptReviewSystemUserRoleEntity;
import com.gxpublish.brain.manuscript.review.domain.entity.ManuscriptReviewSysOssEntity;
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
import com.gxpublish.brain.manuscript.review.mapper.ManuscriptReviewSysOssMapper;
import com.gxpublish.brain.manuscript.review.mapper.ManuscriptReviewVideoMarkerMapper;

@Service
public class ManuscriptReviewReadableService {

    private static final String ENABLED = "1";
    private static final String ACTIVE = "0";
    private static final String REVIEW_NOT_FOUND_MESSAGE = "稿件审校流程不存在";
    private static final String CURRENT_USER_REQUIRED_MESSAGE = "当前登录用户不存在";
    private static final String VIEW_PERMISSION_DENIED_MESSAGE = "当前用户无权查看该流程";
    private static final String RESOURCE_NOT_FOUND_MESSAGE = "资源不存在";
    private static final String PREVIEW_RESOURCE_INCOMPLETE_MESSAGE = "资源预览信息缺失";
    private static final String PREVIEW_TOKEN_REQUIRED_MESSAGE = "预览票据不存在或已失效";
    private static final String PREVIEW_TOKEN_INVALID_MESSAGE = "预览票据无效";
    private static final String PREVIEW_TOKEN_EXPIRED_MESSAGE = "预览票据已过期，请刷新详情后重试";
    private static final String CURRENT_CLIENT_REQUIRED_MESSAGE = "当前客户端不存在";
    private static final String CURRENT_USER_TYPE_REQUIRED_MESSAGE = "当前登录用户类型不存在";
    private static final String PREVIEW_TOKEN_SCOPE = "MANUSCRIPT_REVIEW_PREVIEW";
    private static final long PREVIEW_TOKEN_TTL_SECONDS = 600L;
    private static final String DEFAULT_TENANT_ID = "000000";
    private static final String ROLE_KEY_CERTIFIED_INITIATOR = "manuscript_review_certified_initiator";
    private static final String ROLE_KEY_INITIATOR = "manuscript_review_initiator";
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
    private final ManuscriptReviewSysOssMapper sysOssMapper;
    private final ManuscriptReviewDetailPermissionPolicy permissionPolicy = new ManuscriptReviewDetailPermissionPolicy();
    @Value("${sa-token.jwt-secret-key:abcdefghijklmnopqrstuvwxyz}")
    private String previewTokenSecret;

    @Autowired
    public ManuscriptReviewReadableService(ManuscriptReviewRecordMapper recordMapper,
                                           ManuscriptReviewAttachmentMapper attachmentMapper,
                                           ManuscriptReviewExternalLinkMapper externalLinkMapper,
                                           ManuscriptReviewHistoryMapper historyMapper,
                                           ManuscriptReviewVideoMarkerMapper videoMarkerMapper,
                                           ManuscriptReviewFlowConfigMapper flowConfigMapper,
                                           ManuscriptReviewSystemRoleMapper roleMapper,
                                           ManuscriptReviewSystemUserRoleMapper userRoleMapper,
                                           ManuscriptReviewSystemUserMapper userMapper,
                                           ManuscriptReviewCurrentUserGateway currentUserGateway,
                                           ManuscriptReviewSysOssMapper sysOssMapper) {
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
        this.sysOssMapper = sysOssMapper;
    }

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
        this(recordMapper, attachmentMapper, externalLinkMapper, historyMapper, videoMarkerMapper, flowConfigMapper,
            roleMapper, userRoleMapper, userMapper, currentUserGateway, null);
    }

    public TableDataInfo<ManuscriptReviewLedgerItemResponse> listLedger(ManuscriptReviewLedgerQueryRequest request) {
        ManuscriptReviewLedgerQueryRequest safeRequest = request == null ? new ManuscriptReviewLedgerQueryRequest() : request;
        LedgerVisibilityScope scope = resolveLedgerVisibilityScope();
        List<Long> visibleReviewIds = resolveVisibleReviewIds(scope);
        if (visibleReviewIds.isEmpty()) {
            return new TableDataInfo<>(List.of(), 0);
        }
        Page<ManuscriptReviewRecordEntity> page = new PageQuery(safePageSize(safeRequest.getPageSize()), safePageNum(safeRequest.getPageNum())).build();
        Page<ManuscriptReviewRecordEntity> result = recordMapper.customSelectVisibleLedgerPage(
            page,
            safeRequest,
            visibleReviewIds,
            toDate(parseDateTime(safeRequest.getStartTimeFrom())),
            toDate(parseDateTime(safeRequest.getStartTimeTo()))
        );
        if (result == null || result.getRecords() == null) {
            return new TableDataInfo<>(List.of(), 0);
        }
        return new TableDataInfo<>(result.getRecords().stream().map(this::toLedgerItem).toList(), result.getTotal());
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
        DetailAccessScope accessScope = resolveDetailAccessScope(record);
        if (!accessScope.canView()) {
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
                .filter(marker -> currentVideos.stream().anyMatch(video -> Objects.equals(video.getId(), marker.getVideoAttachmentId())))
                .sorted(Comparator.comparing(ManuscriptReviewVideoMarkerEntity::getVideoAttachmentId, Comparator.nullsLast(Long::compareTo))
                    .thenComparing(ManuscriptReviewVideoMarkerEntity::getStartSeconds, Comparator.nullsLast(Integer::compareTo))
                    .thenComparing(ManuscriptReviewVideoMarkerEntity::getCreateTime, Comparator.nullsLast(Date::compareTo)))
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
        response.setTimelineItems(buildTimelineItems(histories, attachments, externalLinks, videoMarkers));
        response.setPermissionMatrix(buildPermissionMatrix(record, accessScope));
        return response;
    }

    public ManuscriptReviewPreviewTicketResponse issuePreviewTicket(Long resourceId) {
        ManuscriptReviewAttachmentEntity attachment = requirePreviewAttachment(resourceId);
        ManuscriptReviewRecordEntity record = requirePreviewRecord(attachment);
        DetailAccessScope accessScope = resolveDetailAccessScope(record);
        if (!accessScope.canView()) {
            throw new ServiceException(VIEW_PERMISSION_DENIED_MESSAGE);
        }
        String clientId = requireCurrentClientId();
        PreviewTokenPayload payload = new PreviewTokenPayload(
            PREVIEW_TOKEN_SCOPE,
            attachment.getId(),
            requireCurrentUserId(),
            requireCurrentUserType(),
            clientId,
            Instant.now().getEpochSecond() + PREVIEW_TOKEN_TTL_SECONDS
        );
        String previewToken = createPreviewToken(payload);
        return new ManuscriptReviewPreviewTicketResponse(
            attachment.getId(),
            buildTokenizedPreviewResourceUrl(attachment.getId(), previewToken, clientId),
            previewToken,
            payload.expireAtEpochSecond()
        );
    }

    public void previewResource(Long resourceId, HttpServletRequest request, HttpServletResponse response) throws IOException {
        ManuscriptReviewAttachmentEntity attachment = requirePreviewAttachment(resourceId);
        ManuscriptReviewRecordEntity record = requirePreviewRecord(attachment);
        if (currentUserGateway.getCurrentUserId() != null) {
            validateLoggedInPreviewAccess(record, request);
        } else {
            validatePreviewTokenAccess(resourceId, request);
        }
        ManuscriptReviewSysOssEntity sysOss = sysOssMapper == null ? null : sysOssMapper.selectById(attachment.getOssId());
        String upstreamUrl = buildPreviewUpstreamUrl(sysOss, attachment);
        HttpURLConnection connection = openPreviewConnection(upstreamUrl, request.getHeader("Range"));
        try {
            response.setStatus(connection.getResponseCode());
            applyPreviewResponseHeaders(response, attachment, connection);
            try (InputStream inputStream = resolvePreviewInputStream(connection)) {
                inputStream.transferTo(response.getOutputStream());
            }
        } finally {
            connection.disconnect();
        }
    }

    private ManuscriptReviewAttachmentEntity requirePreviewAttachment(Long resourceId) {
        ManuscriptReviewAttachmentEntity attachment = attachmentMapper.selectById(resourceId);
        if (attachment == null) {
            throw new ServiceException(RESOURCE_NOT_FOUND_MESSAGE);
        }
        return attachment;
    }

    private ManuscriptReviewRecordEntity requirePreviewRecord(ManuscriptReviewAttachmentEntity attachment) {
        ManuscriptReviewRecordEntity record = recordMapper.selectById(attachment.getReviewId());
        if (record == null) {
            throw new ServiceException(REVIEW_NOT_FOUND_MESSAGE);
        }
        return record;
    }

    private void validateLoggedInPreviewAccess(ManuscriptReviewRecordEntity record, HttpServletRequest request) {
        if (!Objects.equals(requireCurrentClientId(), requireRequestClientId(request))) {
            throw new ServiceException(PREVIEW_TOKEN_INVALID_MESSAGE);
        }
        DetailAccessScope accessScope = resolveDetailAccessScope(record);
        if (!accessScope.canView()) {
            throw new ServiceException(VIEW_PERMISSION_DENIED_MESSAGE);
        }
    }

    private void validatePreviewTokenAccess(Long resourceId, HttpServletRequest request) {
        String previewToken = trimToNull(request.getParameter("previewToken"));
        if (previewToken == null) {
            throw new ServiceException(PREVIEW_TOKEN_REQUIRED_MESSAGE);
        }
        PreviewTokenPayload payload = parsePreviewToken(previewToken);
        if (!Objects.equals(PREVIEW_TOKEN_SCOPE, payload.scope())
            || !Objects.equals(resourceId, payload.resourceId())
            || !Objects.equals(requireRequestClientId(request), trimToNull(payload.clientId()))) {
            throw new ServiceException(PREVIEW_TOKEN_INVALID_MESSAGE);
        }
        if (payload.expireAtEpochSecond() == null || payload.expireAtEpochSecond() < Instant.now().getEpochSecond()) {
            throw new ServiceException(PREVIEW_TOKEN_EXPIRED_MESSAGE);
        }
    }

    private String createPreviewToken(PreviewTokenPayload payload) {
        String payloadText = serializePreviewTokenPayload(payload);
        String encodedPayload = java.util.Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString(payloadText.getBytes(StandardCharsets.UTF_8));
        return encodedPayload + "." + buildPreviewTokenSignature(payloadText);
    }

    private PreviewTokenPayload parsePreviewToken(String previewToken) {
        String[] segments = previewToken.split("\\.", 2);
        if (segments.length != 2) {
            throw new ServiceException(PREVIEW_TOKEN_INVALID_MESSAGE);
        }
        try {
            String payloadText = new String(java.util.Base64.getUrlDecoder().decode(segments[0]), StandardCharsets.UTF_8);
            if (!Objects.equals(buildPreviewTokenSignature(payloadText), segments[1])) {
                throw new ServiceException(PREVIEW_TOKEN_INVALID_MESSAGE);
            }
            return deserializePreviewTokenPayload(payloadText);
        } catch (IllegalArgumentException exception) {
            throw new ServiceException(PREVIEW_TOKEN_INVALID_MESSAGE);
        }
    }

    private String serializePreviewTokenPayload(PreviewTokenPayload payload) {
        return String.join("|",
            firstNonNull(payload.scope(), ""),
            payload.resourceId() == null ? "" : String.valueOf(payload.resourceId()),
            payload.userId() == null ? "" : String.valueOf(payload.userId()),
            firstNonNull(payload.userType(), ""),
            firstNonNull(payload.clientId(), ""),
            payload.expireAtEpochSecond() == null ? "" : String.valueOf(payload.expireAtEpochSecond()));
    }

    private PreviewTokenPayload deserializePreviewTokenPayload(String payloadText) {
        String[] parts = payloadText.split("\\|", -1);
        if (parts.length != 6) {
            throw new ServiceException(PREVIEW_TOKEN_INVALID_MESSAGE);
        }
        return new PreviewTokenPayload(
            trimToNull(parts[0]),
            parseLongOrNull(parts[1]),
            parseLongOrNull(parts[2]),
            trimToNull(parts[3]),
            trimToNull(parts[4]),
            parseLongOrNull(parts[5])
        );
    }

    private String buildPreviewTokenSignature(String payloadText) {
        return SecureUtil.sha256(PREVIEW_TOKEN_SCOPE + ":" + payloadText + ":" + firstNonNull(previewTokenSecret, ""));
    }

    private String buildTokenizedPreviewResourceUrl(Long resourceId, String previewToken, String clientId) {
        return buildPreviewResourceUrl(resourceId)
            + "?previewToken=" + URLEncoder.encode(previewToken, StandardCharsets.UTF_8)
            + "&clientid=" + URLEncoder.encode(clientId, StandardCharsets.UTF_8);
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

    private List<ManuscriptReviewDetailResponse.TimelineItemVO> buildTimelineItems(List<ManuscriptReviewHistoryEntity> histories,
                                                                                   List<ManuscriptReviewAttachmentEntity> attachments,
                                                                                   List<ManuscriptReviewExternalLinkEntity> externalLinks,
                                                                                   List<ManuscriptReviewVideoMarkerEntity> videoMarkers) {
        return histories.stream()
            .sorted(this::compareTimelineHistory)
            .map(history -> toTimelineItem(history, attachments, externalLinks, videoMarkers))
            .toList();
    }

    private ManuscriptReviewDetailResponse.TimelineItemVO toTimelineItem(ManuscriptReviewHistoryEntity history,
                                                                         List<ManuscriptReviewAttachmentEntity> attachments,
                                                                         List<ManuscriptReviewExternalLinkEntity> externalLinks,
                                                                         List<ManuscriptReviewVideoMarkerEntity> videoMarkers) {
        TimelineResourceReference reference = resolveTimelineResourceReference(history, attachments, externalLinks, videoMarkers);
        return new ManuscriptReviewDetailResponse.TimelineItemVO(
            formatDate(history.getCreateTime()),
            "WORKFLOW",
            "流程",
            trimToNull(history.getActionType()),
            normalizeText(history.getActionText()),
            trimToNull(history.getActorName()),
            null,
            reference == null ? null : reference.displayName(),
            reference == null ? null : reference.resourceId(),
            reference == null ? null : reference.ossId(),
            reference == null ? null : reference.resourceType(),
            reference == null ? null : reference.resourceUrl(),
            reference == null ? null : reference.externalUrl(),
            null,
            null);
    }

    private TimelineResourceReference resolveTimelineResourceReference(ManuscriptReviewHistoryEntity history,
                                                                      List<ManuscriptReviewAttachmentEntity> attachments,
                                                                      List<ManuscriptReviewExternalLinkEntity> externalLinks,
                                                                      List<ManuscriptReviewVideoMarkerEntity> videoMarkers) {
        String actionType = trimToNull(history.getActionType());
        if (!Objects.equals(actionType, "RESOURCE_DISABLE") && !Objects.equals(actionType, "VIDEO_MARK_DISABLE")) {
            return null;
        }
        String actionText = trimToNull(history.getActionText());
        if (actionText == null) {
            return null;
        }

        String resourceName = extractFirstQuotedResourceName(actionText);
        if (resourceName == null) {
            return null;
        }

        List<TimelineResourceReference> candidates = new ArrayList<>();
        if (actionText.contains("附件")) {
            attachments.stream()
                .filter(attachment -> !Boolean.TRUE.equals(attachment.getIsVideo()))
                .filter(attachment -> Objects.equals(trimToNull(attachment.getFileName()), resourceName))
                .map(attachment -> new TimelineResourceReference(
                    attachment.getId(),
                    attachment.getOssId(),
                    "ATTACHMENT",
                    attachment.getFileName(),
                    attachment.getFileUrl(),
                    null,
                    firstNonNull(attachment.getDisabledTime(), attachment.getCreateTime())))
                .forEach(candidates::add);
        }
        if (actionText.contains("视频")) {
            attachments.stream()
                .filter(attachment -> Boolean.TRUE.equals(attachment.getIsVideo()))
                .filter(attachment -> Objects.equals(trimToNull(attachment.getFileName()), resourceName))
                .map(attachment -> new TimelineResourceReference(
                    attachment.getId(),
                    attachment.getOssId(),
                    "VIDEO",
                    attachment.getFileName(),
                    attachment.getFileUrl(),
                    null,
                    firstNonNull(attachment.getDisabledTime(), attachment.getCreateTime())))
                .forEach(candidates::add);
        }
        if (actionText.contains("外链")) {
            externalLinks.stream()
                .filter(externalLink -> Objects.equals(trimToNull(externalLink.getLinkTitle()), resourceName))
                .map(externalLink -> new TimelineResourceReference(
                    externalLink.getId(),
                    null,
                    "EXTERNAL_LINK",
                    externalLink.getLinkTitle(),
                    null,
                    externalLink.getLinkUrl(),
                    firstNonNull(externalLink.getDisabledTime(), externalLink.getCreateTime())))
                .forEach(candidates::add);
        }
        if (Objects.equals(actionType, "VIDEO_MARK_DISABLE")) {
            videoMarkers.stream()
                .filter(videoMarker -> Objects.equals(trimToNull(videoMarker.getMarkerNote()), resourceName))
                .map(videoMarker -> new TimelineResourceReference(
                    videoMarker.getId(),
                    null,
                    "VIDEO_MARK",
                    videoMarker.getMarkerNote(),
                    null,
                    null,
                    firstNonNull(videoMarker.getDisabledTime(), videoMarker.getCreateTime())))
                .forEach(candidates::add);
        }
        if (candidates.isEmpty()) {
            return null;
        }

        Date historyTime = history.getCreateTime();
        return candidates.stream()
            .sorted((left, right) -> compareTimelineReference(left, right, historyTime))
            .findFirst()
            .orElse(null);
    }

    private int compareTimelineReference(TimelineResourceReference left, TimelineResourceReference right, Date historyTime) {
        return Comparator
            .comparing((TimelineResourceReference reference) -> referenceTimeDistance(reference.matchTime(), historyTime))
            .thenComparing(TimelineResourceReference::matchTime, Comparator.nullsLast(Date::compareTo))
            .thenComparing(TimelineResourceReference::resourceId, Comparator.nullsLast(Long::compareTo))
            .compare(left, right);
    }

    private long referenceTimeDistance(Date referenceTime, Date historyTime) {
        if (referenceTime == null || historyTime == null) {
            return Long.MAX_VALUE;
        }
        return Math.abs(referenceTime.getTime() - historyTime.getTime());
    }

    private String extractFirstQuotedResourceName(String actionText) {
        int startIndex = actionText.indexOf('《');
        int endIndex = actionText.indexOf('》', startIndex + 1);
        if (startIndex < 0 || endIndex <= startIndex + 1) {
            return null;
        }
        return trimToNull(actionText.substring(startIndex + 1, endIndex));
    }

    private ManuscriptReviewDetailResponse.PermissionMatrixVO buildPermissionMatrix(ManuscriptReviewRecordEntity record,
                                                                                    DetailAccessScope accessScope) {
        ManuscriptReviewDetailPermissionResult permissionResult = permissionPolicy.resolve(
            ManuscriptReviewDetailPermissionContext.builder()
                .currentUserId(accessScope.currentUserId())
                .initiatorUserId(record.getInitiatorUserId())
                .currentApproverUserIds(accessScope.waitingApprover() ? Set.of(accessScope.currentUserId()) : Set.of())
                .historyParticipantUserIds(Set.of())
                .returnedToInitiator(isReturnedToInitiator(record))
                .build()
        );
        boolean isInitiator = accessScope.initiator();
        boolean isCurrentApprover = accessScope.waitingApprover();
        boolean isFinishedApprover = accessScope.finishedApprover();
        boolean canView = accessScope.canView();
        String businessStatus = mapBusinessStatusCode(record.getFlowStatusLabel());
        boolean canGotoApproval = permissionResult.getAllowedActions().contains(ManuscriptReviewDetailAction.GO_APPROVE);
        boolean canResubmit = permissionResult.getAllowedActions().contains(ManuscriptReviewDetailAction.RESUBMIT);
        boolean canCancel = isInitiator && !isCurrentApprover && ("WAITING".equals(businessStatus) || "BACK".equals(businessStatus));
        String buttonReason = resolveButtonReason(businessStatus, canView, isFinishedApprover);
        return new ManuscriptReviewDetailResponse.PermissionMatrixVO(
            isInitiator,
            isCurrentApprover,
            false,
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
            attachment.getOssId(),
            "ATTACHMENT",
            "附件",
            attachment.getFileName(),
            null,
            formatDate(attachment.getCreateTime()),
            buildPreviewResourceUrl(attachment.getId()));
    }

    private ManuscriptReviewDetailResponse.ResourceItemVO toVideoItem(ManuscriptReviewAttachmentEntity video) {
        return new ManuscriptReviewDetailResponse.ResourceItemVO(
            video.getId(),
            video.getOssId(),
            "VIDEO",
            "视频",
            video.getFileName(),
            null,
            formatDate(video.getCreateTime()),
            buildPreviewResourceUrl(video.getId()));
    }

    private ManuscriptReviewDetailResponse.ResourceItemVO toExternalLinkItem(ManuscriptReviewExternalLinkEntity externalLink) {
        return new ManuscriptReviewDetailResponse.ResourceItemVO(
            externalLink.getId(),
            null,
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
            marker.getVideoAttachmentId(),
            marker.getStartTime(),
            marker.getEndTime(),
            marker.getMarkerNote());
    }

    private DetailAccessScope resolveDetailAccessScope(ManuscriptReviewRecordEntity record) {
        Long currentUserId = requireCurrentUserId();
        boolean initiator = Objects.equals(currentUserId, record.getInitiatorUserId());
        List<Long> waitingReviewIds = parseWorkflowBusinessIds(recordMapper.selectWaitingBusinessIds(currentUserId));
        List<Long> finishedReviewIds = parseWorkflowBusinessIds(recordMapper.selectFinishedBusinessIds(currentUserId));
        boolean waitingApprover = waitingReviewIds.contains(record.getId());
        boolean finishedApprover = finishedReviewIds.contains(record.getId());
        return new DetailAccessScope(currentUserId, initiator, waitingApprover, finishedApprover);
    }

    private boolean isReturnedToInitiator(ManuscriptReviewRecordEntity record) {
        return "已退回".equals(record.getFlowStatusLabel())
            || resolveCurrentNodeStatus(record) == ManuscriptReviewNodeStatusEnum.RETURN_TO_INITIATOR;
    }

    private LedgerVisibilityScope resolveLedgerVisibilityScope() {
        Long currentUserId = requireCurrentUserId();
        String tenantId = normalizeTenantId(currentUserGateway.getCurrentTenantId());
        List<Long> currentRoleIds = firstNonNull(userRoleMapper.selectList(
            new QueryWrapper<ManuscriptReviewSystemUserRoleEntity>().eq("user_id", currentUserId)), List.<ManuscriptReviewSystemUserRoleEntity>of())
            .stream()
            .map(ManuscriptReviewSystemUserRoleEntity::getRoleId)
            .filter(Objects::nonNull)
            .distinct()
            .toList();
        if (currentRoleIds.isEmpty()) {
            return new LedgerVisibilityScope(currentUserId, false);
        }
        Set<String> currentRoleKeys = firstNonNull(roleMapper.selectList(
            new QueryWrapper<ManuscriptReviewSystemRoleEntity>()
                .in("role_id", currentRoleIds)
                .eq("tenant_id", tenantId)
                .eq("status", ACTIVE)
                .eq("del_flag", ACTIVE)), List.<ManuscriptReviewSystemRoleEntity>of())
            .stream()
            .map(ManuscriptReviewSystemRoleEntity::getRoleKey)
            .map(this::trimToNull)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
        boolean allowInitiator = currentRoleKeys.contains(ROLE_KEY_INITIATOR) || currentRoleKeys.contains(ROLE_KEY_CERTIFIED_INITIATOR);
        return new LedgerVisibilityScope(currentUserId, allowInitiator);
    }

    private List<Long> resolveVisibleReviewIds(LedgerVisibilityScope scope) {
        LinkedHashSet<Long> visibleReviewIds = new LinkedHashSet<>();
        if (scope.allowInitiator()) {
            visibleReviewIds.addAll(firstNonNull(recordMapper.selectInitiatedReviewIds(scope.currentUserId()), List.<Long>of()));
        }
        visibleReviewIds.addAll(parseWorkflowBusinessIds(recordMapper.selectWaitingBusinessIds(scope.currentUserId())));
        visibleReviewIds.addAll(parseWorkflowBusinessIds(recordMapper.selectFinishedBusinessIds(scope.currentUserId())));
        return List.copyOf(visibleReviewIds);
    }

    private List<Long> parseWorkflowBusinessIds(List<String> rawBusinessIds) {
        if (rawBusinessIds == null || rawBusinessIds.isEmpty()) {
            return List.of();
        }
        LinkedHashSet<Long> reviewIds = new LinkedHashSet<>();
        for (String rawBusinessId : rawBusinessIds) {
            Long reviewId = parseReviewId(rawBusinessId);
            if (reviewId != null) {
                reviewIds.add(reviewId);
            }
        }
        return List.copyOf(reviewIds);
    }

    private Long parseReviewId(String rawBusinessId) {
        String normalized = trimToNull(rawBusinessId);
        if (normalized == null) {
            return null;
        }
        try {
            return Long.valueOf(normalized);
        } catch (NumberFormatException ignored) {
            return null;
        }
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

    private String resolveButtonReason(String businessStatus, boolean canView, boolean isFinishedApprover) {
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
        if (isFinishedApprover) {
            return "当前记录来自我的已办，仅可查看";
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

    private Date toDate(LocalDateTime value) {
        return value == null ? null : Date.from(value.atZone(BUSINESS_ZONE_ID).toInstant());
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

    private String buildPreviewResourceUrl(Long resourceId) {
        return resourceId == null ? null : "/workflow/manuscript-review/resource/preview/" + resourceId;
    }

    private String buildPreviewUpstreamUrl(ManuscriptReviewSysOssEntity sysOss, ManuscriptReviewAttachmentEntity attachment) {
        if (sysOss == null) {
            String fallbackUrl = trimToNull(attachment.getFileUrl());
            if (fallbackUrl != null) {
                return fallbackUrl;
            }
            throw new ServiceException(PREVIEW_RESOURCE_INCOMPLETE_MESSAGE);
        }
        String service = trimToNull(sysOss.getService());
        String fileName = trimToNull(sysOss.getFileName());
        if (service != null && fileName != null) {
            return OssFactory.instance(service).createPresignedGetUrl(fileName, java.time.Duration.ofMinutes(5));
        }
        String directUrl = trimToNull(sysOss.getUrl());
        if (directUrl != null) {
            return directUrl;
        }
        String fallbackUrl = trimToNull(attachment.getFileUrl());
        if (fallbackUrl != null) {
            return fallbackUrl;
        }
        throw new ServiceException(PREVIEW_RESOURCE_INCOMPLETE_MESSAGE);
    }

    private HttpURLConnection openPreviewConnection(String upstreamUrl, String rangeHeader) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(upstreamUrl).openConnection();
        connection.setRequestMethod("GET");
        connection.setDoInput(true);
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(60000);
        if (rangeHeader != null && !rangeHeader.isBlank()) {
            connection.setRequestProperty("Range", rangeHeader.trim());
        }
        return connection;
    }

    private void applyPreviewResponseHeaders(HttpServletResponse response,
                                             ManuscriptReviewAttachmentEntity attachment,
                                             HttpURLConnection connection) {
        String contentType = firstNonNull(trimToNull(connection.getContentType()), trimToNull(attachment.getMimeType()),
            MediaType.APPLICATION_OCTET_STREAM_VALUE);
        response.setContentType(contentType);
        long contentLength = connection.getContentLengthLong();
        if (contentLength >= 0) {
            response.setContentLengthLong(contentLength);
        }
        copyPreviewHeader(connection, response, "Accept-Ranges");
        copyPreviewHeader(connection, response, "Content-Range");
        copyPreviewHeader(connection, response, "Cache-Control");
        copyPreviewHeader(connection, response, "ETag");
        copyPreviewHeader(connection, response, "Last-Modified");
        response.setHeader("Content-Disposition", buildInlineContentDisposition(attachment.getFileName()));
    }

    private void copyPreviewHeader(HttpURLConnection connection, HttpServletResponse response, String headerName) {
        String value = trimToNull(connection.getHeaderField(headerName));
        if (value != null) {
            response.setHeader(headerName, value);
        }
    }

    private String buildInlineContentDisposition(String fileName) {
        String normalizedFileName = trimToNull(fileName);
        if (normalizedFileName == null) {
            return "inline";
        }
        String encodedFileName = URLEncoder.encode(normalizedFileName, StandardCharsets.UTF_8).replace("+", "%20");
        return "inline; filename*=UTF-8''" + encodedFileName;
    }

    private InputStream resolvePreviewInputStream(HttpURLConnection connection) throws IOException {
        InputStream errorStream = connection.getErrorStream();
        if (errorStream != null && connection.getResponseCode() >= 400) {
            return errorStream;
        }
        return connection.getInputStream();
    }

    private Long requireCurrentUserId() {
        Long currentUserId = currentUserGateway.getCurrentUserId();
        if (currentUserId == null) {
            throw new ServiceException(CURRENT_USER_REQUIRED_MESSAGE);
        }
        return currentUserId;
    }

    private String requireCurrentUserType() {
        String currentUserType = trimToNull(currentUserGateway.getCurrentUserType());
        if (currentUserType == null) {
            throw new ServiceException(CURRENT_USER_TYPE_REQUIRED_MESSAGE);
        }
        return currentUserType;
    }

    private String requireCurrentClientId() {
        String currentClientId = trimToNull(currentUserGateway.getCurrentClientId());
        if (currentClientId == null) {
            throw new ServiceException(CURRENT_CLIENT_REQUIRED_MESSAGE);
        }
        return currentClientId;
    }

    private String requireRequestClientId(HttpServletRequest request) {
        String requestClientId = trimToNull(firstNonNull(request.getHeader("clientid"), request.getParameter("clientid")));
        if (requestClientId == null) {
            throw new ServiceException(CURRENT_CLIENT_REQUIRED_MESSAGE);
        }
        return requestClientId;
    }

    private Long parseLongOrNull(String value) {
        String normalized = trimToNull(value);
        if (normalized == null) {
            return null;
        }
        try {
            return Long.parseLong(normalized);
        } catch (NumberFormatException exception) {
            throw new ServiceException(PREVIEW_TOKEN_INVALID_MESSAGE);
        }
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

    private record TimelineResourceReference(
        Long resourceId,
        Long ossId,
        String resourceType,
        String displayName,
        String resourceUrl,
        String externalUrl,
        Date matchTime
    ) {
    }

    private record DetailAccessScope(Long currentUserId,
                                     boolean initiator,
                                     boolean waitingApprover,
                                     boolean finishedApprover) {

        private boolean canView() {
            return initiator || waitingApprover || finishedApprover;
        }
    }

    private record LedgerVisibilityScope(Long currentUserId, boolean allowInitiator) {
    }

    private record PreviewTokenPayload(String scope,
                                       Long resourceId,
                                       Long userId,
                                       String userType,
                                       String clientId,
                                       Long expireAtEpochSecond) {
    }
}
