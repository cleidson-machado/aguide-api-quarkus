-- V1.0.20__Create_user_ranking_table.sql
-- Migration to create the app_user_ranking table for gamification and engagement tracking
-- Date: 2026-03-24
-- Author: Cleidson Machado

-- ══════════════════════════════════════════════════════════════
-- TABLE: app_user_ranking
-- PURPOSE: Store user ranking, engagement metrics, and conversion potential
-- RELATIONSHIPS:
--   - user_id → app_user.id (OneToOne)
-- BUSINESS RULES:
--   - Each user can have only ONE ranking record
--   - Engagement Level calculated based on totalScore and streak
--   - Conversion Potential based on engagement + contact availability
-- ══════════════════════════════════════════════════════════════

CREATE TABLE IF NOT EXISTS app_user_ranking (
    -- Primary Key
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    -- Foreign Key (OneToOne relationship with app_user)
    user_id UUID NOT NULL UNIQUE,

    -- ══════════════════════════════════════════════════════════════
    -- SCORING AND CLASSIFICATION
    -- ══════════════════════════════════════════════════════════════
    total_score INT NOT NULL DEFAULT 0 CHECK (total_score >= 0),

    engagement_level VARCHAR(20) NOT NULL DEFAULT 'LOW'
        CHECK (engagement_level IN ('LOW', 'MEDIUM', 'HIGH', 'VERY_HIGH')),

    conversion_potential VARCHAR(20) NOT NULL DEFAULT 'VERY_LOW'
        CHECK (conversion_potential IN ('VERY_LOW', 'LOW', 'MEDIUM', 'HIGH', 'VERY_HIGH')),

    -- ══════════════════════════════════════════════════════════════
    -- CONTENT CONSUMPTION METRICS
    -- ══════════════════════════════════════════════════════════════
    total_content_views BIGINT NOT NULL DEFAULT 0 CHECK (total_content_views >= 0),
    unique_content_views BIGINT NOT NULL DEFAULT 0 CHECK (unique_content_views >= 0),

    -- ══════════════════════════════════════════════════════════════
    -- USAGE FREQUENCY METRICS
    -- ══════════════════════════════════════════════════════════════
    avg_daily_usage_minutes INT NOT NULL DEFAULT 0 CHECK (avg_daily_usage_minutes >= 0),
    consecutive_days_streak INT NOT NULL DEFAULT 0 CHECK (consecutive_days_streak >= 0),
    total_active_days BIGINT NOT NULL DEFAULT 0 CHECK (total_active_days >= 0),

    -- ══════════════════════════════════════════════════════════════
    -- INTERACTION METRICS
    -- ══════════════════════════════════════════════════════════════
    total_messages_sent BIGINT NOT NULL DEFAULT 0 CHECK (total_messages_sent >= 0),
    total_conversations_started INT NOT NULL DEFAULT 0 CHECK (total_conversations_started >= 0),
    unique_contacts_messaged INT NOT NULL DEFAULT 0 CHECK (unique_contacts_messaged >= 0),
    active_conversations INT NOT NULL DEFAULT 0 CHECK (active_conversations >= 0),

    -- ══════════════════════════════════════════════════════════════
    -- CONTACT AVAILABILITY (Conversion Indicators)
    -- ══════════════════════════════════════════════════════════════
    has_phones BOOLEAN NOT NULL DEFAULT false,
    total_phones INT NOT NULL DEFAULT 0 CHECK (total_phones >= 0),
    has_whatsapp BOOLEAN NOT NULL DEFAULT false,
    has_telegram BOOLEAN NOT NULL DEFAULT false,

    -- ══════════════════════════════════════════════════════════════
    -- ACTIVITY TIMESTAMPS
    -- ══════════════════════════════════════════════════════════════
    last_activity_at TIMESTAMP,
    last_content_view_at TIMESTAMP,
    last_message_sent_at TIMESTAMP,
    last_login_at TIMESTAMP,

    -- ══════════════════════════════════════════════════════════════
    -- PREFERENCES (derived from engagement patterns)
    -- ══════════════════════════════════════════════════════════════
    favorite_category VARCHAR(100),
    favorite_content_type VARCHAR(50),
    preferred_usage_time VARCHAR(20),

    -- ══════════════════════════════════════════════════════════════
    -- AUDIT TIMESTAMPS
    -- ══════════════════════════════════════════════════════════════
    score_updated_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    deleted_at TIMESTAMP, -- Soft delete

    -- Foreign Key Constraint
    CONSTRAINT fk_ranking_user FOREIGN KEY (user_id) REFERENCES app_user(id) ON DELETE CASCADE
);

-- ══════════════════════════════════════════════════════════════
-- INDEXES for Performance Optimization
-- ══════════════════════════════════════════════════════════════

-- Unique index on user_id (enforces OneToOne relationship)
CREATE UNIQUE INDEX idx_ranking_user_id ON app_user_ranking(user_id) WHERE deleted_at IS NULL;

-- Index for ranking/leaderboard queries (top users by score)
CREATE INDEX idx_ranking_total_score ON app_user_ranking(total_score DESC) WHERE deleted_at IS NULL;

-- Index for filtering by engagement level
CREATE INDEX idx_ranking_engagement_level ON app_user_ranking(engagement_level) WHERE deleted_at IS NULL;

