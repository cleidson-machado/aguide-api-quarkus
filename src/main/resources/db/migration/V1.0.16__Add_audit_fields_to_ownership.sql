-- =========================================================
-- Migration: Add Audit Fields to Content Ownership
-- Version: V1.0.16
-- Author: System
-- Date: 2026-02-19
-- Description: Adiciona campos de auditoria para tracking de
--              tentativas, rejeições e cancelamentos de ownership.
--              Implementa suporte a retry e idempotência.
-- =========================================================

-- Adicionar campo rejection_reason (motivo da rejeição)
ALTER TABLE content_ownership
ADD COLUMN rejection_reason TEXT NULL;

-- Adicionar campo retry_count (contador de tentativas)
ALTER TABLE content_ownership
ADD COLUMN retry_count INTEGER NOT NULL DEFAULT 0;

-- Adicionar campo last_attempt_at (última tentativa)
ALTER TABLE content_ownership
ADD COLUMN last_attempt_at TIMESTAMP NULL;

-- Adicionar campo cancelled_by_user (cancelado pelo usuário)
ALTER TABLE content_ownership
ADD COLUMN cancelled_by_user BOOLEAN NOT NULL DEFAULT false;

-- Criar índice para queries de retry
CREATE INDEX idx_ownership_retry_count ON content_ownership(retry_count);

-- Criar índice para queries de cancelamentos
CREATE INDEX idx_ownership_cancelled ON content_ownership(cancelled_by_user);

-- Comentários para documentação (PostgreSQL)
COMMENT ON COLUMN content_ownership.rejection_reason IS 'Motivo detalhado da rejeição: CHANNEL_MISMATCH, NO_CHANNEL, USER_CANCELLED, etc.';
COMMENT ON COLUMN content_ownership.retry_count IS 'Número de tentativas de validação. Incrementado a cada resubmissão.';
COMMENT ON COLUMN content_ownership.last_attempt_at IS 'Timestamp da última tentativa de validação (sucesso ou falha).';
COMMENT ON COLUMN content_ownership.cancelled_by_user IS 'true = usuário cancelou o pedido; false = rejeição automática do sistema.';

-- =========================================================
-- Notas de Implementação:
--
-- 1. IDEMPOTÊNCIA:
--    - Constraint UNIQUE (user_id, content_id) garante apenas 1 registro
--    - Resubmissões ATUALIZAM o registro existente (não criam duplicatas)
--    - retry_count é incrementado automaticamente no Service
--
-- 2. REJECTION TRACKING:
--    - rejection_reason armazena o motivo específico da rejeição
--    - Valores possíveis: CHANNEL_MISMATCH, NO_CHANNEL, USER_CANCELLED
--    - NULL quando status = VERIFIED
--
-- 3. RETRY LOGIC:
--    - retry_count começa em 0 na primeira tentativa
--    - Incrementado a cada nova tentativa
--    - Permite implementar limite de retries no futuro
--
-- 4. USER CANCELLATION:
--    - cancelled_by_user = true → usuário explicitamente cancelou
--    - cancelled_by_user = false → sistema rejeitou automaticamente
--    - Útil para analytics e suporte ao cliente
--
-- 5. TIMESTAMP TRACKING:
--    - last_attempt_at registra TODAS as tentativas (sucesso ou falha)
--    - updated_at continua registrando qualquer mudança no registro
--    - created_at permanece inalterado (primeira tentativa)
--
-- 6. FUTURO - AUDIT LOG TABLE:
--    - Estes campos são suficientes para auditoria básica
--    - Se precisar histórico COMPLETO, criar ownership_audit_log depois
--    - Por ora, mantemos simplicidade com campos diretos
-- =========================================================
