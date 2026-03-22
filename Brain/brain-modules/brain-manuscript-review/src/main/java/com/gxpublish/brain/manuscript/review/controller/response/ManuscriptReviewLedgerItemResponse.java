package com.gxpublish.brain.manuscript.review.controller.response;

import lombok.Data;

@Data
public class ManuscriptReviewLedgerItemResponse {

    private Long reviewId;
    private String manuscriptCode;
    private String title;
    private String processTypeLabel;
    private String mediaChannelLabel;
    private String flowStatusLabel;
    private String currentNodeLabel;
    private String initiatorName;
    private String updateTime;
}
