package br.com.aguideptbr.features.userposition.enuns;

/**
 * Enum para motivos de adição de pontos no sistema de ranking.
 *
 * Usado para auditoria e rastreamento de como os pontos foram ganhos.
 * Cada valor representa um evento específico que concede pontos ao usuário.
 *
 * @see br.com.aguideptbr.features.userposition.UserRankingAuditModel
 */
public enum PointsReason {

    // ═══════════════════════════════════════════════════════════
    // LOGIN AND ACTIVITY
    // ═══════════════════════════════════════════════════════════

    /** Daily login - First login of the day (+1 point) */
    DAILY_LOGIN("Daily login"),

    /** 7-day consecutive login streak bonus (+5 points) */
    STREAK_BONUS_7_DAYS("7-day streak bonus"),

    /** 30-day consecutive login streak bonus (+20 points) */
    STREAK_BONUS_30_DAYS("30-day streak bonus"),

    // ═══════════════════════════════════════════════════════════
    // CONTENT OWNERSHIP VERIFICATION WIZARD
    // ═══════════════════════════════════════════════════════════

    /** User entered the content verification wizard (+2 points) */
    WIZARD_ENTRY("Content verification wizard entry"),

    /** Completed Step 1 of verification (title + URL) (+3 points) */
    WIZARD_STEP_1("Verification Step 1 completed"),

    /** Completed Step 2 of verification (proof of ownership) (+5 points) */
    WIZARD_STEP_2("Verification Step 2 completed"),

    /** Submitted complete verification form (+10 points) */
    WIZARD_SUBMISSION("Verification form submitted"),

    /** Content ownership verification approved by admin (+25 points) */
    VERIFICATION_APPROVED("Verification approved"),

    // ═══════════════════════════════════════════════════════════
    // PROFILE AND ONBOARDING
    // ═══════════════════════════════════════════════════════════

    /** Completed onboarding flow (all 3 promotional phases) (+5 points) */
    ONBOARDING_COMPLETED("Onboarding completed"),

    /** Profile 50% complete (+3 points) */
    PROFILE_50_PERCENT("Profile 50% complete"),

    /** Profile 100% complete (+10 points) */
    PROFILE_100_PERCENT("Profile 100% complete"),

    /** User classified their profile type (Creator/Consumer) (+5 points) */
    PROFILE_CLASSIFICATION("Profile type classified"),

    // ═══════════════════════════════════════════════════════════
    // CONTENT ENGAGEMENT MILESTONES
    // ═══════════════════════════════════════════════════════════

    /** Viewed 10 content items (+2 points) */
    CONTENT_VIEWS_10("10 content views milestone"),

    /** Viewed 50 content items (+10 points) */
    CONTENT_VIEWS_50("50 content views milestone"),

    /** Viewed 100 content items (+25 points) */
    CONTENT_VIEWS_100("100 content views milestone"),

    // ═══════════════════════════════════════════════════════════
    // SOCIAL (FUTURE FEATURES)
    // ═══════════════════════════════════════════════════════════

    /** First connection with another user (+3 points) */
    FIRST_CONNECTION("First connection"),

    /** Reached 10 connections (+5 points) */
    CONNECTIONS_10("10 connections milestone"),

    // ═══════════════════════════════════════════════════════════
    // MANUAL/UNSPECIFIED
    // ═══════════════════════════════════════════════════════════

    /** Points added manually by admin or no specific reason provided */
    UNSPECIFIED("Unspecified reason");

    private final String description;

    PointsReason(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Converte uma string para o enum correspondente.
     * Ignora case e espaços. Retorna UNSPECIFIED se não encontrar match.
     *
     * @param value String a ser convertida
     * @return PointsReason correspondente ou UNSPECIFIED
     */
    public static PointsReason fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return UNSPECIFIED;
        }

        String normalized = value.trim().toUpperCase();
        try {
            return PointsReason.valueOf(normalized);
        } catch (IllegalArgumentException e) {
            return UNSPECIFIED;
        }
    }
}
