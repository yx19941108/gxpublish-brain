package com.gxpublish.brain.manuscript.review.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.gxpublish.brain.common.core.exception.ServiceException;
import com.gxpublish.brain.manuscript.review.domain.command.CreateManuscriptReviewCommand;
import com.gxpublish.brain.manuscript.review.domain.command.ResubmitManuscriptReviewCommand;
import com.gxpublish.brain.manuscript.review.domain.enums.ManuscriptReviewProcessType;
import com.gxpublish.brain.manuscript.review.domain.result.ManuscriptReviewResult;
import com.gxpublish.brain.manuscript.review.gateway.ManuscriptReviewConfigGateway;
import com.gxpublish.brain.manuscript.review.gateway.ManuscriptReviewCurrentUserGateway;
import com.gxpublish.brain.manuscript.review.gateway.ManuscriptReviewResubmitGateway;
import com.gxpublish.brain.manuscript.review.gateway.ManuscriptReviewSerialGateway;
import com.gxpublish.brain.manuscript.review.gateway.ManuscriptReviewUserRoleGateway;

@Tag("dev")
class ManuscriptReviewServiceTest {

    private static final ZoneId BUSINESS_ZONE_ID = ZoneId.of("Asia/Shanghai");

    @Test
    void shouldCreateAndSubmitWithoutDraftForNormalApplicant() {
        ServiceFixture fixture = new ServiceFixture(1001L);
        fixture.stubApprovalChainReady(ManuscriptReviewProcessType.AUDIT);
        when(fixture.serialGateway.nextSerial(ManuscriptReviewProcessType.AUDIT, "20260321")).thenReturn(1);
        when(fixture.userRoleGateway.hasCertifiedApplicantRole(1001L)).thenReturn(false);

        ManuscriptReviewResult result = fixture.service.createAndSubmit(
            CreateManuscriptReviewCommand.builder()
                .processType(ManuscriptReviewProcessType.AUDIT)
                .title("测试稿件")
                .content("正文")
                .attachmentCount(1)
                .build()
        );

        assertEquals("SH20260321001", result.getManuscriptCode());
        assertEquals("审批中", result.getFlowStatus());
        assertEquals("待一级审批", result.getCurrentNode());
        verify(fixture.userRoleGateway).hasCertifiedApplicantRole(1001L);
    }

    @Test
    void shouldRejectSubmitWhenTitleMissing() {
        ServiceFixture fixture = new ServiceFixture(1006L);
        fixture.stubApprovalChainReady(ManuscriptReviewProcessType.AUDIT);

        ServiceException exception = assertThrows(ServiceException.class, () -> fixture.service.createAndSubmit(
            CreateManuscriptReviewCommand.builder()
                .processType(ManuscriptReviewProcessType.AUDIT)
                .title("   ")
                .content("正文")
                .attachmentCount(1)
                .build()
        ));

        assertEquals("标题不能为空", exception.getMessage());
    }

    @Test
    void shouldRejectSubmitWhenContentBlank() {
        ServiceFixture fixture = new ServiceFixture(1007L);
        fixture.stubApprovalChainReady(ManuscriptReviewProcessType.AUDIT);

        ServiceException exception = assertThrows(ServiceException.class, () -> fixture.service.createAndSubmit(
            CreateManuscriptReviewCommand.builder()
                .processType(ManuscriptReviewProcessType.AUDIT)
                .title("正文缺失稿件")
                .content("   ")
                .attachmentCount(1)
                .build()
        ));

        assertEquals("正文不能为空", exception.getMessage());
    }

