package com.gxpublish.brain.manuscript.review.gateway;

import com.gxpublish.brain.manuscript.review.domain.enums.ManuscriptReviewProcessType;

public interface ManuscriptReviewConfigGateway {

    boolean hasCompleteApprovalChain(ManuscriptReviewProcessType processType);

    boolean hasActiveApproverMembers(ManuscriptReviewProcessType processType);
}
