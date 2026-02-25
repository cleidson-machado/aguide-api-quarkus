-- =========================================================
-- Migration: Add Validation Hash to Content Record
-- Version: V1.0.14
-- Author: System
-- Date: 2026-02-19
-- Description: Adiciona coluna validation_hash à tabela content_record
--              para marcar conteúdo com propriedade validada.
--              Conteúdo com hash != null possui dono reconhecido.
-- =========================================================

-- Adicionar coluna validation_hash à tabela content_record
ALTER TABLE content_record
ADD COLUMN validation_hash VARCHAR(512) NULL;

-- Criar índice para melhorar performance de queries por hash
CREATE INDEX idx_content_record_validation_hash ON content_record(validation_hash);

-- Comentários para documentação (PostgreSQL)
COMMENT ON COLUMN content_record.validation_hash IS 'HMAC-SHA256 hash de validação de propriedade. NULL = sem dono; != NULL = propriedade validada. Calculado com userId + contentId + channelId + secret.';

-- =========================================================
-- Notas de Implementação:
--
-- 1. validation_hash é NULLABLE (opcional)
-- 2. Hash é gerado usando HMAC-SHA256 no backend
-- 3. Conteúdo com hash NULL = sem propriedade validada
-- 4. Conteúdo com hash preenchido = propriedade reconhecida
-- 5. Flutter pode verificar propriedade diretamente na query
-- 6. Evita request extra ao carregar lista de conteúdo
-- 7. Hash máximo: 512 chars (hexadecimal de SHA256 = 64 chars + margem)
--
-- Fluxo de Validação:
-- 1. Flutter envia userId + contentId para API
-- 2. Backend valida channelId (user) == channelId (content)
-- 3. Backend gera HMAC e atualiza content_record.validation_hash
-- 4. Próximas queries já trazem indicador de propriedade
-- =========================================================
