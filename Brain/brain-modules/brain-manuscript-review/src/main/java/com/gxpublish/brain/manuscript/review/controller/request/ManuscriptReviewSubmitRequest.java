package com.gxpublish.brain.manuscript.review.controller.request;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ManuscriptReviewSubmitRequest {

    private Long id;
    private String processType;
    private String externalManuscriptCode;
    private String title;
    private String mediaChannel;
    private String submitDepartment;
    private String authorName;
    private String remark;
    private String contentBody;
}
