package com.gxpublish.brain.manuscript.review.controller.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class ManuscriptReviewDetailResponse {

    private Long reviewId;
    private SummaryCard summaryCard;
    private ManuscriptCard manuscriptCard;
    private ActionBar actionBar;
    private List<TimelineItem> timeline;
    private ResourceSection resources;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SummaryCard {

        private String flowStatusLabel;
        private String currentNodeLabel;
        private String initiatorName;
        private String updateTime;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ManuscriptCard {

        private String processTypeLabel;
        private String manuscriptCode;
        private String externalManuscriptCode;
        private String title;
        private String mediaChannelLabel;
        private String submitterDeptName;
        private String authorNames;
        private String note;
        private String content;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActionBar {

        private boolean canModify;
        private List<String> actions;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimelineItem {

        private String createTime;
        private String text;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResourceSection {

        private List<AttachmentItem> currentAttachments;
        private List<ExternalLinkItem> currentExternalLinks;
        private List<VideoItem> currentVideos;
        private List<AttachmentItem> historyAttachments;
        private List<ExternalLinkItem> historyExternalLinks;
        private List<VideoMarkerItem> historyVideoMarkers;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AttachmentItem {

        private String fileName;
        private String fileUrl;
        private Long fileSize;
        private String mimeType;
        private String disabledTime;
        private String remark;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExternalLinkItem {

        private String linkTitle;
        private String linkUrl;
        private String disabledTime;
        private String remark;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VideoItem {

        private String fileName;
        private String fileUrl;
        private Long fileSize;
        private String mimeType;
        private String duration;
        private List<VideoMarkerItem> markers;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VideoMarkerItem {

        private String startTime;
        private String endTime;
        private String markerNote;
        private String createTime;
        private String disabledTime;
    }
}
