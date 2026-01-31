-- ========================================
-- CRIAÇÃO DAS TABELAS - H2 COMPATIBLE
-- Versão: 1.0.0
-- Data: 2025-10-05
-- Sintaxe: H2 Database (para testes)
-- ========================================

-- ========== TABELA: app_user ==========
CREATE TABLE app_user (
    id UUID PRIMARY KEY DEFAULT RANDOM_UUID(),
    name VARCHAR(255) NOT NULL,
    surname VARCHAR(255),
    email VARCHAR(255) NOT NULL UNIQUE,
    passwd VARCHAR(255) NOT NULL
);

-- Índice para busca rápida por email
CREATE INDEX idx_app_user_email ON app_user(email);

-- ========== TABELA: content_record ==========
CREATE TABLE content_record (
    -- ========== IDENTIFICAÇÃO ==========
    id UUID PRIMARY KEY DEFAULT RANDOM_UUID(),

    -- ========== INFORMAÇÕES BÁSICAS ==========
    title VARCHAR(1000) NOT NULL,
    description TEXT,
    url VARCHAR(2048) UNIQUE,
    thumbnail_url VARCHAR(2048),

    -- ========== CANAL E TIPO ==========
    channel_name VARCHAR(255),
    content_type VARCHAR(50),

    -- ========== CATEGORIZAÇÃO ==========
    category_id VARCHAR(50),
    category_name VARCHAR(255),
    tags TEXT,

    -- ========== CARACTERÍSTICAS TÉCNICAS ==========
    duration_seconds INTEGER,
    duration_iso VARCHAR(50),
    definition VARCHAR(20),
    caption BOOLEAN,

    -- ========== MÉTRICAS DE ENGAJAMENTO ==========
    view_count BIGINT DEFAULT 0,
    like_count BIGINT DEFAULT 0,
    comment_count BIGINT DEFAULT 0,

    -- ========== IDIOMAS ==========
    default_language VARCHAR(10),
    default_audio_language VARCHAR(10)
);

-- Índices para melhorar performance de buscas (H2 não suporta GIN)
CREATE INDEX idx_content_record_title ON content_record(title);
CREATE INDEX idx_content_record_category_id ON content_record(category_id);
CREATE INDEX idx_content_record_content_type ON content_record(content_type);
