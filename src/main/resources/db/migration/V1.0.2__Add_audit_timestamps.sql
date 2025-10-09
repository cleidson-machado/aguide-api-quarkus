-- ========================================
-- ADD AUDIT TIMESTAMPS TO CONTENT_RECORD
-- Versão: 1.0.2
-- Data: 2025-10-09
-- ==========================================

-- Adiciona colunas de auditoria para rastrear criação e atualização
ALTER TABLE content_record
ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
ADD COLUMN updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

-- Cria índice para consultas ordenadas por data de criação
CREATE INDEX idx_content_record_created_at ON content_record(created_at DESC);

-- Cria índice para consultas ordenadas por data de atualização
CREATE INDEX idx_content_record_updated_at ON content_record(updated_at DESC);

-- Comentários para documentação
COMMENT ON COLUMN content_record.created_at IS 'Data e hora de criação do registro';
COMMENT ON COLUMN content_record.updated_at IS 'Data e hora da última atualização do registro';