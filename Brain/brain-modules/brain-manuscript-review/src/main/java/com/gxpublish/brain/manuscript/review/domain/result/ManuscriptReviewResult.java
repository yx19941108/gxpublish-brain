package com.gxpublish.brain.manuscript.review.domain.result;

public class ManuscriptReviewResult {

    private final String manuscriptCode;
    private final String flowStatus;
    private final String currentNode;
    private final String routingComment;

    private ManuscriptReviewResult(Builder builder) {
        this.manuscriptCode = builder.manuscriptCode;
        this.flowStatus = builder.flowStatus;
        this.currentNode = builder.currentNode;
        this.routingComment = builder.routingComment;
    }

    public static Builder builder() {
        return new Builder();
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

    public static final class Builder {

        private String manuscriptCode;
        private String flowStatus;
        private String currentNode;
        private String routingComment;

        private Builder() {
        }

        public Builder manuscriptCode(String manuscriptCode) {
            this.manuscriptCode = manuscriptCode;
            return this;
        }

        public Builder flowStatus(String flowStatus) {
            this.flowStatus = flowStatus;
            return this;
        }

        public Builder currentNode(String currentNode) {
            this.currentNode = currentNode;
            return this;
        }

        public Builder routingComment(String routingComment) {
            this.routingComment = routingComment;
            return this;
        }

        public ManuscriptReviewResult build() {
            return new ManuscriptReviewResult(this);
        }
    }
}
