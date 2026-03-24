package com.gxpublish.brain.manuscript.review.controller.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ManuscriptReviewSubmitResponse {

    private Long id;

    public static ManuscriptReviewSubmitResponse fromId(Long id) {
        return new ManuscriptReviewSubmitResponse(id);
    }
}
