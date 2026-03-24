package com.gxpublish.brain.manuscript.review.domain.command;

public class AddManuscriptReviewResourceCommand {

    private final Long reviewId;
    private final String resourceType;
    private final String displayName;
    private final Long ossId;
    private final String externalUrl;

    private AddManuscriptReviewResourceCommand(Builder builder) {
        this.reviewId = builder.reviewId;
        this.resourceType = builder.resourceType;
        this.displayName = builder.displayName;
        this.ossId = builder.ossId;
        this.externalUrl = builder.externalUrl;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Long getReviewId() {
        return reviewId;
    }

    public String getResourceType() {
        return resourceType;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Long getOssId() {
        return ossId;
    }

    public String getExternalUrl() {
        return externalUrl;
    }

    public static final class Builder {

        private Long reviewId;
        private String resourceType;
        private String displayName;
        private Long ossId;
        private String externalUrl;

        private Builder() {
        }

        public Builder reviewId(Long reviewId) {
            this.reviewId = reviewId;
            return this;
        }

        public Builder resourceType(String resourceType) {
            this.resourceType = resourceType;
            return this;
        }

        public Builder displayName(String displayName) {
            this.displayName = displayName;
            return this;
        }

        public Builder ossId(Long ossId) {
            this.ossId = ossId;
            return this;
        }

        public Builder externalUrl(String externalUrl) {
            this.externalUrl = externalUrl;
            return this;
        }

        public AddManuscriptReviewResourceCommand build() {
            return new AddManuscriptReviewResourceCommand(this);
        }
    }
}
