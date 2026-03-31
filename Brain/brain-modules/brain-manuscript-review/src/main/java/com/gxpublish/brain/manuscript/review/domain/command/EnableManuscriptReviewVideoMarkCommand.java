package com.gxpublish.brain.manuscript.review.domain.command;

public class EnableManuscriptReviewVideoMarkCommand {

    private final Long markId;

    private EnableManuscriptReviewVideoMarkCommand(Builder builder) {
        this.markId = builder.markId;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Long getMarkId() {
        return markId;
    }

    public static final class Builder {

        private Long markId;

        private Builder() {
        }

        public Builder markId(Long markId) {
            this.markId = markId;
            return this;
        }

        public EnableManuscriptReviewVideoMarkCommand build() {
            return new EnableManuscriptReviewVideoMarkCommand(this);
        }
    }
}
