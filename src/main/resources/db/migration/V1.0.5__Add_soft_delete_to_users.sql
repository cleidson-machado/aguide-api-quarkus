-- =============================================
-- Adiciona Soft Delete à Tabela app_user
-- Versão: 1.0.5
-- Data: 2026-02-04
-- =============================================

-- Adiciona coluna deleted_at para soft delete
ALTER TABLE app_user
ADD COLUMN deleted_at TIMESTAMP DEFAULT NULL;

-- Adiciona comentário na coluna
COMMENT ON COLUMN app_user.deleted_at IS 'Data de exclusão lógica (soft delete). NULL = usuário ativo';

-- Cria índice para melhorar performance de queries que filtram usuários ativos
CREATE INDEX idx_user_active ON app_user(deleted_at) WHERE deleted_at IS NULL;

-- Cria índice composto para buscar usuários ativos por email
CREATE INDEX idx_user_email_active ON app_user(email, deleted_at) WHERE deleted_at IS NULL;
