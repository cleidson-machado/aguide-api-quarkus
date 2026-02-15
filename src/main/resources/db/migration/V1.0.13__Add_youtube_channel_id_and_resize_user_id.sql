-- =========================================================
-- Migration: Add YouTube Channel ID and Resize YouTube User ID
-- Version: V1.0.13
-- Author: Cleidson Machado
-- Date: 2026-02-15
-- Description: Adiciona coluna youtube_channel_id e aumenta
--              tamanho do youtube_user_id de VARCHAR(50) para VARCHAR(255)
-- =========================================================

-- Adicionar coluna YouTube Channel ID à tabela app_user
ALTER TABLE app_user
ADD COLUMN youtube_channel_id VARCHAR(255) NULL;

-- Alterar tamanho do youtube_user_id de VARCHAR(50) para VARCHAR(255)
ALTER TABLE app_user
ALTER COLUMN youtube_user_id TYPE VARCHAR(255);

-- Criar índice para melhorar performance de queries por YouTube Channel ID
CREATE INDEX idx_app_user_youtube_channel_id ON app_user(youtube_channel_id);

-- Comentários para documentação (PostgreSQL)
COMMENT ON COLUMN app_user.youtube_channel_id IS 'YouTube Channel ID (formato: UCxxxxx ou UXxxxxx). Capturado durante login OAuth com Google. Null se o usuário não tiver canal YouTube.';

-- =========================================================
-- Notas de Implementação:
--
-- 1. youtube_channel_id é NULLABLE (opcional)
-- 2. youtube_user_id aumentado de VARCHAR(50) para VARCHAR(255)
-- 3. youtube_channel_id tem tamanho máximo de 255 chars
-- 4. Índice criado em youtube_channel_id para otimizar buscas
-- 5. Campos são atualizados durante login OAuth (GoogleOAuthRequest)
-- 6. Se os campos vierem null, mantém valor anterior (não sobrescreve)
--
-- Campos YouTube na tabela app_user:
-- - youtube_user_id: User ID do YouTube (UCxxxxx)
-- - youtube_channel_id: Channel ID do YouTube (UCxxxxx)
-- - youtube_channel_title: Título do canal YouTube
-- =========================================================
