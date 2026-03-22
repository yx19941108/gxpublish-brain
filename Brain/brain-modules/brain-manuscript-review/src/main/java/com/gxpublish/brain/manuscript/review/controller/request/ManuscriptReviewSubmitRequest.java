package com.gxpublish.brain.manuscript.review.controller.request;

import java.util.List;

import com.gxpublish.brain.manuscript.review.domain.enums.ManuscriptReviewProcessType;

public class ManuscriptReviewSubmitRequest {

    private ManuscriptReviewProcessType processType;
    private String title;
    private String content;
    private int attachmentCount;
    private int externalLinkCount;
    private List<String> externalLinkUrls;

    public ManuscriptReviewProcessType getProcessType() {
        return processType;
    }

    public void setProcessType(ManuscriptReviewProcessType processType) {
        this.processType = processType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getAttachmentCount() {
        return attachmentCount;
    }

    public void setAttachmentCount(int attachmentCount) {
        this.attachmentCount = attachmentCount;
    }

    public int getExternalLinkCount() {
        return externalLinkCount;
    }

    public void setExternalLinkCount(int externalLinkCount) {
        this.externalLinkCount = externalLinkCount;
    }

    public List<String> getExternalLinkUrls() {
        return externalLinkUrls;
    }

    public void setExternalLinkUrls(List<String> externalLinkUrls) {
        this.externalLinkUrls = externalLinkUrls;
    }
}
