package com.gxpublish.brain.manuscript.review.controller.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ManuscriptReviewLedgerQueryRequest {

    private String keyword;
    private String processType;
    private String mediaChannel;
    private String businessStatus;
    private String currentNodeCode;
    private String startTimeFrom;
    private String startTimeTo;
    private Integer pageNum;
    private Integer pageSize;

    @JsonIgnore
    public String getProcessTypeLabel() {
        return processType;
    }

    public void setProcessTypeLabel(String processTypeLabel) {
        this.processType = processTypeLabel;
    }

    @JsonIgnore
    public String getFlowStatusLabel() {
        return businessStatus;
    }

    public void setFlowStatusLabel(String flowStatusLabel) {
        this.businessStatus = flowStatusLabel;
    }

    @JsonIgnore
    public String getCurrentNodeLabel() {
        return currentNodeCode;
    }

    public void setCurrentNodeLabel(String currentNodeLabel) {
        this.currentNodeCode = currentNodeLabel;
    }
}
