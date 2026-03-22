package com.gxpublish.brain.manuscript.review.domain.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.gxpublish.brain.manuscript.review.domain.enums.ManuscriptReviewProcessType;

public class CreateManuscriptReviewCommand {

    private final ManuscriptReviewProcessType processType;
    private final String title;
    private final String content;
    private final int attachmentCount;
    private final int externalLinkCount;
    private final List<String> externalLinkUrls;

    private CreateManuscriptReviewCommand(Builder builder) {
        this.processType = builder.processType;
        this.title = builder.title;
        this.content = builder.content;
        this.attachmentCount = builder.attachmentCount;
        this.externalLinkCount = builder.externalLinkCount;
        this.externalLinkUrls = immutableCopy(builder.externalLinkUrls);
    }

    public static Builder builder() {
        return new Builder();
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

    private static List<String> immutableCopy(List<String> source) {
        if (source == null || source.isEmpty()) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(new ArrayList<>(source));
    }

    public static final class Builder {

        private ManuscriptReviewProcessType processType;
        private String title;
        private String content;
        private int attachmentCount;
        private int externalLinkCount;
        private List<String> externalLinkUrls;

        private Builder() {
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

        public CreateManuscriptReviewCommand build() {
            return new CreateManuscriptReviewCommand(this);
        }
    }
}
