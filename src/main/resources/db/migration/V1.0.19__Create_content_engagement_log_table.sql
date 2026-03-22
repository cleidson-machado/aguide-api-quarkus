-- V1.0.19__Create_content_engagement_log_table.sql
-- Migration to create the content_engagement_log table for tracking user interactions with content
-- Date: 2026-03-22
-- Author: Cleidson Machado

-- ══════════════════════════════════════════════════════════════
-- TABLE: content_engagement_log
-- PURPOSE: Track all user interactions with content (views, likes, shares, etc.)
-- RELATIONSHIPS:
--   - user_id → app_user.id (ManyToOne)
--   - content_id → content_record.id (ManyToOne)
-- ══════════════════════════════════════════════════════════════

CREATE TABLE IF NOT EXISTS content_engagement_log (
    -- Primary Key
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    -- Foreign Keys
    user_id UUID,
    content_id UUID,

    -- Engagement Type and Status (ENUMs stored as VARCHAR)
    engagement_type VARCHAR(20) NOT NULL
        CHECK (engagement_type IN (
            'VIEW', 'LIKE', 'DISLIKE', 'SHARE', 'BOOKMARK',
            'COMMENT', 'COMPLETE', 'PARTIAL_VIEW', 'CLICK_TO_VIEW'
        )),
    engagement_status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
        CHECK (engagement_status IN ('ACTIVE', 'REMOVED', 'EXPIRED', 'FLAGGED')),

    -- Viewing Metrics
    view_duration_seconds INT CHECK (view_duration_seconds >= 0),
    completion_percentage INT CHECK (completion_percentage >= 0 AND completion_percentage <= 100),
    repeat_count INT DEFAULT 1 CHECK (repeat_count >= 1),

    -- Technical Context
    device_type VARCHAR(20),
    platform VARCHAR(20),
    source VARCHAR(50),
    user_ip VARCHAR(45), -- supports both IPv4 and IPv6
    user_agent TEXT,

    -- Additional Data
    metadata TEXT,
    comment_text TEXT,
    rating INT CHECK (rating >= 1 AND rating <= 5),

    -- Timestamps
    engaged_at TIMESTAMP NOT NULL DEFAULT NOW(),
    ended_at TIMESTAMP,

    -- Audit Fields
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),

    -- Foreign Key Constraints
    CONSTRAINT fk_engagement_user FOREIGN KEY (user_id) REFERENCES app_user(id) ON DELETE SET NULL,
    CONSTRAINT fk_engagement_content FOREIGN KEY (content_id) REFERENCES content_record(id) ON DELETE SET NULL
);

-- ══════════════════════════════════════════════════════════════
-- INDEXES for Performance Optimization
-- ══════════════════════════════════════════════════════════════

-- Index for finding engagements by user (most common query)
CREATE INDEX idx_engagement_user_id ON content_engagement_log(user_id);

-- Index for finding engagements by content
CREATE INDEX idx_engagement_content_id ON content_engagement_log(content_id);

-- Composite index for user+content queries
CREATE INDEX idx_engagement_user_content ON content_engagement_log(user_id, content_id);

-- Index for filtering by engagement type
CREATE INDEX idx_engagement_type ON content_engagement_log(engagement_type);

-- Index for filtering by status
CREATE INDEX idx_engagement_status ON content_engagement_log(engagement_status);

-- Index for time-based queries (recent engagements)
CREATE INDEX idx_engagement_engaged_at ON content_engagement_log(engaged_at DESC);

-- Composite index for active engagements by user+content+type (uniqueness check for reversible engagements)
CREATE INDEX idx_engagement_user_content_type_status ON content_engagement_log(user_id, content_id, engagement_type, engagement_status);

-- ══════════════════════════════════════════════════════════════
-- COMMENTS for Documentation
-- ══════════════════════════════════════════════════════════════

COMMENT ON TABLE content_engagement_log IS 'Logs all user interactions with content (views, likes, shares, bookmarks, comments, etc.)';

COMMENT ON COLUMN content_engagement_log.id IS 'Primary key (UUID)';
COMMENT ON COLUMN content_engagement_log.user_id IS 'Foreign key to app_user table (nullable for anonymous tracking)';
COMMENT ON COLUMN content_engagement_log.content_id IS 'Foreign key to content_record table';
COMMENT ON COLUMN content_engagement_log.engagement_type IS 'Type of interaction: VIEW, LIKE, DISLIKE, SHARE, BOOKMARK, COMMENT, COMPLETE, PARTIAL_VIEW, CLICK_TO_VIEW';
COMMENT ON COLUMN content_engagement_log.engagement_status IS 'Status: ACTIVE, REMOVED, EXPIRED, FLAGGED';
COMMENT ON COLUMN content_engagement_log.view_duration_seconds IS 'Time spent viewing in seconds';
COMMENT ON COLUMN content_engagement_log.completion_percentage IS 'Percentage of content completed (0-100)';
COMMENT ON COLUMN content_engagement_log.repeat_count IS 'Number of times user interacted with this content';
COMMENT ON COLUMN content_engagement_log.device_type IS 'Device type: mobile, tablet, web';
COMMENT ON COLUMN content_engagement_log.platform IS 'Platform: Android, iOS, web';
COMMENT ON COLUMN content_engagement_log.source IS 'Source: home, search, recommendations, profile';
COMMENT ON COLUMN content_engagement_log.user_ip IS 'User IP address (IPv4 or IPv6)';
COMMENT ON COLUMN content_engagement_log.user_agent IS 'Browser/app user agent string';
COMMENT ON COLUMN content_engagement_log.metadata IS 'Additional custom data in JSON format';
COMMENT ON COLUMN content_engagement_log.comment_text IS 'Comment text if engagement_type is COMMENT';
COMMENT ON COLUMN content_engagement_log.rating IS 'Rating value (1-5 stars)';
COMMENT ON COLUMN content_engagement_log.engaged_at IS 'When the interaction started';
COMMENT ON COLUMN content_engagement_log.ended_at IS 'When the interaction ended';
COMMENT ON COLUMN content_engagement_log.created_at IS 'Record creation timestamp';
COMMENT ON COLUMN content_engagement_log.updated_at IS 'Last update timestamp';
