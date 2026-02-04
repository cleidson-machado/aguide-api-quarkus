-- =============================================
-- Adiciona Soft Delete à Tabela app_user (H2)
-- Versão: 1.0.5
-- Data: 2026-02-04
-- =============================================

-- Adiciona coluna deleted_at para soft delete
ALTER TABLE app_user ADD COLUMN deleted_at TIMESTAMP DEFAULT NULL;

-- Cria índice para melhorar performance (H2 não suporta WHERE em índices)
CREATE INDEX idx_user_active ON app_user(deleted_at);

-- Cria índice composto
CREATE INDEX idx_user_email_active ON app_user(email, deleted_at);
