package br.com.aguideptbr.features.engagement;

public enum EngagementStatus {
    ACTIVE,
    REMOVED,
    EXPIRED,
    FLAGGED;

    public boolean isActive() {
        return this == ACTIVE;
    }

    public boolean isValidForStatistics() {
        return this == ACTIVE || this == REMOVED;
    }

    public boolean shouldBeHidden() {
        return this == EXPIRED || this == FLAGGED;
    }

    public boolean canBeReactivated() {
        return this == REMOVED;
    }
}
