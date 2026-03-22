package com.gxpublish.brain.manuscript.review.domain.policy;

import java.util.Set;

import com.gxpublish.brain.manuscript.review.domain.enums.ManuscriptReviewDetailAction;

public class ManuscriptReviewDetailPermissionResult {

    private final boolean canModify;
    private final Set<ManuscriptReviewDetailAction> allowedActions;
    private final boolean supportsModifyAndSubmitShortcut;
    private final boolean supportsModifyAndApproveShortcut;

    private ManuscriptReviewDetailPermissionResult(Builder builder) {
        this.canModify = builder.canModify;
        this.allowedActions = Set.copyOf(builder.allowedActions);
        this.supportsModifyAndSubmitShortcut = builder.supportsModifyAndSubmitShortcut;
        this.supportsModifyAndApproveShortcut = builder.supportsModifyAndApproveShortcut;
    }

    public static Builder builder() {
        return new Builder();
    }

    public boolean canModify() {
        return canModify;
    }

    public Set<ManuscriptReviewDetailAction> getAllowedActions() {
        return allowedActions;
    }

    public boolean supportsModifyAndSubmitShortcut() {
        return supportsModifyAndSubmitShortcut;
    }

    public boolean supportsModifyAndApproveShortcut() {
        return supportsModifyAndApproveShortcut;
    }

    public static final class Builder {

        private boolean canModify;
        private Set<ManuscriptReviewDetailAction> allowedActions;
        private boolean supportsModifyAndSubmitShortcut;
        private boolean supportsModifyAndApproveShortcut;

        private Builder() {
        }

        public Builder canModify(boolean canModify) {
            this.canModify = canModify;
            return this;
        }

        public Builder allowedActions(Set<ManuscriptReviewDetailAction> allowedActions) {
            this.allowedActions = allowedActions;
            return this;
        }

        public Builder supportsModifyAndSubmitShortcut(boolean supportsModifyAndSubmitShortcut) {
            this.supportsModifyAndSubmitShortcut = supportsModifyAndSubmitShortcut;
            return this;
        }

        public Builder supportsModifyAndApproveShortcut(boolean supportsModifyAndApproveShortcut) {
            this.supportsModifyAndApproveShortcut = supportsModifyAndApproveShortcut;
            return this;
        }

        public ManuscriptReviewDetailPermissionResult build() {
            return new ManuscriptReviewDetailPermissionResult(this);
        }
    }
}
