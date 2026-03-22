package com.gxpublish.brain.manuscript.review.gateway;

public interface ManuscriptReviewResubmitGateway {

    boolean isReturnedToInitiator(Long reviewId, Long applicantUserId);
}

