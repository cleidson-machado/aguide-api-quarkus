-- V1.0.22__Add_profile_completion_to_ranking.sql
-- Migration to add profile completion percentage tracking to user ranking
-- Date: 2026-04-04
-- Purpose: Support Phase B telemetry from Flutter frontend - profile wizard completion tracking

-- ══════════════════════════════════════════════════════════════
-- ADD COLUMN: profile_completion_percentage
-- PURPOSE: Track user's profile completion progress (0-100%)
-- BUSINESS RULES:
--   - Default value: 0 (profile not started)
--   - Range: 0-100 (percentage)
--   - Updated by Flutter when user progresses through profile wizard
--   - Used for engagement tracking and onboarding completion metrics
-- ══════════════════════════════════════════════════════════════

ALTER TABLE app_user_ranking
ADD COLUMN profile_completion_percentage INTEGER DEFAULT 0
    CHECK (profile_completion_percentage >= 0 AND profile_completion_percentage <= 100);

-- ══════════════════════════════════════════════════════════════
-- INDEX: Optimize queries for incomplete profiles
-- PURPOSE: Efficient filtering of users who haven't completed their profile
-- USE CASE: Marketing campaigns targeting users with incomplete profiles
-- ══════════════════════════════════════════════════════════════

CREATE INDEX idx_ranking_profile_completion ON app_user_ranking(profile_completion_percentage)
    WHERE deleted_at IS NULL;

-- ══════════════════════════════════════════════════════════════
-- DOCUMENTATION
-- ══════════════════════════════════════════════════════════════

COMMENT ON COLUMN app_user_ranking.profile_completion_percentage IS 'Percentage of profile completion (0-100): tracks wizard progress for onboarding metrics';

-- ══════════════════════════════════════════════════════════════
-- POINTS SYSTEM INTEGRATION (Phase B)
-- ══════════════════════════════════════════════════════════════
-- Points awarded for profile completion milestones:
--   - 50% complete: +3 points (via /add-points with reason "PROFILE_50_PERCENT")
--   - 100% complete: +10 points (via /add-points with reason "PROFILE_100_PERCENT")
-- Note: Points are awarded by Flutter app when thresholds are crossed
