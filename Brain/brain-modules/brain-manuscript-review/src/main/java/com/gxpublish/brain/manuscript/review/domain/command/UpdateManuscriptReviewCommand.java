package com.gxpublish.brain.manuscript.review.domain.command;

import com.gxpublish.brain.manuscript.review.domain.enums.ManuscriptReviewProcessType;

public class UpdateManuscriptReviewCommand {

    private final Long id;
    private final ManuscriptReviewProcessType processType;
    private final String externalManuscriptCode;
    private final String title;
    private final String mediaChannel;
    private final String submitDepartment;
    private final String authorName;
    private final String remark;
    private final String contentBody;

    private UpdateManuscriptReviewCommand(Builder builder) {
        this.id = builder.id;
        this.processType = builder.processType;
        this.externalManuscriptCode = builder.externalManuscriptCode;
        this.title = builder.title;
        this.mediaChannel = builder.mediaChannel;
        this.submitDepartment = builder.submitDepartment;
        this.authorName = builder.authorName;
        this.remark = builder.remark;
        this.contentBody = builder.contentBody;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Long getId() {
        return id;
    }

    public ManuscriptReviewProcessType getProcessType() {
        return processType;
    }

    public String getExternalManuscriptCode() {
        return externalManuscriptCode;
    }

    public String getTitle() {
        return title;
    }

    public String getMediaChannel() {
        return mediaChannel;
    }

    public String getSubmitDepartment() {
        return submitDepartment;
    }

    public String getAuthorName() {
        return authorName;
    }

    public String getRemark() {
        return remark;
    }

    public String getContentBody() {
        return contentBody;
    }

    public static final class Builder {

        private Long id;
        private ManuscriptReviewProcessType processType;
        private String externalManuscriptCode;
        private String title;
        private String mediaChannel;
        private String submitDepartment;
        private String authorName;
        private String remark;
        private String contentBody;

        private Builder() {
        }

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder processType(ManuscriptReviewProcessType processType) {
            this.processType = processType;
            return this;
        }

        public Builder externalManuscriptCode(String externalManuscriptCode) {
            this.externalManuscriptCode = externalManuscriptCode;
            return this;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder mediaChannel(String mediaChannel) {
            this.mediaChannel = mediaChannel;
            return this;
        }

        public Builder submitDepartment(String submitDepartment) {
            this.submitDepartment = submitDepartment;
            return this;
        }

        public Builder authorName(String authorName) {
            this.authorName = authorName;
            return this;
        }

        public Builder remark(String remark) {
            this.remark = remark;
            return this;
        }

        public Builder contentBody(String contentBody) {
            this.contentBody = contentBody;
            return this;
        }

        public UpdateManuscriptReviewCommand build() {
            return new UpdateManuscriptReviewCommand(this);
        }
    }
}