    @Test
    void shouldRejectSubmitWhenAttachmentAndExternalLinkBothMissing() {
        ServiceFixture fixture = new ServiceFixture(1008L);
        fixture.stubApprovalChainReady(ManuscriptReviewProcessType.AUDIT);
        when(fixture.serialGateway.nextSerial(ManuscriptReviewProcessType.AUDIT, "20260321")).thenReturn(1);
        when(fixture.userRoleGateway.hasCertifiedApplicantRole(1008L)).thenReturn(false);

        ServiceException exception = assertThrows(ServiceException.class, () -> fixture.service.createAndSubmit(
            CreateManuscriptReviewCommand.builder()
                .processType(ManuscriptReviewProcessType.AUDIT)
                .title("缺少附件和外链稿件")
                .content("正文")
                .attachmentCount(0)
                .externalLinkCount(0)
                .build()
        ));

        assertEquals("附件或外链至少提供一种", exception.getMessage());
    }

    @Test
    void shouldRejectSubmitWhenExternalLinkUrlProtocolInvalid() {
        ServiceFixture fixture = new ServiceFixture(1013L);
        fixture.stubApprovalChainReady(ManuscriptReviewProcessType.AUDIT);

        ServiceException exception = assertThrows(ServiceException.class, () -> fixture.service.createAndSubmit(
            CreateManuscriptReviewCommand.builder()
                .processType(ManuscriptReviewProcessType.AUDIT)
                .title("外链协议不合法")
                .content("正文")
                .attachmentCount(1)
                .externalLinkUrls(List.of("ftp://a"))
                .build()
        ));

        assertEquals("外链只允许http/https协议", exception.getMessage());
    }

    @Test
    void shouldRejectSubmitWhenExternalLinkUrlsDuplicateWithinFlow() {
        ServiceFixture fixture = new ServiceFixture(1014L);
        fixture.stubApprovalChainReady(ManuscriptReviewProcessType.AUDIT);

        ServiceException exception = assertThrows(ServiceException.class, () -> fixture.service.createAndSubmit(
            CreateManuscriptReviewCommand.builder()
                .processType(ManuscriptReviewProcessType.AUDIT)
                .title("外链重复")
                .content("正文")
                .attachmentCount(1)
                .externalLinkUrls(List.of("https://a", "https://a"))
                .build()
        ));

        assertEquals("同一流程内URL不允许重复", exception.getMessage());
    }

    @Test
    void shouldRejectResubmitWhenAttachmentAndExternalLinkBothMissing() {
        ServiceFixture fixture = new ServiceFixture(1009L);
        fixture.stubApprovalChainReady(ManuscriptReviewProcessType.AUDIT);
        when(fixture.userRoleGateway.hasCertifiedApplicantRole(1009L)).thenReturn(false);

        ServiceException exception = assertThrows(ServiceException.class, () -> fixture.service.resubmit(
            ResubmitManuscriptReviewCommand.builder()
                .processType(ManuscriptReviewProcessType.AUDIT)
                .title("再次提交缺少附件和外链稿件")
                .content("正文")
                .attachmentCount(0)
                .externalLinkCount(0)
                .build()
        ));

        assertEquals("附件或外链至少提供一种", exception.getMessage());
    }

    @Test
    void shouldRejectResubmitWhenReviewIsNotReturnedToInitiator() {
        ServiceFixture fixture = new ServiceFixture(1011L);
        fixture.stubApprovalChainReady(ManuscriptReviewProcessType.AUDIT);
        when(fixture.userRoleGateway.hasCertifiedApplicantRole(1011L)).thenReturn(false);

        ServiceException exception = assertThrows(ServiceException.class, () -> fixture.service.resubmit(
            ResubmitManuscriptReviewCommand.builder()
                .reviewId(9001L)
                .processType(ManuscriptReviewProcessType.AUDIT)
                .title("测试稿件")
                .content("正文")
                .attachmentCount(1)
                .returnedToInitiator(false)
                .build()
        ));

        assertEquals("当前流程未退回发起人，不能再次提交", exception.getMessage());
        verify(fixture.resubmitGateway).isReturnedToInitiator(9001L, 1011L);
    }

