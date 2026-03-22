package br.com.aguideptbr.features.engagement;

public enum EngagementType {
    VIEW,
    LIKE,
    DISLIKE,
    SHARE,
    BOOKMARK,
    COMMENT,
    COMPLETE,
    PARTIAL_VIEW,
    CLICK_TO_VIEW;

    public boolean isPositiveEngagement() {
        return this == LIKE || this == SHARE || this == BOOKMARK ||
                this == COMMENT || this == COMPLETE;
    }

    public boolean isNegativeEngagement() {
        return this == DISLIKE;
    }

    public boolean isViewRelated() {
        return this == VIEW || this == COMPLETE || this == PARTIAL_VIEW || this == CLICK_TO_VIEW;
    }

    public boolean isReversible() {
        return this == LIKE || this == DISLIKE || this == BOOKMARK;
    }

    public boolean isClickAction() {
        return this == CLICK_TO_VIEW;
    }
}
