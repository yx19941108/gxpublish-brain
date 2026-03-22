package com.gxpublish.brain.manuscript.review.controller.response;

import com.gxpublish.brain.manuscript.review.domain.result.ManuscriptReviewResult;

public class ManuscriptReviewSubmitResponse {

    private final String manuscriptCode;
    private final String flowStatus;
    private final String currentNode;
    private final String routingComment;

    private ManuscriptReviewSubmitResponse(String manuscriptCode,
                                           String flowStatus,
                                           String currentNode,
                                           String routingComment) {
        this.manuscriptCode = manuscriptCode;
        this.flowStatus = flowStatus;
        this.currentNode = currentNode;
        this.routingComment = routingComment;
    }

    public static ManuscriptReviewSubmitResponse from(ManuscriptReviewResult result) {
        return new ManuscriptReviewSubmitResponse(
            result.getManuscriptCode(),
            result.getFlowStatus(),
            result.getCurrentNode(),
            result.getRoutingComment()
        );
    }

    public String getManuscriptCode() {
        return manuscriptCode;
    }

    public String getFlowStatus() {
        return flowStatus;
    }

    public String getCurrentNode() {
        return currentNode;
    }

    public String getRoutingComment() {
        return routingComment;
    }
}
