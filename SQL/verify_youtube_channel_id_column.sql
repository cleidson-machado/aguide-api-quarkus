-- =========================================================
-- Script: Verificar se coluna youtube_channel_id existe
-- Descrição: Verifica estrutura da tabela app_user e migração V1.0.13
-- =========================================================

-- 1. Verificar se a coluna youtube_channel_id existe na tabela
SELECT
    column_name,
    data_type,
    character_maximum_length,
    is_nullable
FROM information_schema.columns
WHERE table_schema = 'public'
  AND table_name = 'app_user'
  AND column_name IN ('youtube_user_id', 'youtube_channel_id', 'youtube_channel_title')
ORDER BY column_name;

-- 2. Verificar se a migração V1.0.13 foi aplicada
SELECT
    installed_rank,
    version,
    description,
    type,
    script,
    checksum,
    installed_on,
    success
FROM flyway_schema_history
WHERE version = '1.0.13'
ORDER BY installed_rank DESC;

-- 3. Verificar dados dos usuários com YouTube info
SELECT
    id,
    name,
    surname,
    email,
    youtube_user_id,
    youtube_channel_id,
    youtube_channel_title,
    oauth_provider,
    updated_at
FROM app_user
WHERE oauth_provider = 'GOOGLE'
ORDER BY updated_at DESC;

-- =========================================================
-- IMPORTANTE: Se a coluna youtube_channel_id NÃO existir:
-- Execute manualmente a migração V1.0.13:
--
-- ALTER TABLE app_user ADD COLUMN youtube_channel_id VARCHAR(255) NULL;
-- ALTER TABLE app_user ALTER COLUMN youtube_user_id TYPE VARCHAR(255);
-- CREATE INDEX idx_app_user_youtube_channel_id ON app_user(youtube_channel_id);
-- =========================================================
