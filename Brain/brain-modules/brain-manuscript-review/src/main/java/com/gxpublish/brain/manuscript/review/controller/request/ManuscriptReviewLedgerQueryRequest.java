package com.gxpublish.brain.manuscript.review.controller.request;

import lombok.Data;

@Data
public class ManuscriptReviewLedgerQueryRequest {

    private String keyword;
    private String processTypeLabel;
    private String flowStatusLabel;
    private String currentNodeLabel;
}
