-- ========================================
-- RENOMEAÇÃO DE COLUNAS - CONTENT_RECORD
-- Versão: 1.0.10
-- Data: 2026-02-11
-- Descrição: Renomeia as colunas url e thumbnail_url
--            para video_url e video_thumbnail_url
--            para melhor clareza semântica
-- ========================================

-- Renomeia a coluna url para video_url
ALTER TABLE content_record
RENAME COLUMN url TO video_url;

-- Renomeia a coluna thumbnail_url para video_thumbnail_url
ALTER TABLE content_record
RENAME COLUMN thumbnail_url TO video_thumbnail_url;

-- Nota: A constraint UNIQUE na coluna video_url é mantida automaticamente
-- Nota: Os índices existentes são preservados pelo PostgreSQL
