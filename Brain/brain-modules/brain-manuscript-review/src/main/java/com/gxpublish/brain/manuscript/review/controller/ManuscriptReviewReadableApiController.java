package com.gxpublish.brain.manuscript.review.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.gxpublish.brain.common.core.domain.R;
import com.gxpublish.brain.manuscript.review.controller.request.ManuscriptReviewLedgerQueryRequest;
import com.gxpublish.brain.manuscript.review.controller.response.ManuscriptReviewDetailResponse;
import com.gxpublish.brain.manuscript.review.controller.response.ManuscriptReviewLedgerItemResponse;
import com.gxpublish.brain.manuscript.review.service.ManuscriptReviewReadableService;

@RestController
@RequestMapping("/manuscript-review/readable")
public class ManuscriptReviewReadableApiController {

    private final ManuscriptReviewReadableService readableService;

    public ManuscriptReviewReadableApiController(ManuscriptReviewReadableService readableService) {
        this.readableService = readableService;
    }

    @GetMapping("/ledger")
    public R<List<ManuscriptReviewLedgerItemResponse>> ledger(ManuscriptReviewLedgerQueryRequest request) {
        return R.ok(readableService.listLedger(request));
    }

    @GetMapping("/detail")
    public R<ManuscriptReviewDetailResponse> detail(@RequestParam("reviewId") Long reviewId) {
        return R.ok(readableService.getDetail(reviewId));
    }
}
