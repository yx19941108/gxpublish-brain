package com.gxpublish.brain.manuscript.review.gateway.impl;

import org.springframework.stereotype.Component;

import com.gxpublish.brain.common.satoken.utils.LoginHelper;
import com.gxpublish.brain.manuscript.review.gateway.ManuscriptReviewCurrentUserGateway;

@Component
public class LoginHelperCurrentUserGateway implements ManuscriptReviewCurrentUserGateway {

    @Override
    public Long getCurrentUserId() {
        return LoginHelper.getUserId();
    }
}
