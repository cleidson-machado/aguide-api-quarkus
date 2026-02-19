-- =========================================================
-- Migration: Create Content Ownership Table
-- Version: V1.0.15
-- Author: System
-- Date: 2026-02-19
-- Description: Cria tabela content_ownership como soft reference
--              entre usuários e conteúdo do YouTube, com validação
--              criptográfica HMAC-SHA256.
-- =========================================================

-- Criar ENUM para ownership_status
CREATE TYPE ownership_status AS ENUM ('PENDING', 'VERIFIED', 'REJECTED');

-- Criar tabela content_ownership
CREATE TABLE content_ownership (
    -- Identificação primária
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    -- Referências (soft references - não usamos FK rígidas)
    user_id UUID NOT NULL,
    content_id UUID NOT NULL,

    -- Campos de validação
    youtube_channel_id VARCHAR(255) NOT NULL,
    content_channel_id VARCHAR(255) NOT NULL,

    -- Status e hash de validação
    ownership_status ownership_status NOT NULL DEFAULT 'PENDING',
    validation_hash VARCHAR(512) NOT NULL,

    -- Auditoria de verificação
    verified_at TIMESTAMP,
    verified_by UUID,

    -- Timestamps de auditoria
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Constraint: um usuário pode validar a mesma propriedade apenas uma vez
    CONSTRAINT uk_ownership_user_content UNIQUE (user_id, content_id)
);

-- Criar índices para performance
CREATE INDEX idx_ownership_user_id ON content_ownership(user_id);
CREATE INDEX idx_ownership_content_id ON content_ownership(content_id);
CREATE INDEX idx_ownership_status ON content_ownership(ownership_status);
CREATE INDEX idx_ownership_youtube_channel ON content_ownership(youtube_channel_id);
CREATE INDEX idx_ownership_validation_hash ON content_ownership(validation_hash);
CREATE INDEX idx_ownership_created_at ON content_ownership(created_at);

-- Comentários para documentação (PostgreSQL)
COMMENT ON TABLE content_ownership IS 'Tabela de soft reference entre usuários e conteúdo do YouTube. Valida propriedade através de HMAC-SHA256 sem foreign keys rígidas.';

COMMENT ON COLUMN content_ownership.id IS 'Identificador único UUID da ownership.';
COMMENT ON COLUMN content_ownership.user_id IS 'Soft reference para app_user.id. Não é FK rígida.';
COMMENT ON COLUMN content_ownership.content_id IS 'Soft reference para content_record.id. Não é FK rígida.';
COMMENT ON COLUMN content_ownership.youtube_channel_id IS 'YouTube Channel ID do usuário (copiado de app_user.youtube_channel_id no momento da validação).';
COMMENT ON COLUMN content_ownership.content_channel_id IS 'YouTube Channel ID do conteúdo (copiado de content_record.channel_id no momento da validação).';
COMMENT ON COLUMN content_ownership.ownership_status IS 'Status da ownership: PENDING (aguardando validação), VERIFIED (validada), REJECTED (hash inválido).';
COMMENT ON COLUMN content_ownership.validation_hash IS 'HMAC-SHA256 hash calculado no backend. Garante integridade da validação.';
COMMENT ON COLUMN content_ownership.verified_at IS 'Timestamp de quando a ownership foi verificada.';
COMMENT ON COLUMN content_ownership.verified_by IS 'ID do administrador que verificou manualmente (opcional).';
COMMENT ON COLUMN content_ownership.created_at IS 'Timestamp de criação do registro.';
COMMENT ON COLUMN content_ownership.updated_at IS 'Timestamp da última atualização.';

-- =========================================================
-- Notas de Implementação:
--
-- 1. SOFT REFERENCES: user_id e content_id não têm FK rígidas
--    - Dados vêm de fontes externas (YouTube API)
--    - Ciclos de vida são independentes
--    - Dados podem ser alterados/deletados externamente
--
-- 2. VALIDAÇÃO HMAC-SHA256:
--    - Hash calculado: HMAC(userId + contentId + channelIds, secret)
--    - Backend SEMPRE recalcula o hash (nunca confia no cliente)
--    - Se hashes coincidirem → VERIFIED
--    - Se hashes divergirem → REJECTED
--
-- 3. FLUXO DE VALIDAÇÃO:
--    a) Flutter envia: userId, contentId, timestamp
--    b) Backend busca: user.youtubeChannelId, content.channelId
--    c) Backend valida: user.channelId == content.channelId
--    d) Backend gera: HMAC-SHA256(userId + contentId + channelIds + secret)
--    e) Backend salva: content_ownership (status VERIFIED ou REJECTED)
--    f) Backend atualiza: content_record.validation_hash (para query rápida)
--
-- 4. CONSTRAINT ÚNICA:
--    - Um usuário só pode validar ownership do mesmo conteúdo uma vez
--    - Evita duplicatas e re-validações desnecessárias
--
-- 5. ÍNDICES:
--    - user_id: lista todo conteúdo de um usuário
--    - content_id: verifica quem reivindicou um conteúdo
--    - status: queries por status (pending, verified, rejected)
--    - youtube_channel_id: busca por canal
--    - validation_hash: validação de integridade
--    - created_at: ordem cronológica
--
-- 6. SEGURANÇA:
--    - Todos os endpoints protegidos com JWT
--    - Usuário só pode validar ownership para seu próprio userId
--    - Secret key gerenciada via variável de ambiente
--    - Hash sempre recalculado no backend
-- =========================================================
