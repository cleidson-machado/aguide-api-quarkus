-- ========================================
-- ADD MUTE/CLEAR TO PARTICIPANT + CREATE USER BLOCK TABLE
-- Version: 1.0.24
-- Date: 2026-04-25
-- Author: Cleidson
-- Description:
--   1. Add muted_at and cleared_at columns to app_conversation_participant
--      (supports mute-toggle and per-participant clear)
--   2. Create app_user_block table for blocking users in DIRECT conversations
-- ========================================

-- ✅ NON-DESTRUCTIVE ONLY

-- ==========================================================
-- PARTE 1: Campos de silenciamento e limpeza por participante
-- ==========================================================

-- muted_at: registra quando o usuário silenciou (null = não silenciado)
ALTER TABLE app_conversation_participant
    ADD COLUMN IF NOT EXISTS muted_at TIMESTAMP;

-- cleared_at: marco de limpeza por participante
--   Mensagens com sent_at <= cleared_at ficam ocultas para este participante
ALTER TABLE app_conversation_participant
    ADD COLUMN IF NOT EXISTS cleared_at TIMESTAMP;

-- Documentação
COMMENT ON COLUMN app_conversation_participant.muted_at IS
    'Data/hora em que o usuário silenciou esta conversa. NULL = não silenciado. Atualizado junto com is_muted.';

COMMENT ON COLUMN app_conversation_participant.cleared_at IS
    'Marco de limpeza por participante. Mensagens com sent_at <= cleared_at ficam ocultas apenas para este usuário. Outros participantes continuam vendo o histórico completo.';

-- Índice parcial para acelerar contagem de mensagens após cleared_at
CREATE INDEX IF NOT EXISTS idx_participant_cleared_at
    ON app_conversation_participant(user_id, conversation_id, cleared_at)
    WHERE cleared_at IS NOT NULL;

-- ==========================================================
-- PARTE 2: Tabela de bloqueio entre usuários
-- ==========================================================

CREATE TABLE IF NOT EXISTS app_user_block (
    id              UUID      PRIMARY KEY DEFAULT gen_random_uuid(),

    -- Quem bloqueou
    blocker_user_id UUID      NOT NULL,

    -- Quem foi bloqueado
    blocked_user_id UUID      NOT NULL,

    -- Auditoria
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Constraints
    CONSTRAINT fk_user_block_blocker FOREIGN KEY (blocker_user_id)
        REFERENCES app_user(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_block_blocked FOREIGN KEY (blocked_user_id)
        REFERENCES app_user(id) ON DELETE CASCADE,

    -- Um usuário só pode bloquear outro uma vez
    CONSTRAINT unique_user_block UNIQUE (blocker_user_id, blocked_user_id),

    -- Não pode bloquear a si mesmo (proteção no DB)
    CONSTRAINT check_no_self_block CHECK (blocker_user_id <> blocked_user_id)
);

-- Índices de performance
CREATE INDEX IF NOT EXISTS idx_user_block_blocker
    ON app_user_block(blocker_user_id);

CREATE INDEX IF NOT EXISTS idx_user_block_blocked
    ON app_user_block(blocked_user_id);

-- Documentação
COMMENT ON TABLE app_user_block IS
    'Registra bloqueios unilaterais entre usuários. A bloqueia B não implica B bloqueia A.';

COMMENT ON COLUMN app_user_block.blocker_user_id IS
    'Usuário que executou o bloqueio.';

COMMENT ON COLUMN app_user_block.blocked_user_id IS
    'Usuário que foi bloqueado. Não pode enviar mensagens DIRECT para o bloqueador.';
