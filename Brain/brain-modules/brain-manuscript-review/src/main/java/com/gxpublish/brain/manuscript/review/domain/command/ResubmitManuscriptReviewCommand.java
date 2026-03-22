package com.gxpublish.brain.manuscript.review.domain.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.gxpublish.brain.manuscript.review.domain.enums.ManuscriptReviewProcessType;

public class ResubmitManuscriptReviewCommand {

    private final Long reviewId;
    private final ManuscriptReviewProcessType processType;
    private final String title;
    private final String content;
    private final int attachmentCount;
    private final int externalLinkCount;
    private final List<String> externalLinkUrls;
    private final boolean returnedToInitiator;

    private ResubmitManuscriptReviewCommand(Builder builder) {
        this.reviewId = builder.reviewId;
        this.processType = builder.processType;
        this.title = builder.title;
        this.content = builder.content;
        this.attachmentCount = builder.attachmentCount;
        this.externalLinkCount = builder.externalLinkCount;
        this.externalLinkUrls = immutableCopy(builder.externalLinkUrls);
        this.returnedToInitiator = builder.returnedToInitiator;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Long getReviewId() {
        return reviewId;
    }

    public ManuscriptReviewProcessType getProcessType() {
        return processType;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public int getAttachmentCount() {
        return attachmentCount;
    }

    public int getExternalLinkCount() {
        return externalLinkCount;
    }

    public List<String> getExternalLinkUrls() {
        return externalLinkUrls;
    }

    public boolean isReturnedToInitiator() {
        return returnedToInitiator;
    }

    private static List<String> immutableCopy(List<String> source) {
        if (source == null || source.isEmpty()) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(new ArrayList<>(source));
    }

    public static final class Builder {

        private Long reviewId;
        private ManuscriptReviewProcessType processType;
        private String title;
        private String content;
        private int attachmentCount;
        private int externalLinkCount;
        private List<String> externalLinkUrls;
        private boolean returnedToInitiator;

        private Builder() {
        }

        public Builder reviewId(Long reviewId) {
            this.reviewId = reviewId;
            return this;
        }

        public Builder processType(ManuscriptReviewProcessType processType) {
            this.processType = processType;
            return this;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder content(String content) {
            this.content = content;
            return this;
        }

        public Builder attachmentCount(int attachmentCount) {
            this.attachmentCount = attachmentCount;
            return this;
        }

        public Builder externalLinkCount(int externalLinkCount) {
            this.externalLinkCount = externalLinkCount;
            return this;
        }

        public Builder externalLinkUrls(List<String> externalLinkUrls) {
            this.externalLinkUrls = externalLinkUrls;
            return this;
        }

        public Builder returnedToInitiator(boolean returnedToInitiator) {
            this.returnedToInitiator = returnedToInitiator;
            return this;
        }

        public ResubmitManuscriptReviewCommand build() {
            return new ResubmitManuscriptReviewCommand(this);
        }
    }
}
