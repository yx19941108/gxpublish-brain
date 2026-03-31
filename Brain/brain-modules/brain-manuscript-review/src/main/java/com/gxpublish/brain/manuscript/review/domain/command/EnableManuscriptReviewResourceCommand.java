package com.gxpublish.brain.manuscript.review.domain.command;

public class EnableManuscriptReviewResourceCommand {

    private final Long resourceId;

    private EnableManuscriptReviewResourceCommand(Builder builder) {
        this.resourceId = builder.resourceId;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Long getResourceId() {
        return resourceId;
    }

    public static final class Builder {

        private Long resourceId;

        private Builder() {
        }

        public Builder resourceId(Long resourceId) {
            this.resourceId = resourceId;
            return this;
        }

        public EnableManuscriptReviewResourceCommand build() {
            return new EnableManuscriptReviewResourceCommand(this);
        }
    }
}
