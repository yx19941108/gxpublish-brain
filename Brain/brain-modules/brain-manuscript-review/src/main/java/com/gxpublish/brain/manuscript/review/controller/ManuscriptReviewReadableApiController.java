package com.gxpublish.brain.manuscript.review.controller;

import java.util.List;

import cn.dev33.satoken.annotation.SaIgnore;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gxpublish.brain.common.core.domain.R;
import com.gxpublish.brain.common.mybatis.core.page.TableDataInfo;
import com.gxpublish.brain.manuscript.review.controller.request.ManuscriptReviewLedgerQueryRequest;
import com.gxpublish.brain.manuscript.review.controller.response.ManuscriptReviewDetailResponse;
import com.gxpublish.brain.manuscript.review.controller.response.ManuscriptReviewLedgerItemResponse;
import com.gxpublish.brain.manuscript.review.controller.response.ManuscriptReviewPreviewTicketResponse;
import com.gxpublish.brain.manuscript.review.service.ManuscriptReviewReadableService;

@RestController
@ConditionalOnBean(ManuscriptReviewReadableService.class)
@RequestMapping("/workflow/manuscript-review")
public class ManuscriptReviewReadableApiController {

    private final ManuscriptReviewReadableService readableService;

    public ManuscriptReviewReadableApiController(ManuscriptReviewReadableService readableService) {
        this.readableService = readableService;
    }

    @GetMapping("/list")
    public TableDataInfo<ManuscriptReviewLedgerItemResponse> list(ManuscriptReviewLedgerQueryRequest request) {
        return readableService.listLedger(request);
    }

    @GetMapping("/{id}")
    public R<ManuscriptReviewDetailResponse> detail(@PathVariable("id") Long id) {
        return R.ok(readableService.getDetail(id));
    }

    @GetMapping("/resource/preview-ticket/{resourceId}")
    public R<ManuscriptReviewPreviewTicketResponse> issuePreviewTicket(@PathVariable("resourceId") Long resourceId) {
        return R.ok(readableService.issuePreviewTicket(resourceId));
    }

    @SaIgnore
    @GetMapping("/resource/preview/{resourceId}")
    public void previewResource(@PathVariable("resourceId") Long resourceId,
                                HttpServletRequest request,
                                HttpServletResponse response) throws java.io.IOException {
        readableService.previewResource(resourceId, request, response);
    }
}
