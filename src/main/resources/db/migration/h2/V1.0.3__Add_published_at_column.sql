-- ========================================
-- ADICIONA COLUNA published_at - H2 COMPATIBLE
-- Versão: 1.0.3
-- Data: 2026-01-31
-- Descrição: Adiciona coluna para armazenar a data de publicação do conteúdo
-- Sintaxe: H2 Database (para testes)
-- ========================================

-- Adiciona coluna published_at na tabela content_record
ALTER TABLE content_record
ADD COLUMN published_at TIMESTAMP;

-- Cria índice para melhorar performance de ordenação por data de publicação
CREATE INDEX idx_content_record_published_at ON content_record(published_at);