    @Test
    void shouldRejectResubmitWhenGatewayReportsNotReturnedEvenIfCommandClaimsReturnedToInitiator() {
        ServiceFixture fixture = new ServiceFixture(1012L);
        fixture.stubApprovalChainReady(ManuscriptReviewProcessType.AUDIT);
        when(fixture.userRoleGateway.hasCertifiedApplicantRole(1012L)).thenReturn(false);
        when(fixture.resubmitGateway.isReturnedToInitiator(9002L, 1012L)).thenReturn(false);

        ServiceException exception = assertThrows(ServiceException.class, () -> fixture.service.resubmit(
            ResubmitManuscriptReviewCommand.builder()
                .reviewId(9002L)
                .processType(ManuscriptReviewProcessType.AUDIT)
                .title("测试稿件")
                .content("正文")
                .attachmentCount(1)
                .returnedToInitiator(true)
                .build()
        ));

        assertEquals("当前流程未退回发起人，不能再次提交", exception.getMessage());
        verify(fixture.resubmitGateway).isReturnedToInitiator(9002L, 1012L);
    }

    @Test
    void shouldGenerateProofreadCodeWithJdPrefix() {
        ServiceFixture fixture = new ServiceFixture(1002L);
        fixture.stubApprovalChainReady(ManuscriptReviewProcessType.PROOFREAD);
        when(fixture.serialGateway.nextSerial(ManuscriptReviewProcessType.PROOFREAD, "20260321")).thenReturn(7);
        when(fixture.userRoleGateway.hasCertifiedApplicantRole(1002L)).thenReturn(false);

        ManuscriptReviewResult result = fixture.service.createAndSubmit(
            CreateManuscriptReviewCommand.builder()
                .processType(ManuscriptReviewProcessType.PROOFREAD)
                .title("校对稿件")
                .content("正文")
                .attachmentCount(1)
                .build()
        );

        assertEquals("JD20260321007", result.getManuscriptCode());
        assertEquals("待一级审批", result.getCurrentNode());
    }

    @Test
    void shouldRejectSubmitWhenApprovalChainConfigMissing() {
        ServiceFixture fixture = new ServiceFixture(1003L);
        when(fixture.configGateway.hasCompleteApprovalChain(ManuscriptReviewProcessType.AUDIT)).thenReturn(false);

        ServiceException exception = assertThrows(ServiceException.class, () -> fixture.service.createAndSubmit(
            CreateManuscriptReviewCommand.builder()
                .processType(ManuscriptReviewProcessType.AUDIT)
                .title("缺配置稿件")
                .content("正文")
                .attachmentCount(1)
                .build()
        ));

        assertEquals("审核流程缺少完整审批链配置", exception.getMessage());
    }

    @Test
    void shouldRejectSubmitWhenApproverRoleHasNoActiveMembers() {
        ServiceFixture fixture = new ServiceFixture(1004L);
        when(fixture.configGateway.hasCompleteApprovalChain(ManuscriptReviewProcessType.AUDIT)).thenReturn(true);
        when(fixture.configGateway.hasActiveApproverMembers(ManuscriptReviewProcessType.AUDIT)).thenReturn(false);

        ServiceException exception = assertThrows(ServiceException.class, () -> fixture.service.createAndSubmit(
            CreateManuscriptReviewCommand.builder()
                .processType(ManuscriptReviewProcessType.AUDIT)
                .title("缺审批人稿件")
                .content("正文")
                .attachmentCount(1)
                .build()
        ));

        assertEquals("审核流程审批角色缺少有效成员", exception.getMessage());
    }

    @Test
    void shouldSkipFirstApprovalForCertifiedApplicant() {
        ServiceFixture fixture = new ServiceFixture(1005L);
        when(fixture.configGateway.hasCompleteApprovalChain(any())).thenReturn(true);
        when(fixture.configGateway.hasActiveApproverMembers(any())).thenReturn(true);
        when(fixture.serialGateway.nextSerial(ManuscriptReviewProcessType.AUDIT, "20260321")).thenReturn(9);
        when(fixture.userRoleGateway.hasCertifiedApplicantRole(1005L)).thenReturn(true);

        ManuscriptReviewResult result = fixture.service.createAndSubmit(
            CreateManuscriptReviewCommand.builder()
                .processType(ManuscriptReviewProcessType.AUDIT)
                .title("持证稿件")
                .content("正文")
                .attachmentCount(1)
                .build()
        );

        assertEquals("审批中", result.getFlowStatus());
        assertEquals("待二级审批", result.getCurrentNode());
        assertEquals("系统判定发起人具备持证资格，自动跳过一级审批", result.getRoutingComment());
    }

