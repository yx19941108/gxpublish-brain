package com.gxpublish.brain.manuscript.review.domain.command;

public class AddManuscriptReviewVideoMarkCommand {

    private final Long reviewId;
    private final Long resourceId;
    private final String startTimeText;
    private final String endTimeText;
    private final String markContent;

    private AddManuscriptReviewVideoMarkCommand(Builder builder) {
        this.reviewId = builder.reviewId;
        this.resourceId = builder.resourceId;
        this.startTimeText = builder.startTimeText;
        this.endTimeText = builder.endTimeText;
        this.markContent = builder.markContent;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Long getReviewId() {
        return reviewId;
    }

    public Long getResourceId() {
        return resourceId;
    }

    public String getStartTimeText() {
        return startTimeText;
    }

    public String getEndTimeText() {
        return endTimeText;
    }

    public String getMarkContent() {
        return markContent;
    }

    public static final class Builder {

        private Long reviewId;
        private Long resourceId;
        private String startTimeText;
        private String endTimeText;
        private String markContent;

        private Builder() {
        }

        public Builder reviewId(Long reviewId) {
            this.reviewId = reviewId;
            return this;
        }

        public Builder resourceId(Long resourceId) {
            this.resourceId = resourceId;
            return this;
        }

        public Builder startTimeText(String startTimeText) {
            this.startTimeText = startTimeText;
            return this;
        }

        public Builder endTimeText(String endTimeText) {
            this.endTimeText = endTimeText;
            return this;
        }

        public Builder markContent(String markContent) {
            this.markContent = markContent;
            return this;
        }

        public AddManuscriptReviewVideoMarkCommand build() {
            return new AddManuscriptReviewVideoMarkCommand(this);
        }
    }
}
