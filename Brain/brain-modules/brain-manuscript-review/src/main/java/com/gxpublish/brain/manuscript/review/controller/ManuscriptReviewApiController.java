package com.gxpublish.brain.manuscript.review.controller;

import org.springframework.validation.annotation.Validated;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gxpublish.brain.common.core.domain.R;
import com.gxpublish.brain.manuscript.review.controller.request.ManuscriptReviewSubmitRequest;
import com.gxpublish.brain.manuscript.review.controller.response.ManuscriptReviewSubmitResponse;
import com.gxpublish.brain.manuscript.review.domain.command.CreateManuscriptReviewCommand;
import com.gxpublish.brain.manuscript.review.service.ManuscriptReviewService;

@Validated
@RestController
@ConditionalOnBean(ManuscriptReviewService.class)
@RequestMapping("/manuscript-review")
public class ManuscriptReviewApiController {

    private final ManuscriptReviewService manuscriptReviewService;

    public ManuscriptReviewApiController(ManuscriptReviewService manuscriptReviewService) {
        this.manuscriptReviewService = manuscriptReviewService;
    }

    @PostMapping("/submit")
    public R<ManuscriptReviewSubmitResponse> submit(@RequestBody ManuscriptReviewSubmitRequest request) {
        return R.ok(ManuscriptReviewSubmitResponse.from(
            manuscriptReviewService.createAndSubmit(
                CreateManuscriptReviewCommand.builder()
                    .processType(request.getProcessType())
                    .title(request.getTitle())
                    .content(request.getContent())
                    .attachmentCount(request.getAttachmentCount())
                    .externalLinkCount(request.getExternalLinkCount())
                    .externalLinkUrls(request.getExternalLinkUrls())
                    .build()
            )
        ));
    }
}