    @Test
    void shouldStartSerialFrom001WhenGatewayReturnsZero() {
        ServiceFixture fixture = new ServiceFixture(1010L);
        fixture.stubApprovalChainReady(ManuscriptReviewProcessType.AUDIT);
        when(fixture.serialGateway.nextSerial(ManuscriptReviewProcessType.AUDIT, "20260321")).thenReturn(0);
        when(fixture.userRoleGateway.hasCertifiedApplicantRole(1010L)).thenReturn(false);

        ManuscriptReviewResult result = fixture.service.createAndSubmit(
            CreateManuscriptReviewCommand.builder()
                .processType(ManuscriptReviewProcessType.AUDIT)
                .title("流水号为零稿件")
                .content("正文")
                .attachmentCount(1)
                .build()
        );

        assertEquals("SH20260321001", result.getManuscriptCode());
    }

    @Test
    void shouldRejectSubmitWhenCurrentUserMissing() {
        ServiceFixture fixture = new ServiceFixture(null);
        fixture.stubApprovalChainReady(ManuscriptReviewProcessType.AUDIT);

        ServiceException exception = assertThrows(ServiceException.class, () -> fixture.service.createAndSubmit(
            CreateManuscriptReviewCommand.builder()
                .processType(ManuscriptReviewProcessType.AUDIT)
                .title("无登录用户稿件")
                .content("正文")
                .attachmentCount(1)
                .build()
        ));

        assertEquals("当前登录用户不存在", exception.getMessage());
    }

    @Test
    void shouldNotExposeApplicantUserIdOnCommands() {
        assertThrows(NoSuchMethodException.class, () -> CreateManuscriptReviewCommand.class.getMethod("getApplicantUserId"));
        assertThrows(NoSuchMethodException.class, () -> ResubmitManuscriptReviewCommand.class.getMethod("getApplicantUserId"));
    }

    private static Clock fixedBusinessClock() {
        return Clock.fixed(
            LocalDate.of(2026, 3, 21).atStartOfDay(BUSINESS_ZONE_ID).toInstant(),
            BUSINESS_ZONE_ID
        );
    }

    private static final class ServiceFixture {

        private final ManuscriptReviewConfigGateway configGateway = mock(ManuscriptReviewConfigGateway.class);
        private final ManuscriptReviewSerialGateway serialGateway = mock(ManuscriptReviewSerialGateway.class);
        private final ManuscriptReviewUserRoleGateway userRoleGateway = mock(ManuscriptReviewUserRoleGateway.class);
        private final ManuscriptReviewResubmitGateway resubmitGateway = mock(ManuscriptReviewResubmitGateway.class);
        private final ManuscriptReviewCurrentUserGateway currentUserGateway = mock(ManuscriptReviewCurrentUserGateway.class);
        private final ManuscriptReviewService service;

        private ServiceFixture(Long currentUserId) {
            when(currentUserGateway.getCurrentUserId()).thenReturn(currentUserId);
            this.service = new ManuscriptReviewService(
                configGateway,
                serialGateway,
                userRoleGateway,
                resubmitGateway,
                currentUserGateway,
                fixedBusinessClock()
            );
        }

        private void stubApprovalChainReady(ManuscriptReviewProcessType processType) {
            when(configGateway.hasCompleteApprovalChain(processType)).thenReturn(true);
            when(configGateway.hasActiveApproverMembers(processType)).thenReturn(true);
        }
    }
}
