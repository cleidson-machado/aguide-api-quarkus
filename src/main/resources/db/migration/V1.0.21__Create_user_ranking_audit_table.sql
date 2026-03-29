-- Migration: V1.0.21__Create_user_ranking_audit_table.sql
-- Descrição: Cria tabela de auditoria para rastreamento de todas as mudanças em user_ranking
-- Data: 2026-03-29
-- Autor: Backend Team - Security Enhancement

-- Tabela de auditoria para user_ranking
CREATE TABLE app_user_ranking_audit (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    -- Referência ao ranking modificado
    ranking_id UUID NOT NULL,
    user_id UUID NOT NULL,

    -- Tipo de operação
    operation VARCHAR(20) NOT NULL CHECK (operation IN ('CREATE', 'UPDATE', 'ADD_POINTS', 'DELETE', 'RESTORE')),

    -- Dados da mudança
    field_name VARCHAR(100),  -- Nome do campo alterado (null para CREATE/DELETE)
    old_value TEXT,           -- Valor antigo (null para CREATE)
    new_value TEXT,           -- Valor novo (null para DELETE)

    -- Específico para ADD_POINTS
    points_added INTEGER,     -- Quantidade de pontos adicionados
    points_reason VARCHAR(50), -- Motivo: 'daily_login', '7day_bonus', '30day_bonus', etc.

    -- Metadados da requisição
    ip_address VARCHAR(45),   -- IPv4 ou IPv6
    user_agent TEXT,          -- Browser/app do cliente
    request_id VARCHAR(100),  -- Correlation ID (se disponível)

    -- Timestamp
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Índices para performance
CREATE INDEX idx_ranking_audit_ranking_id ON app_user_ranking_audit(ranking_id);
CREATE INDEX idx_ranking_audit_user_id ON app_user_ranking_audit(user_id);
CREATE INDEX idx_ranking_audit_operation ON app_user_ranking_audit(operation);
CREATE INDEX idx_ranking_audit_created_at ON app_user_ranking_audit(created_at DESC);
CREATE INDEX idx_ranking_audit_user_created ON app_user_ranking_audit(user_id, created_at DESC);

-- Comentários na tabela
COMMENT ON TABLE app_user_ranking_audit IS 'Auditoria completa de todas as operações em app_user_ranking';
COMMENT ON COLUMN app_user_ranking_audit.ranking_id IS 'ID do ranking modificado (referência histórica, não FK para permitir auditoria de deletados)';
COMMENT ON COLUMN app_user_ranking_audit.user_id IS 'ID do usuário dono do ranking (desnormalizado para queries mais rápidas)';
COMMENT ON COLUMN app_user_ranking_audit.operation IS 'Tipo de operação: CREATE, UPDATE, ADD_POINTS, DELETE, RESTORE';
COMMENT ON COLUMN app_user_ranking_audit.field_name IS 'Nome do campo alterado (UPDATE) ou NULL (CREATE/DELETE/ADD_POINTS)';
COMMENT ON COLUMN app_user_ranking_audit.points_added IS 'Quantidade de pontos adicionados (apenas para ADD_POINTS)';
COMMENT ON COLUMN app_user_ranking_audit.points_reason IS 'Motivo da adição de pontos: daily_login, 7day_bonus, 30day_bonus';
COMMENT ON COLUMN app_user_ranking_audit.ip_address IS 'IP do cliente (para detecção de fraude)';
COMMENT ON COLUMN app_user_ranking_audit.user_agent IS 'User-Agent do cliente (identifica app/browser)';
COMMENT ON COLUMN app_user_ranking_audit.request_id IS 'Correlation ID para rastreamento end-to-end';
