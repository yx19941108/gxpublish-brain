package com.gxpublish.brain.manuscript.review.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gxpublish.brain.manuscript.review.domain.entity.ManuscriptReviewAttachmentEntity;
import com.gxpublish.brain.manuscript.review.domain.entity.ManuscriptReviewExternalLinkEntity;
import com.gxpublish.brain.manuscript.review.domain.entity.ManuscriptReviewFlowConfigEntity;
import com.gxpublish.brain.manuscript.review.domain.entity.ManuscriptReviewHistoryEntity;
import com.gxpublish.brain.manuscript.review.domain.entity.ManuscriptReviewRecordEntity;
import com.gxpublish.brain.manuscript.review.domain.entity.ManuscriptReviewSystemRoleEntity;
import com.gxpublish.brain.manuscript.review.domain.entity.ManuscriptReviewSystemUserEntity;
import com.gxpublish.brain.manuscript.review.domain.entity.ManuscriptReviewSystemUserRoleEntity;
import com.gxpublish.brain.manuscript.review.domain.entity.ManuscriptReviewVideoMarkerEntity;
import com.gxpublish.brain.manuscript.review.gateway.ManuscriptReviewCurrentUserGateway;
import com.gxpublish.brain.manuscript.review.gateway.ManuscriptReviewSerialGateway;
import com.gxpublish.brain.manuscript.review.mapper.ManuscriptReviewAttachmentMapper;
import com.gxpublish.brain.manuscript.review.mapper.ManuscriptReviewExternalLinkMapper;
import com.gxpublish.brain.manuscript.review.mapper.ManuscriptReviewFlowConfigMapper;
import com.gxpublish.brain.manuscript.review.mapper.ManuscriptReviewHistoryMapper;
import com.gxpublish.brain.manuscript.review.mapper.ManuscriptReviewRecordMapper;
import com.gxpublish.brain.manuscript.review.mapper.ManuscriptReviewSystemRoleMapper;
import com.gxpublish.brain.manuscript.review.mapper.ManuscriptReviewSysOssMapper;
import com.gxpublish.brain.manuscript.review.mapper.ManuscriptReviewSystemUserMapper;
import com.gxpublish.brain.manuscript.review.mapper.ManuscriptReviewSystemUserRoleMapper;
import com.gxpublish.brain.manuscript.review.mapper.ManuscriptReviewVideoMarkerMapper;
import com.gxpublish.brain.manuscript.review.service.ManuscriptReviewReadableService;
import com.gxpublish.brain.manuscript.review.service.ManuscriptReviewService;

@Tag("dev")
class ManuscriptReviewSharedApiMvcTest {

