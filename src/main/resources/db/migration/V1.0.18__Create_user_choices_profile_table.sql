-- =========================================================
-- Migration: Create User Choices Profile Table
-- Version: V1.0.18
-- Author: System
-- Date: 2026-03-16
-- Description: Cria tabela app_user_choices_profile para armazenar
--              escolhas e preferências de usuários coletadas no
--              formulário de onboarding (CREATOR ou CONSUMER).
-- =========================================================

-- Criar tabela app_user_choices_profile
CREATE TABLE app_user_choices_profile (
    -- Identificação primária
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    -- Campos comuns (obrigatórios para ambos os perfis)
    user_id UUID NOT NULL,
    profile_type VARCHAR(20) NOT NULL,
    niche_context VARCHAR(300) NOT NULL,

    -- ========== CAMPOS CREATOR ==========
    channel_name VARCHAR(200),
    channel_handle VARCHAR(300),
    channel_age_range VARCHAR(30),
    subscriber_range VARCHAR(30),
    monetization_status VARCHAR(30),
    main_niche VARCHAR(200),
    content_formats TEXT, -- JSON array: ["VLOG", "TUTORIAL"]
    commercial_intent VARCHAR(50),
    offered_service VARCHAR(500),
    publishing_frequency VARCHAR(30),
    content_differential VARCHAR(500),

    -- ========== CAMPOS CONSUMER ==========
    current_situation VARCHAR(50),
    main_objective VARCHAR(50),
    visa_type_interest VARCHAR(50),
    knowledge_level VARCHAR(30),
    current_info_sources TEXT, -- JSON array: ["YOUTUBE", "BLOGS"]
    main_difficulty VARCHAR(50),
    preferred_content_type VARCHAR(50),
    service_hiring_intent VARCHAR(50),
    immigration_timeframe VARCHAR(50),
    platform_expectation VARCHAR(500),

    -- Timestamps de auditoria
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,

    -- Constraints
    CONSTRAINT fk_user_choice_user FOREIGN KEY (user_id) REFERENCES app_user(id) ON DELETE CASCADE,
    CONSTRAINT chk_profile_type CHECK (profile_type IN ('CREATOR', 'CONSUMER'))
);

-- Criar índices para performance
CREATE INDEX idx_user_choices_user_id ON app_user_choices_profile(user_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_user_choices_profile_type ON app_user_choices_profile(profile_type) WHERE deleted_at IS NULL;
CREATE INDEX idx_user_choices_niche_context ON app_user_choices_profile(niche_context) WHERE deleted_at IS NULL;
CREATE INDEX idx_user_choices_deleted_at ON app_user_choices_profile(deleted_at);

-- Índices específicos para CREATOR
CREATE INDEX idx_user_choices_monetization_status ON app_user_choices_profile(monetization_status) WHERE profile_type = 'CREATOR' AND deleted_at IS NULL;
CREATE INDEX idx_user_choices_channel_handle ON app_user_choices_profile(channel_handle) WHERE profile_type = 'CREATOR' AND deleted_at IS NULL;

-- Índices específicos para CONSUMER
CREATE INDEX idx_user_choices_visa_type_interest ON app_user_choices_profile(visa_type_interest) WHERE profile_type = 'CONSUMER' AND deleted_at IS NULL;
CREATE INDEX idx_user_choices_service_hiring_intent ON app_user_choices_profile(service_hiring_intent) WHERE profile_type = 'CONSUMER' AND deleted_at IS NULL;
CREATE INDEX idx_user_choices_immigration_timeframe ON app_user_choices_profile(immigration_timeframe) WHERE profile_type = 'CONSUMER' AND deleted_at IS NULL;

-- Comentários de documentação
COMMENT ON TABLE app_user_choices_profile IS 'Armazena escolhas de onboarding dos usuários (CREATOR ou CONSUMER)';
COMMENT ON COLUMN app_user_choices_profile.user_id IS 'Referência ao usuário (soft reference para flexibilidade)';
COMMENT ON COLUMN app_user_choices_profile.profile_type IS 'Tipo de perfil: CREATOR (produtor) ou CONSUMER (audiência)';
COMMENT ON COLUMN app_user_choices_profile.niche_context IS 'Nicho MACRO da plataforma (ex: "Imigração Portugal", "Tecnologia")';
COMMENT ON COLUMN app_user_choices_profile.main_niche IS 'Nicho ESPECÍFICO do canal (CREATOR only, ex: "Visto D7")';
COMMENT ON COLUMN app_user_choices_profile.channel_handle IS 'Handle do YouTube (CREATOR, ex: "@MeuCanal")';
COMMENT ON COLUMN app_user_choices_profile.content_formats IS 'Formatos de conteúdo (CREATOR) - JSON array validado via ContentFormat enum';
COMMENT ON COLUMN app_user_choices_profile.current_info_sources IS 'Fontes de informação (CONSUMER) - JSON array validado via InfoSource enum';
COMMENT ON COLUMN app_user_choices_profile.deleted_at IS 'Timestamp de soft delete. NULL = ativo';
