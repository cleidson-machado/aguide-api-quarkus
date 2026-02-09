-- Migration: Adicionar soft delete na tabela phone_numbers
-- Permite marcar telefones como deletados sem remover do banco

ALTER TABLE phone_numbers
ADD COLUMN deleted_at TIMESTAMP NULL;

-- Comentário
COMMENT ON COLUMN phone_numbers.deleted_at IS 'Data de exclusão lógica (soft delete). Null = telefone ativo';

-- Índice para otimizar consultas de telefones ativos
CREATE INDEX idx_phone_deleted_at ON phone_numbers(user_id) WHERE deleted_at IS NULL;
