package com.gxpublish.brain.manuscript.review.domain.command;

public class DisableManuscriptReviewVideoMarkCommand {

    private final Long markId;
    private final String disabledReason;

    private DisableManuscriptReviewVideoMarkCommand(Builder builder) {
        this.markId = builder.markId;
        this.disabledReason = builder.disabledReason;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Long getMarkId() {
        return markId;
    }

    public String getDisabledReason() {
        return disabledReason;
    }

    public static final class Builder {

        private Long markId;
        private String disabledReason;

        private Builder() {
        }

        public Builder markId(Long markId) {
            this.markId = markId;
            return this;
        }

        public Builder disabledReason(String disabledReason) {
            this.disabledReason = disabledReason;
            return this;
        }

        public DisableManuscriptReviewVideoMarkCommand build() {
            return new DisableManuscriptReviewVideoMarkCommand(this);
        }
    }
}