    @Test
    void shouldReturnFrozenLedgerPageWithCodeBasedFiltersForApi01() throws Exception {
        SharedApiFixture fixture = new SharedApiFixture(3003L, "000000", 2001L, "张三");
        fixture.storeRecord(buildLedgerRecord(9201L, "SH20260321001", "稿件A-较早", "新华社/要闻", "审批中", "待一级审批",
            "2026-03-21 09:00:00", "2026-03-21 10:00:00"));
        fixture.storeRecord(buildLedgerRecord(9202L, "SH20260321002", "稿件A-较新", "新华社/要闻", "审批中", "待一级审批",
            "2026-03-21 11:00:00", "2026-03-21 12:00:00"));
        fixture.storeRecord(buildLedgerRecord(9203L, "JD20260321001", "不匹配稿件", "客户端/视频", "已退回", "待发起人处理",
            "2026-03-22 09:00:00", "2026-03-22 09:30:00"));

        fixture.perform(get("/workflow/manuscript-review/list")
                .param("keyword", "稿件A")
                .param("processType", "AUDIT")
                .param("mediaChannel", "新华社/要闻")
                .param("businessStatus", "WAITING")
                .param("currentNodeCode", "LEVEL_1")
                .param("startTimeFrom", "2026-03-21 00:00:00")
                .param("startTimeTo", "2026-03-21 23:59:59")
                .param("pageNum", "1")
                .param("pageSize", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.total").value(2))
            .andExpect(jsonPath("$.rows", hasSize(1)))
            .andExpect(jsonPath("$.rows[0].id").value(9202L))
            .andExpect(jsonPath("$.rows[0].processType").value("AUDIT"))
            .andExpect(jsonPath("$.rows[0].processTypeLabel").value("审核流程"))
            .andExpect(jsonPath("$.rows[0].mediaChannel").value("新华社/要闻"))
            .andExpect(jsonPath("$.rows[0].businessStatus").value("WAITING"))
            .andExpect(jsonPath("$.rows[0].businessStatusLabel").value("审批中"))
            .andExpect(jsonPath("$.rows[0].currentNodeCode").value("LEVEL_1"))
            .andExpect(jsonPath("$.rows[0].currentNodeLabel").value("待一级审批"))
            .andExpect(jsonPath("$.rows[0].permissionMatrix").doesNotExist())
            .andExpect(jsonPath("$.rows[0].canEdit").doesNotExist());
    }

    @Test
    void shouldReturnFrozenDetailVoFromNewWriteSideFieldsForApi02() throws Exception {
        SharedApiFixture fixture = new SharedApiFixture(3003L, "000000", 2001L, "张三");
        ManuscriptReviewRecordEntity record = new ManuscriptReviewRecordEntity();
        record.setId(9301L);
        record.setTenantId("000000");
        record.setProcessType("AUDIT");
        record.setFlowStatusLabel("已退回");
        record.setCurrentNodeLabel("待发起人处理");
        record.setManuscriptCode("SH20260321010");
        record.setExternalManuscriptCode("EXT-9301");
        record.setTitle("共享详情稿件");
        record.setMediaChannel("新媒体/新栏目");
        record.setSubmitDepartment("新报送部门");
        record.setAuthorName("新作者A、新作者B");
        record.setRemarkText("新说明");
        record.setContentBody("新正文内容");
        record.setFirstSubmitTime(dateTime("2026-03-21 08:00:00"));
        record.setLatestSubmitTime(dateTime("2026-03-24 09:30:00"));
        record.setMediaChannelLabel("旧媒体/旧栏目");
        record.setSubmitterDeptName("旧部门");
        record.setAuthorNames("旧作者");
        record.setNote("旧说明");
        record.setContent("旧正文");
        record.setInitiatorUserId(3003L);
        record.setInitiatorName("张三");
        record.setCreateTime(dateTime("2026-03-21 07:30:00"));
        record.setUpdateTime(dateTime("2026-03-24 09:35:00"));
        fixture.storeRecord(record);

        fixture.storeAttachment(buildAttachment(9401L, 9301L, false, "现行附件.pdf", "https://files.example/current.pdf", "1",
            "2026-03-21 08:05:00", null));
        fixture.storeAttachment(buildAttachment(9402L, 9301L, false, "停用附件.pdf", "https://files.example/disabled.pdf", "0",
            "2026-03-21 08:06:00", "2026-03-24 08:00:00"));
        fixture.storeAttachment(buildAttachment(9403L, 9301L, true, "样片.mp4", "https://files.example/video.mp4", "1",
            "2026-03-21 08:10:00", null));
        fixture.storeAttachment(buildAttachment(9404L, 9301L, true, "补充样片.mp4", "https://files.example/video-2.mp4", "1",
            "2026-03-21 08:11:00", null));
        fixture.storeExternalLink(buildExternalLink(9501L, 9301L, "素材参考", "https://example.com/ref", "1",
            "2026-03-21 08:12:00", null));
        fixture.storeExternalLink(buildExternalLink(9502L, 9301L, "停用外链", "https://example.com/old", "0",
            "2026-03-21 08:13:00", "2026-03-24 08:05:00"));
        fixture.storeVideoMarker(buildVideoMark(9601L, 9301L, 9403L, "00:00:05", "00:00:12", "当前标注", "1",
            "2026-03-21 08:20:00", null));
        fixture.storeVideoMarker(buildVideoMark(9603L, 9301L, 9404L, "00:00:08", "00:00:15", "第二视频标注", "1",
            "2026-03-21 08:21:00", null));
        fixture.storeVideoMarker(buildVideoMark(9602L, 9301L, 9403L, "00:00:15", null, "停用标注", "0",
            "2026-03-21 08:25:00", "2026-03-24 08:10:00"));
        fixture.storeHistory(buildHistory(9701L, 9301L, "CREATE", "张三新增了流程。", "张三", "2026-03-21 08:00:00"));
        fixture.storeHistory(buildHistory(9702L, 9301L, "RESOURCE_DISABLE", "李四停用了附件《停用附件.pdf》。", "李四",
            "2026-03-24 08:00:00"));
        fixture.storeHistory(buildHistory(9703L, 9301L, "RESOURCE_DISABLE", "李四停用了外链《停用外链》。", "李四",
            "2026-03-24 08:05:00"));
        fixture.storeHistory(buildHistory(9704L, 9301L, "RETURN_TO_INITIATOR", "李四退回给发起人：请补充说明。", "李四",
            "2026-03-24 09:00:00"));

        fixture.perform(get("/workflow/manuscript-review/{id}", 9301L))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.id").value(9301L))
            .andExpect(jsonPath("$.data.processType").value("AUDIT"))
            .andExpect(jsonPath("$.data.processTypeLabel").value("审核流程"))
            .andExpect(jsonPath("$.data.mediaChannel").value("新媒体/新栏目"))
            .andExpect(jsonPath("$.data.submitDepartment").value("新报送部门"))
            .andExpect(jsonPath("$.data.authorName").value("新作者A、新作者B"))
            .andExpect(jsonPath("$.data.remark").value("新说明"))
            .andExpect(jsonPath("$.data.contentBody").value("新正文内容"))
            .andExpect(jsonPath("$.data.businessStatus").value("BACK"))
            .andExpect(jsonPath("$.data.businessStatusLabel").value("已退回"))
            .andExpect(jsonPath("$.data.currentNodeCode").value("RETURN_TO_INITIATOR"))
            .andExpect(jsonPath("$.data.currentNodeStatus").value("RETURN_TO_INITIATOR"))
            .andExpect(jsonPath("$.data.currentNodeLabel").value("待发起人处理"))
            .andExpect(jsonPath("$.data.firstSubmitTime").value("2026-03-21 08:00:00"))
            .andExpect(jsonPath("$.data.latestSubmitTime").value("2026-03-24 09:30:00"))
            .andExpect(jsonPath("$.data.attachmentList", hasSize(1)))
            .andExpect(jsonPath("$.data.attachmentList[0].id").value(9401L))
            .andExpect(jsonPath("$.data.attachmentList[0].ossId").value(8801L))
            .andExpect(jsonPath("$.data.attachmentList[0].resourceUrl").value("https://files.example/current.pdf"))
            .andExpect(jsonPath("$.data.externalLinkList", hasSize(1)))
            .andExpect(jsonPath("$.data.externalLinkList[0].id").value(9501L))
            .andExpect(jsonPath("$.data.externalLinkList[0].externalUrl").value("https://example.com/ref"))
            .andExpect(jsonPath("$.data.videoList", hasSize(2)))
            .andExpect(jsonPath("$.data.videoList[0].id").value(9403L))
            .andExpect(jsonPath("$.data.videoList[0].ossId").value(8803L))
            .andExpect(jsonPath("$.data.videoList[0].resourceUrl").value("https://files.example/video.mp4"))
            .andExpect(jsonPath("$.data.videoList[1].id").value(9404L))
            .andExpect(jsonPath("$.data.videoList[1].resourceUrl").value("https://files.example/video-2.mp4"))
            .andExpect(jsonPath("$.data.videoMarkList", hasSize(2)))
            .andExpect(jsonPath("$.data.videoMarkList[0].id").value(9601L))
            .andExpect(jsonPath("$.data.videoMarkList[0].resourceId").value(9403L))
            .andExpect(jsonPath("$.data.videoMarkList[1].id").value(9603L))
            .andExpect(jsonPath("$.data.videoMarkList[1].resourceId").value(9404L))
            .andExpect(jsonPath("$.data.timelineItems", hasSize(4)))
            .andExpect(jsonPath("$.data.timelineItems[0].eventText").value("张三新增了流程。"))
            .andExpect(jsonPath("$.data.timelineItems[1].relatedResourceName").value("停用附件.pdf"))
            .andExpect(jsonPath("$.data.timelineItems[1].relatedResourceType").value("ATTACHMENT"))
            .andExpect(jsonPath("$.data.timelineItems[1].relatedResourceOssId").value(8802L))
            .andExpect(jsonPath("$.data.timelineItems[1].relatedResourceUrl").value("https://files.example/disabled.pdf"))
            .andExpect(jsonPath("$.data.timelineItems[2].relatedResourceName").value("停用外链"))
            .andExpect(jsonPath("$.data.timelineItems[2].relatedResourceType").value("EXTERNAL_LINK"))
            .andExpect(jsonPath("$.data.timelineItems[2].relatedExternalUrl").value("https://example.com/old"))
            .andExpect(jsonPath("$.data.permissionMatrix.canResubmit").value(true))
            .andExpect(jsonPath("$.data.permissionMatrix.canGotoApproval").value(false))
            .andExpect(content().string(not(containsString("旧媒体/旧栏目"))))
            .andExpect(content().string(not(containsString("旧部门"))))
            .andExpect(content().string(not(containsString("旧正文"))));
    }

    @Test
    void shouldExposeSeparatedNonStubWriteEndpointsForApi03() throws Exception {
        SharedApiFixture fixture = new SharedApiFixture(3003L, "000000", 2001L, "张三");
        String createPayload = fixture.json(Map.of(
            "processType", "AUDIT",
            "externalManuscriptCode", "EXT-API03",
            "title", "接口边界稿件",
            "mediaChannel", "新华社/要闻",
            "submitDepartment", "总编室",
            "authorName", "张三、李四",
            "remark", "新建说明",
            "contentBody", "正文内容"
        ));

        fixture.perform(post("/workflow/manuscript-review")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createPayload))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data").value(9001L));

        fixture.perform(post("/workflow/manuscript-review/resource")
                .contentType(MediaType.APPLICATION_JSON)
                .content(fixture.json(Map.of(
                    "reviewId", 9001L,
                    "resourceType", "EXTERNAL_LINK",
                    "displayName", "素材参考",
                    "externalUrl", "https://example.com/api03"
                ))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.id").value(9003L))
            .andExpect(jsonPath("$.data.resourceType").value("EXTERNAL_LINK"));

        fixture.perform(post("/workflow/manuscript-review/submitAndFlowStart")
                .contentType(MediaType.APPLICATION_JSON)
                .content(fixture.json(Map.of("id", 9001L))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.id").value(9001L))
            .andExpect(jsonPath("$.data.manuscriptCode").value("SH20260321001"))
            .andExpect(jsonPath("$.data.latestSubmitTime").value("2026-03-21 00:00:00"));

        fixture.storeHistory(buildHistory(9801L, 9001L, "RETURN_TO_INITIATOR", "李四退回给发起人：请补充说明。", "李四",
            "2026-03-21 00:10:00"));
        fixture.patchRecordStatus(9001L, "已退回", "待发起人处理");

        fixture.perform(put("/workflow/manuscript-review")
                .contentType(MediaType.APPLICATION_JSON)
                .content(fixture.json(Map.of(
                    "id", 9001L,
                    "processType", "AUDIT",
                    "externalManuscriptCode", "EXT-API03",
                    "title", "接口边界稿件-修改后",
                    "mediaChannel", "新华社/要闻",
                    "submitDepartment", "总编室",
                    "authorName", "张三、李四",
                    "remark", "修改说明",
                    "contentBody", "修改后的正文内容"
                ))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));

        fixture.perform(post("/workflow/manuscript-review/resubmit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(fixture.json(Map.of("id", 9001L))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.id").value(9001L))
            .andExpect(jsonPath("$.data.title").value("接口边界稿件-修改后"));

        ManuscriptReviewRecordEntity cancelRecord = buildLedgerRecord(9009L, "SH20260321009", "待撤销稿件", "新华社/要闻",
            "审批中", "待一级审批", "2026-03-21 00:00:00", "2026-03-21 00:00:00");
        cancelRecord.setInitiatorUserId(3003L);
        cancelRecord.setInitiatorName("张三");
        fixture.storeRecord(cancelRecord);

        fixture.perform(put("/workflow/manuscript-review/cancelProcessApply")
                .contentType(MediaType.APPLICATION_JSON)
                .content(fixture.json(Map.of(
                    "id", 9009L,
                    "reason", "发起人主动撤销"
                ))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void shouldDisableVideoMarkViaApiAndAppendHistory() throws Exception {
        SharedApiFixture fixture = new SharedApiFixture(3003L, "000000", 2001L, "张三");
        fixture.storeRecord(buildLedgerRecord(9310L, "SH20260321010", "标注停用稿件", "新华社/要闻",
            "已退回", "待发起人处理", "2026-03-21 08:00:00", "2026-03-21 08:30:00"));
        fixture.storeAttachment(buildAttachment(9410L, 9310L, true, "样片.mp4", "https://files.example/video.mp4", "1",
            "2026-03-21 08:10:00", null));
        fixture.storeVideoMarker(buildVideoMark(9610L, 9310L, 9410L, "00:00:05", "00:00:10", "第一处问题", "1",
            "2026-03-21 08:20:00", null));

        fixture.perform(put("/workflow/manuscript-review/video-mark/disable")
                .contentType(MediaType.APPLICATION_JSON)
                .content(fixture.json(Map.of(
                    "markId", 9610L,
                    "disabledReason", "标注已废弃"
                ))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));

        assertEquals("0", fixture.videoMarkers.get(9610L).getEnabled());
        assertEquals("标注已废弃", fixture.videoMarkers.get(9610L).getRemark());
        assertTrue(fixture.histories.stream().anyMatch(history ->
            "VIDEO_MARK_DISABLE".equals(history.getActionType())
                && "张三停用了视频标注《第一处问题》（00:00:05 - 00:00:10）。".equals(history.getActionText())));
    }

    private static ManuscriptReviewRecordEntity buildLedgerRecord(Long reviewId,
                                                                  String manuscriptCode,
                                                                  String title,
                                                                  String mediaChannel,
                                                                  String flowStatusLabel,
                                                                  String currentNodeLabel,
                                                                  String firstSubmitTime,
                                                                  String updateTime) {
        ManuscriptReviewRecordEntity entity = new ManuscriptReviewRecordEntity();
        entity.setId(reviewId);
        entity.setTenantId("000000");
        entity.setProcessType("AUDIT");
        entity.setFlowStatusLabel(flowStatusLabel);
        entity.setCurrentNodeLabel(currentNodeLabel);
        entity.setManuscriptCode(manuscriptCode);
        entity.setExternalManuscriptCode("EXT-" + reviewId);
        entity.setTitle(title);
        entity.setMediaChannel(mediaChannel);
        entity.setSubmitDepartment("总编室");
        entity.setAuthorName("张三");
        entity.setRemarkText("说明");
        entity.setContentBody("正文内容");
        entity.setFirstSubmitTime(dateTime(firstSubmitTime));
        entity.setLatestSubmitTime(dateTime(firstSubmitTime));
        entity.setInitiatorUserId(3003L);
        entity.setInitiatorName("张三");
        entity.setCreateTime(dateTime(firstSubmitTime));
        entity.setUpdateTime(dateTime(updateTime));
        return entity;
    }

    private static ManuscriptReviewAttachmentEntity buildAttachment(Long id, Long reviewId, boolean video, String fileName,
                                                                    String fileUrl, String enabled, String createTime,
                                                                    String disabledTime) {
        ManuscriptReviewAttachmentEntity entity = new ManuscriptReviewAttachmentEntity();
        entity.setId(id);
        entity.setTenantId("000000");
        entity.setReviewId(reviewId);
        entity.setFileName(fileName);
        entity.setOssId(id - 600L);
        entity.setFileUrl(fileUrl);
        entity.setIsVideo(video);
        entity.setVideoDurationSeconds(video ? 600 : null);
        entity.setEnabled(enabled);
        entity.setCreateTime(dateTime(createTime));
        entity.setDisabledTime(disabledTime == null ? null : dateTime(disabledTime));
        return entity;
    }

    private static ManuscriptReviewExternalLinkEntity buildExternalLink(Long id, Long reviewId, String title, String url,
                                                                        String enabled, String createTime,
                                                                        String disabledTime) {
        ManuscriptReviewExternalLinkEntity entity = new ManuscriptReviewExternalLinkEntity();
        entity.setId(id);
        entity.setTenantId("000000");
        entity.setReviewId(reviewId);
        entity.setLinkTitle(title);
        entity.setLinkUrl(url);
        entity.setEnabled(enabled);
        entity.setCreateTime(dateTime(createTime));
        entity.setDisabledTime(disabledTime == null ? null : dateTime(disabledTime));
        return entity;
    }

    private static ManuscriptReviewVideoMarkerEntity buildVideoMark(Long id, Long reviewId, Long videoAttachmentId,
                                                                    String startTime, String endTime, String markContent,
                                                                    String enabled, String createTime,
                                                                    String disabledTime) {
        ManuscriptReviewVideoMarkerEntity entity = new ManuscriptReviewVideoMarkerEntity();
        entity.setId(id);
        entity.setTenantId("000000");
        entity.setReviewId(reviewId);
        entity.setVideoAttachmentId(videoAttachmentId);
        entity.setStartTime(startTime);
        entity.setEndTime(endTime);
        entity.setMarkerNote(markContent);
        entity.setEnabled(enabled);
        entity.setCreateTime(dateTime(createTime));
        entity.setDisabledTime(disabledTime == null ? null : dateTime(disabledTime));
        return entity;
    }

    private static ManuscriptReviewHistoryEntity buildHistory(Long id, Long reviewId, String actionType, String actionText,
                                                              String actorName, String createTime) {
        ManuscriptReviewHistoryEntity entity = new ManuscriptReviewHistoryEntity();
        entity.setId(id);
        entity.setReviewId(reviewId);
        entity.setActionType(actionType);
        entity.setActionText(actionText);
        entity.setActorName(actorName);
        entity.setCreateTime(dateTime(createTime));
        return entity;
    }

    private static Date dateTime(String value) {
        LocalDateTime dateTime = LocalDateTime.parse(value.replace(" ", "T"));
        return Date.from(dateTime.atZone(ZoneId.of("Asia/Shanghai")).toInstant());
    }

    private static final class SharedApiFixture {

        private static final ZoneId BUSINESS_ZONE_ID = ZoneId.of("Asia/Shanghai");

        private final ObjectMapper objectMapper = new ObjectMapper();
        private final AtomicLong idSequence = new AtomicLong(9000L);

        private final Map<Long, ManuscriptReviewRecordEntity> records = new LinkedHashMap<>();
        private final Map<Long, ManuscriptReviewAttachmentEntity> attachments = new LinkedHashMap<>();
        private final Map<Long, ManuscriptReviewExternalLinkEntity> externalLinks = new LinkedHashMap<>();
        private final Map<Long, ManuscriptReviewVideoMarkerEntity> videoMarkers = new LinkedHashMap<>();
        private final List<ManuscriptReviewHistoryEntity> histories = new ArrayList<>();

        private final ManuscriptReviewRecordMapper recordMapper = mock(ManuscriptReviewRecordMapper.class);
        private final ManuscriptReviewAttachmentMapper attachmentMapper = mock(ManuscriptReviewAttachmentMapper.class);
        private final ManuscriptReviewExternalLinkMapper externalLinkMapper = mock(ManuscriptReviewExternalLinkMapper.class);
        private final ManuscriptReviewVideoMarkerMapper videoMarkerMapper = mock(ManuscriptReviewVideoMarkerMapper.class);
        private final ManuscriptReviewHistoryMapper historyMapper = mock(ManuscriptReviewHistoryMapper.class);
        private final ManuscriptReviewFlowConfigMapper flowConfigMapper = mock(ManuscriptReviewFlowConfigMapper.class);
        private final ManuscriptReviewSystemRoleMapper roleMapper = mock(ManuscriptReviewSystemRoleMapper.class);
        private final ManuscriptReviewSysOssMapper sysOssMapper = mock(ManuscriptReviewSysOssMapper.class);
        private final ManuscriptReviewSystemUserRoleMapper userRoleMapper = mock(ManuscriptReviewSystemUserRoleMapper.class);
        private final ManuscriptReviewSystemUserMapper userMapper = mock(ManuscriptReviewSystemUserMapper.class);
        private final ManuscriptReviewSerialGateway serialGateway = mock(ManuscriptReviewSerialGateway.class);
        private final ManuscriptReviewCurrentUserGateway currentUserGateway = mock(ManuscriptReviewCurrentUserGateway.class);

        private final MockMvc mockMvc;

        private SharedApiFixture(Long currentUserId, String tenantId, Long currentDeptId, String currentUsername) {
            when(currentUserGateway.getCurrentUserId()).thenReturn(currentUserId);
            when(currentUserGateway.getCurrentTenantId()).thenReturn(tenantId);
            when(currentUserGateway.getCurrentDeptId()).thenReturn(currentDeptId);
            when(currentUserGateway.getCurrentUsername()).thenReturn(currentUsername);
            when(serialGateway.nextSerial(any(), any())).thenReturn(1);
            when(recordMapper.selectById(any())).thenAnswer(invocation -> records.get(invocation.getArgument(0, Long.class)));
            when(recordMapper.selectList(any())).thenAnswer(invocation -> new ArrayList<>(records.values()));
            when(recordMapper.selectCount(any())).thenReturn(0L);
            when(attachmentMapper.selectById(any())).thenAnswer(invocation -> attachments.get(invocation.getArgument(0, Long.class)));
            when(attachmentMapper.selectList(any())).thenAnswer(invocation -> new ArrayList<>(attachments.values()));
            when(externalLinkMapper.selectById(any())).thenAnswer(invocation -> externalLinks.get(invocation.getArgument(0, Long.class)));
            when(externalLinkMapper.selectList(any())).thenAnswer(invocation -> new ArrayList<>(externalLinks.values()));
            when(videoMarkerMapper.selectById(any())).thenAnswer(invocation -> videoMarkers.get(invocation.getArgument(0, Long.class)));
            when(videoMarkerMapper.selectList(any())).thenAnswer(invocation -> new ArrayList<>(videoMarkers.values()));
            when(historyMapper.selectList(any())).thenAnswer(invocation -> new ArrayList<>(histories));
            ManuscriptReviewFlowConfigEntity flowConfig = new ManuscriptReviewFlowConfigEntity();
            flowConfig.setTenantId(tenantId);
            flowConfig.setProcessType("AUDIT");
            flowConfig.setFlowCode("manuscript_review_audit_flow");
            flowConfig.setLevelOneRoleKey("manuscript_review_level_1_approver");
            flowConfig.setLevelTwoRoleKey("manuscript_review_level_2_approver");
            flowConfig.setLevelThreeRoleKey("manuscript_review_level_3_approver");
            flowConfig.setStatus("0");
            when(flowConfigMapper.selectOne(any())).thenReturn(flowConfig);
            when(roleMapper.selectList(any())).thenReturn(List.of());

            ManuscriptReviewSystemRoleEntity activeRole = new ManuscriptReviewSystemRoleEntity();
            activeRole.setRoleId(7101L);
            activeRole.setTenantId(tenantId);
            activeRole.setRoleKey("manuscript_review_level_1_approver");
            activeRole.setStatus("0");
            activeRole.setDelFlag("0");
            when(roleMapper.selectOne(any())).thenReturn(activeRole);

            ManuscriptReviewSystemUserRoleEntity activeUserRole = new ManuscriptReviewSystemUserRoleEntity();
            activeUserRole.setUserId(currentUserId);
            activeUserRole.setRoleId(7101L);
            when(userRoleMapper.selectList(any())).thenReturn(List.of(activeUserRole));

            ManuscriptReviewSystemUserEntity activeUser = new ManuscriptReviewSystemUserEntity();
            activeUser.setUserId(currentUserId);
            activeUser.setTenantId(tenantId);
            activeUser.setStatus("0");
            activeUser.setDelFlag("0");
            when(userMapper.selectList(any())).thenReturn(List.of(activeUser));

            when(recordMapper.insert(any(ManuscriptReviewRecordEntity.class))).thenAnswer(invocation -> {
                ManuscriptReviewRecordEntity entity = invocation.getArgument(0, ManuscriptReviewRecordEntity.class);
                records.put(entity.getId(), entity);
                return 1;
            });
            when(recordMapper.updateById(any(ManuscriptReviewRecordEntity.class))).thenAnswer(invocation -> {
                ManuscriptReviewRecordEntity entity = invocation.getArgument(0, ManuscriptReviewRecordEntity.class);
                ManuscriptReviewRecordEntity current = records.get(entity.getId());
                if (current != null) {
                    mergeRecord(current, entity);
                }
                return 1;
            });
            when(attachmentMapper.insert(any(ManuscriptReviewAttachmentEntity.class))).thenAnswer(invocation -> {
                ManuscriptReviewAttachmentEntity entity = invocation.getArgument(0, ManuscriptReviewAttachmentEntity.class);
                attachments.put(entity.getId(), entity);
                return 1;
            });
            when(attachmentMapper.updateById(any(ManuscriptReviewAttachmentEntity.class))).thenAnswer(invocation -> {
                ManuscriptReviewAttachmentEntity entity = invocation.getArgument(0, ManuscriptReviewAttachmentEntity.class);
                ManuscriptReviewAttachmentEntity current = attachments.get(entity.getId());
                if (current != null) {
                    mergeAttachment(current, entity);
                }
                return 1;
            });
            when(externalLinkMapper.insert(any(ManuscriptReviewExternalLinkEntity.class))).thenAnswer(invocation -> {
                ManuscriptReviewExternalLinkEntity entity = invocation.getArgument(0, ManuscriptReviewExternalLinkEntity.class);
                externalLinks.put(entity.getId(), entity);
                return 1;
            });
            when(externalLinkMapper.updateById(any(ManuscriptReviewExternalLinkEntity.class))).thenAnswer(invocation -> {
                ManuscriptReviewExternalLinkEntity entity = invocation.getArgument(0, ManuscriptReviewExternalLinkEntity.class);
                ManuscriptReviewExternalLinkEntity current = externalLinks.get(entity.getId());
                if (current != null) {
                    mergeExternalLink(current, entity);
                }
                return 1;
            });
            when(videoMarkerMapper.insert(any(ManuscriptReviewVideoMarkerEntity.class))).thenAnswer(invocation -> {
                ManuscriptReviewVideoMarkerEntity entity = invocation.getArgument(0, ManuscriptReviewVideoMarkerEntity.class);
                videoMarkers.put(entity.getId(), entity);
                return 1;
            });
            when(videoMarkerMapper.updateById(any(ManuscriptReviewVideoMarkerEntity.class))).thenAnswer(invocation -> {
                ManuscriptReviewVideoMarkerEntity entity = invocation.getArgument(0, ManuscriptReviewVideoMarkerEntity.class);
                ManuscriptReviewVideoMarkerEntity current = videoMarkers.get(entity.getId());
                if (current != null) {
                    mergeVideoMarker(current, entity);
                }
                return 1;
            });
            when(historyMapper.insert(any(ManuscriptReviewHistoryEntity.class))).thenAnswer(invocation -> {
                ManuscriptReviewHistoryEntity entity = invocation.getArgument(0, ManuscriptReviewHistoryEntity.class);
                histories.add(entity);
                return 1;
            });

            com.gxpublish.brain.common.core.service.WorkflowService workflowService =
                mock(com.gxpublish.brain.common.core.service.WorkflowService.class);
            com.gxpublish.brain.workflow.service.IFlwInstanceService flwInstanceService =
                mock(com.gxpublish.brain.workflow.service.IFlwInstanceService.class);
            when(workflowService.startCompleteTask(any(com.gxpublish.brain.common.core.domain.dto.StartProcessDTO.class))).thenReturn(true);
            when(workflowService.getInstanceIdByBusinessId(any())).thenReturn(99200L);

            ManuscriptReviewService manuscriptReviewService = new ManuscriptReviewService(
                recordMapper,
                attachmentMapper,
                externalLinkMapper,
                videoMarkerMapper,
                historyMapper,
                flowConfigMapper,
                roleMapper,
                userRoleMapper,
                userMapper,
                serialGateway,
                currentUserGateway,
                workflowService,
                flwInstanceService,
                sysOssMapper,
                Clock.fixed(LocalDate.of(2026, 3, 21).atStartOfDay(BUSINESS_ZONE_ID).toInstant(), BUSINESS_ZONE_ID),
                idSequence::incrementAndGet
            );
            ManuscriptReviewReadableService readableService = new ManuscriptReviewReadableService(
                recordMapper,
                attachmentMapper,
                externalLinkMapper,
                historyMapper,
                videoMarkerMapper,
                flowConfigMapper,
                roleMapper,
                userRoleMapper,
                userMapper,
                currentUserGateway
            );

            ManuscriptReviewApiController apiController = new ManuscriptReviewApiController();
            ReflectionTestUtils.setField(apiController, "manuscriptReviewService", manuscriptReviewService);
            ReflectionTestUtils.setField(apiController, "manuscriptReviewReadableService", readableService);

            this.mockMvc = MockMvcBuilders.standaloneSetup(apiController, new ManuscriptReviewReadableApiController(readableService))
                .build();
        }

        private ResultActions perform(MockHttpServletRequestBuilder requestBuilder) throws Exception {
            return mockMvc.perform(requestBuilder);
        }

        private String json(Object value) throws Exception {
            return objectMapper.writeValueAsString(value);
        }

        private void storeRecord(ManuscriptReviewRecordEntity entity) {
            records.put(entity.getId(), entity);
        }

        private void patchRecordStatus(Long id, String flowStatusLabel, String currentNodeLabel) {
            ManuscriptReviewRecordEntity record = records.get(id);
            if (record != null) {
                record.setFlowStatusLabel(flowStatusLabel);
                record.setCurrentNodeLabel(currentNodeLabel);
            }
        }

        private void storeAttachment(ManuscriptReviewAttachmentEntity entity) {
            attachments.put(entity.getId(), entity);
        }

        private void storeExternalLink(ManuscriptReviewExternalLinkEntity entity) {
            externalLinks.put(entity.getId(), entity);
        }

        private void storeVideoMarker(ManuscriptReviewVideoMarkerEntity entity) {
            videoMarkers.put(entity.getId(), entity);
        }

        private void storeHistory(ManuscriptReviewHistoryEntity entity) {
            histories.add(entity);
        }

        private static void mergeRecord(ManuscriptReviewRecordEntity current, ManuscriptReviewRecordEntity patch) {
            if (patch.getProcessType() != null) {
                current.setProcessType(patch.getProcessType());
            }
            if (patch.getFlowStatusLabel() != null) {
                current.setFlowStatusLabel(patch.getFlowStatusLabel());
            }
            if (patch.getCurrentNodeLabel() != null) {
                current.setCurrentNodeLabel(patch.getCurrentNodeLabel());
            }
            if (patch.getManuscriptCode() != null) {
                current.setManuscriptCode(patch.getManuscriptCode());
            }
            if (patch.getExternalManuscriptCode() != null) {
                current.setExternalManuscriptCode(patch.getExternalManuscriptCode());
            }
            if (patch.getTitle() != null) {
                current.setTitle(patch.getTitle());
            }
            if (patch.getMediaChannel() != null) {
                current.setMediaChannel(patch.getMediaChannel());
            }
            if (patch.getSubmitDepartment() != null) {
                current.setSubmitDepartment(patch.getSubmitDepartment());
            }
            if (patch.getAuthorName() != null) {
                current.setAuthorName(patch.getAuthorName());
            }
            if (patch.getRemarkText() != null) {
                current.setRemarkText(patch.getRemarkText());
            }
            if (patch.getContentBody() != null) {
                current.setContentBody(patch.getContentBody());
            }
            if (patch.getFirstSubmitTime() != null) {
                current.setFirstSubmitTime(patch.getFirstSubmitTime());
            }
            if (patch.getLatestSubmitTime() != null) {
                current.setLatestSubmitTime(patch.getLatestSubmitTime());
            }
            if (patch.getUpdateBy() != null) {
                current.setUpdateBy(patch.getUpdateBy());
            }
            if (patch.getUpdateTime() != null) {
                current.setUpdateTime(patch.getUpdateTime());
            }
        }

        private static void mergeAttachment(ManuscriptReviewAttachmentEntity current, ManuscriptReviewAttachmentEntity patch) {
            if (patch.getEnabled() != null) {
                current.setEnabled(patch.getEnabled());
            }
            if (patch.getDisabledBy() != null) {
                current.setDisabledBy(patch.getDisabledBy());
            }
            if (patch.getDisabledTime() != null) {
                current.setDisabledTime(patch.getDisabledTime());
            }
            if (patch.getRemark() != null) {
                current.setRemark(patch.getRemark());
            }
            if (patch.getUpdateBy() != null) {
                current.setUpdateBy(patch.getUpdateBy());
            }
            if (patch.getUpdateTime() != null) {
                current.setUpdateTime(patch.getUpdateTime());
            }
        }

        private static void mergeExternalLink(ManuscriptReviewExternalLinkEntity current, ManuscriptReviewExternalLinkEntity patch) {
            if (patch.getEnabled() != null) {
                current.setEnabled(patch.getEnabled());
            }
            if (patch.getDisabledBy() != null) {
                current.setDisabledBy(patch.getDisabledBy());
            }
            if (patch.getDisabledTime() != null) {
                current.setDisabledTime(patch.getDisabledTime());
            }
            if (patch.getRemark() != null) {
                current.setRemark(patch.getRemark());
            }
            if (patch.getUpdateBy() != null) {
                current.setUpdateBy(patch.getUpdateBy());
            }
            if (patch.getUpdateTime() != null) {
                current.setUpdateTime(patch.getUpdateTime());
            }
        }

        private static void mergeVideoMarker(ManuscriptReviewVideoMarkerEntity current, ManuscriptReviewVideoMarkerEntity patch) {
            if (patch.getEnabled() != null) {
                current.setEnabled(patch.getEnabled());
            }
            if (patch.getDisabledBy() != null) {
                current.setDisabledBy(patch.getDisabledBy());
            }
            if (patch.getDisabledTime() != null) {
                current.setDisabledTime(patch.getDisabledTime());
            }
            if (patch.getRemark() != null) {
                current.setRemark(patch.getRemark());
            }
            if (patch.getUpdateBy() != null) {
                current.setUpdateBy(patch.getUpdateBy());
            }
            if (patch.getUpdateTime() != null) {
                current.setUpdateTime(patch.getUpdateTime());
            }
        }
    }
}
