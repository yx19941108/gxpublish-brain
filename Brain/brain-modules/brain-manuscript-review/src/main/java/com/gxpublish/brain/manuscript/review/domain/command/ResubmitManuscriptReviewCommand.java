package com.gxpublish.brain.manuscript.review.domain.command;

public class ResubmitManuscriptReviewCommand {

    private final Long reviewId;

    private ResubmitManuscriptReviewCommand(Builder builder) {
        this.reviewId = builder.reviewId;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Long getReviewId() {
        return reviewId;
    }

    public static final class Builder {

        private Long reviewId;

        private Builder() {
        }

        public Builder reviewId(Long reviewId) {
            this.reviewId = reviewId;
            return this;
        }

        public ResubmitManuscriptReviewCommand build() {
            return new ResubmitManuscriptReviewCommand(this);
        }
    }
}
