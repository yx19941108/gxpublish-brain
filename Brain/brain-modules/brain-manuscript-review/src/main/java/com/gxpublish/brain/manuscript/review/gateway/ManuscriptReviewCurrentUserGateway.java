package com.gxpublish.brain.manuscript.review.gateway;

public interface ManuscriptReviewCurrentUserGateway {

    Long getCurrentUserId();

    String getCurrentTenantId();

    Long getCurrentDeptId();

    String getCurrentUsername();
}
