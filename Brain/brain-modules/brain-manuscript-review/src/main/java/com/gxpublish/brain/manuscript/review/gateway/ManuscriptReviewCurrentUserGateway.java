package com.gxpublish.brain.manuscript.review.gateway;

public interface ManuscriptReviewCurrentUserGateway {

    Long getCurrentUserId();

    String getCurrentUserType();

    String getCurrentClientId();

    String getCurrentTenantId();

    Long getCurrentDeptId();

    String getCurrentUsername();
}
