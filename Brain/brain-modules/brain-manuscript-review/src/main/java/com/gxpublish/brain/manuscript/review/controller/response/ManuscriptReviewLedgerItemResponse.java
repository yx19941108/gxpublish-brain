package com.gxpublish.brain.manuscript.review.controller.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ManuscriptReviewLedgerItemResponse {

    private Long id;
    private String processType;
    private String processTypeLabel;
    private String manuscriptCode;
    private String title;
    private String mediaChannel;
    private String businessStatus;
    private String businessStatusLabel;
    private String currentNodeCode;
    private String currentNodeLabel;
    private String initiatorName;
    private String updateTime;
}
