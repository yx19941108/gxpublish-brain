package com.gxpublish.brain.manuscript.review.gateway.impl;

import org.springframework.stereotype.Component;

import com.gxpublish.brain.common.satoken.utils.LoginHelper;
import com.gxpublish.brain.common.core.domain.model.LoginUser;
import com.gxpublish.brain.manuscript.review.gateway.ManuscriptReviewCurrentUserGateway;

@Component
public class LoginHelperCurrentUserGateway implements ManuscriptReviewCurrentUserGateway {

    @Override
    public Long getCurrentUserId() {
        return LoginHelper.getUserId();
    }

    @Override
    public String getCurrentTenantId() {
        return LoginHelper.getTenantId();
    }

    @Override
    public Long getCurrentDeptId() {
        return LoginHelper.getDeptId();
    }

    @Override
    public String getCurrentUsername() {
        LoginUser loginUser = LoginHelper.getLoginUser();
        if (loginUser != null && loginUser.getNickname() != null && !loginUser.getNickname().trim().isEmpty()) {
            return loginUser.getNickname().trim();
        }
        return LoginHelper.getUsername();
    }
}
