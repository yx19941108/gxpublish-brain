package com.gxpublish.brain.manuscript.review.controller.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ManuscriptReviewDetailResponse {

    private Long id;
    private String processType;
    private String processTypeLabel;
    private String manuscriptCode;
    private String externalManuscriptCode;
    private String title;
    private String mediaChannel;
    private String submitDepartment;
    private String authorName;
    private String remark;
    private String contentBody;
    private String contentSummary;
    private String businessStatus;
    private String businessStatusLabel;
    private String currentNodeCode;
    private String currentNodeStatus;
    private String currentNodeLabel;
    private String initiatorName;
    private String firstSubmitTime;
    private String latestSubmitTime;
    private String updateTime;
    private List<ResourceItemVO> attachmentList;
    private List<ResourceItemVO> externalLinkList;
    private List<ResourceItemVO> videoList;
    private List<VideoMarkItemVO> videoMarkList;
    private List<TimelineItemVO> timelineItems;
    private PermissionMatrixVO permissionMatrix;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class PermissionMatrixVO {

        private boolean isInitiator;
        private boolean isCurrentApprover;
        private boolean isHistoryParticipant;
        private boolean canView;
        private boolean canEdit;
        private boolean canResubmit;
        private boolean canCancel;
        private boolean canGotoApproval;
        private String buttonReason;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class TimelineItemVO {

        private String eventTime;
        private String eventType;
        private String eventTypeLabel;
        private String eventCode;
        private String eventText;
        private String operatorName;
        private String relatedNode;
        private String relatedResourceName;
        private String statusLabel;
        private String diffSummary;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ResourceItemVO {

        private Long id;
        private String resourceType;
        private String resourceTypeLabel;
        private String displayName;
        private String externalUrl;
        private String createdTime;
        private String resourceUrl;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class VideoMarkItemVO {

        private Long id;
        private String startTimeText;
        private String endTimeText;
        private String markContent;
    }
}
