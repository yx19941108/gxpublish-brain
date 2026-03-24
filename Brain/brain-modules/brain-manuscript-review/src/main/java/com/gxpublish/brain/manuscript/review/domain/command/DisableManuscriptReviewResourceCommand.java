package com.gxpublish.brain.manuscript.review.domain.command;

public class DisableManuscriptReviewResourceCommand {

    private final Long resourceId;
    private final String disabledReason;

    private DisableManuscriptReviewResourceCommand(Builder builder) {
        this.resourceId = builder.resourceId;
        this.disabledReason = builder.disabledReason;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Long getResourceId() {
        return resourceId;
    }

    public String getDisabledReason() {
        return disabledReason;
    }

    public static final class Builder {

        private Long resourceId;
        private String disabledReason;

        private Builder() {
        }

        public Builder resourceId(Long resourceId) {
            this.resourceId = resourceId;
            return this;
        }

        public Builder disabledReason(String disabledReason) {
            this.disabledReason = disabledReason;
            return this;
        }

        public DisableManuscriptReviewResourceCommand build() {
            return new DisableManuscriptReviewResourceCommand(this);
        }
    }
}
