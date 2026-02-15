-- =========================================================
-- Migration: Add YouTube Fields to Users Table
-- Version: V1.0.12
-- Author: Cleidson Machado
-- Date: 2026-02-15
-- Description: Adiciona campos opcionais para armazenar
--              YouTube User ID e Channel Title dos usuários
-- =========================================================

-- Adicionar colunas YouTube à tabela app_user
ALTER TABLE app_user
ADD COLUMN youtube_user_id VARCHAR(50) NULL,
ADD COLUMN youtube_channel_title VARCHAR(255) NULL;

-- Criar índice para melhorar performance de queries por YouTube User ID
CREATE INDEX idx_app_user_youtube_user_id ON app_user(youtube_user_id);

-- Comentários para documentação (PostgreSQL)
COMMENT ON COLUMN app_user.youtube_user_id IS 'YouTube User ID ou Channel ID (formato: UCxxxxx ou UXxxxxx). Capturado durante login OAuth com Google. Null se o usuário não tiver canal YouTube.';
COMMENT ON COLUMN app_user.youtube_channel_title IS 'Título do canal YouTube do usuário. Null se o usuário não tiver canal YouTube.';

-- =========================================================
-- Notas de Implementação:
--
-- 1. Ambos os campos são NULLABLE (opcionais)
-- 2. youtube_user_id tem tamanho máximo de 50 chars
-- 3. youtube_channel_title tem tamanho máximo de 255 chars
-- 4. Índice criado em youtube_user_id para otimizar buscas
-- 5. Campos são atualizados durante login OAuth (GoogleOAuthRequest)
-- 6. Se os campos vierem null, mantém valor anterior (não sobrescreve)
-- =========================================================
