package com.gxpublish.brain.manuscript.review.gateway;

import com.gxpublish.brain.manuscript.review.domain.enums.ManuscriptReviewProcessType;

public interface ManuscriptReviewSerialGateway {

    int nextSerial(ManuscriptReviewProcessType processType, String businessDate);
}
