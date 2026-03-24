package com.gxpublish.brain.manuscript.review.controller;

import java.util.List;

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
}
