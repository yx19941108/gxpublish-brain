package com.gxpublish.brain.manuscript.review.domain.enums;

public enum ManuscriptReviewProcessType {

    AUDIT("审核流程", "SH"),
    PROOFREAD("校对流程", "JD");

    private final String displayName;
    private final String manuscriptCodePrefix;

    ManuscriptReviewProcessType(String displayName, String manuscriptCodePrefix) {
        this.displayName = displayName;
        this.manuscriptCodePrefix = manuscriptCodePrefix;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getManuscriptCodePrefix() {
        return manuscriptCodePrefix;
    }
}