-- Index for filtering by conversion potential
CREATE INDEX idx_ranking_conversion_potential ON app_user_ranking(conversion_potential) WHERE deleted_at IS NULL;

-- Composite index for high-value users (high engagement + high conversion)
CREATE INDEX idx_ranking_high_value_users ON app_user_ranking(engagement_level, conversion_potential, total_score DESC)
    WHERE deleted_at IS NULL AND engagement_level IN ('HIGH', 'VERY_HIGH');

-- Index for active users (recent activity)
CREATE INDEX idx_ranking_last_activity ON app_user_ranking(last_activity_at DESC) WHERE deleted_at IS NULL;

-- Index for soft delete queries
CREATE INDEX idx_ranking_deleted_at ON app_user_ranking(deleted_at) WHERE deleted_at IS NOT NULL;

-- ══════════════════════════════════════════════════════════════
-- COMMENTS for Documentation
-- ══════════════════════════════════════════════════════════════

COMMENT ON TABLE app_user_ranking IS 'Stores user ranking, engagement metrics, and conversion potential for gamification';

COMMENT ON COLUMN app_user_ranking.id IS 'Primary key (UUID)';
COMMENT ON COLUMN app_user_ranking.user_id IS 'Foreign key to app_user table (OneToOne relationship, unique)';
COMMENT ON COLUMN app_user_ranking.total_score IS 'Cumulative engagement score (points system)';
COMMENT ON COLUMN app_user_ranking.engagement_level IS 'Calculated engagement level: LOW (0-100), MEDIUM (100-500), HIGH (500-1000), VERY_HIGH (1000+)';
COMMENT ON COLUMN app_user_ranking.conversion_potential IS 'Likelihood of conversion: VERY_LOW, LOW, MEDIUM, HIGH, VERY_HIGH';
COMMENT ON COLUMN app_user_ranking.total_content_views IS 'Total number of content items viewed';
COMMENT ON COLUMN app_user_ranking.unique_content_views IS 'Number of unique content items viewed';
COMMENT ON COLUMN app_user_ranking.avg_daily_usage_minutes IS 'Average daily usage in minutes';
COMMENT ON COLUMN app_user_ranking.consecutive_days_streak IS 'Current streak of consecutive active days';
COMMENT ON COLUMN app_user_ranking.total_active_days IS 'Total number of days user has been active';
COMMENT ON COLUMN app_user_ranking.total_messages_sent IS 'Total messages sent by user';
COMMENT ON COLUMN app_user_ranking.total_conversations_started IS 'Number of conversations initiated by user';
COMMENT ON COLUMN app_user_ranking.unique_contacts_messaged IS 'Number of unique contacts the user has messaged';
COMMENT ON COLUMN app_user_ranking.active_conversations IS 'Number of currently active conversations';
COMMENT ON COLUMN app_user_ranking.has_phones IS 'Indicates if user has phone numbers registered';
COMMENT ON COLUMN app_user_ranking.total_phones IS 'Total number of phone numbers registered';
COMMENT ON COLUMN app_user_ranking.has_whatsapp IS 'Indicates if user has WhatsApp enabled';
COMMENT ON COLUMN app_user_ranking.has_telegram IS 'Indicates if user has Telegram enabled';
COMMENT ON COLUMN app_user_ranking.last_activity_at IS 'Timestamp of last recorded activity';
COMMENT ON COLUMN app_user_ranking.last_content_view_at IS 'Timestamp of last content view';
COMMENT ON COLUMN app_user_ranking.last_message_sent_at IS 'Timestamp of last message sent';
COMMENT ON COLUMN app_user_ranking.last_login_at IS 'Timestamp of last login';
COMMENT ON COLUMN app_user_ranking.favorite_category IS 'User''s most viewed content category';
COMMENT ON COLUMN app_user_ranking.favorite_content_type IS 'User''s preferred content type';
COMMENT ON COLUMN app_user_ranking.preferred_usage_time IS 'Time period when user is most active';
COMMENT ON COLUMN app_user_ranking.score_updated_at IS 'Timestamp of last score update';
COMMENT ON COLUMN app_user_ranking.created_at IS 'Timestamp when record was created';
COMMENT ON COLUMN app_user_ranking.updated_at IS 'Timestamp of last update';
COMMENT ON COLUMN app_user_ranking.deleted_at IS 'Timestamp of soft delete (NULL = active)';

-- ══════════════════════════════════════════════════════════════
-- BUSINESS RULES DOCUMENTATION
-- ══════════════════════════════════════════════════════════════

-- Engagement Level Calculation:
--   - VERY_HIGH: total_score >= 1000 OR consecutive_days_streak >= 30
--   - HIGH: total_score >= 500 OR consecutive_days_streak >= 14
--   - MEDIUM: total_score >= 100 OR total_content_views >= 50
--   - LOW: all other cases

-- Conversion Potential Calculation:
--   - VERY_HIGH: has_contacts AND (engagement_level = VERY_HIGH OR HIGH)
--   - HIGH: has_contacts AND engagement_level = MEDIUM
--   - MEDIUM: has_contacts OR engagement_level >= MEDIUM
--   - LOW: engagement_level = LOW AND total_score > 0
--   - VERY_LOW: no significant activity

-- has_contacts = has_whatsapp OR has_telegram OR total_phones > 0
