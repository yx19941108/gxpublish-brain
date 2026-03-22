package com.gxpublish.brain.manuscript.review.domain.policy;

import java.util.Collections;
import java.util.Set;

public class ManuscriptReviewDetailPermissionContext {

    private final Long currentUserId;
    private final Long initiatorUserId;
    private final Set<Long> currentApproverUserIds;
    private final Set<Long> historyParticipantUserIds;
    private final boolean returnedToInitiator;

    private ManuscriptReviewDetailPermissionContext(Builder builder) {
        this.currentUserId = builder.currentUserId;
        this.initiatorUserId = builder.initiatorUserId;
        this.currentApproverUserIds = builder.currentApproverUserIds == null
            ? Collections.emptySet()
            : Set.copyOf(builder.currentApproverUserIds);
        this.historyParticipantUserIds = builder.historyParticipantUserIds == null
            ? Collections.emptySet()
            : Set.copyOf(builder.historyParticipantUserIds);
        this.returnedToInitiator = builder.returnedToInitiator;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Long getCurrentUserId() {
        return currentUserId;
    }

    public Long getInitiatorUserId() {
        return initiatorUserId;
    }

    public Set<Long> getCurrentApproverUserIds() {
        return currentApproverUserIds;
    }

    public Set<Long> getHistoryParticipantUserIds() {
        return historyParticipantUserIds;
    }

    public boolean isReturnedToInitiator() {
        return returnedToInitiator;
    }

    public static final class Builder {

        private Long currentUserId;
        private Long initiatorUserId;
        private Set<Long> currentApproverUserIds;
        private Set<Long> historyParticipantUserIds;
        private boolean returnedToInitiator;

        private Builder() {
        }

        public Builder currentUserId(Long currentUserId) {
            this.currentUserId = currentUserId;
            return this;
        }

        public Builder initiatorUserId(Long initiatorUserId) {
            this.initiatorUserId = initiatorUserId;
            return this;
        }

        public Builder currentApproverUserIds(Set<Long> currentApproverUserIds) {
            this.currentApproverUserIds = currentApproverUserIds;
            return this;
        }

        public Builder historyParticipantUserIds(Set<Long> historyParticipantUserIds) {
            this.historyParticipantUserIds = historyParticipantUserIds;
            return this;
        }

        public Builder returnedToInitiator(boolean returnedToInitiator) {
            this.returnedToInitiator = returnedToInitiator;
            return this;
        }

        public ManuscriptReviewDetailPermissionContext build() {
            return new ManuscriptReviewDetailPermissionContext(this);
        }
    }
}
