-- Migration: Criar tabela de telefones com suporte a múltiplos apps de mensagem
-- Suporta: WhatsApp, Telegram, Signal
-- Formato: E.164 internacional (+556798407322)

CREATE TABLE phone_numbers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,

    -- Componentes do telefone
    country_code VARCHAR(5) NOT NULL,           -- Ex: "+55", "+351"
    area_code VARCHAR(5),                       -- Ex: "67", "11" (Brasil apenas)
    number VARCHAR(20) NOT NULL,                -- Ex: "984073221"
    full_number VARCHAR(30) NOT NULL UNIQUE,    -- Ex: "+556798407322" (formato E.164)

    -- Tipo e status
    type VARCHAR(20) NOT NULL DEFAULT 'MOBILE', -- MOBILE ou LANDLINE
    is_primary BOOLEAN DEFAULT FALSE,           -- Telefone principal do usuário
    is_verified BOOLEAN DEFAULT FALSE,          -- Verificado via SMS (2FA futuro)

    -- Apps de mensagem disponíveis neste número
    has_whatsapp BOOLEAN DEFAULT FALSE,         -- Tem WhatsApp
    has_telegram BOOLEAN DEFAULT FALSE,         -- Tem Telegram
    has_signal BOOLEAN DEFAULT FALSE,           -- Tem Signal

    -- Auditoria
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Constraints
    CONSTRAINT fk_phone_user FOREIGN KEY (user_id)
        REFERENCES app_user(id) ON DELETE CASCADE,
    CONSTRAINT check_type CHECK (type IN ('MOBILE', 'LANDLINE'))
);

-- Índices para performance
CREATE INDEX idx_phone_user_id ON phone_numbers(user_id);
CREATE INDEX idx_phone_full_number ON phone_numbers(full_number);
CREATE INDEX idx_phone_primary ON phone_numbers(user_id, is_primary) WHERE is_primary = TRUE;

-- Índices para busca por app de mensagem
CREATE INDEX idx_phone_whatsapp ON phone_numbers(user_id) WHERE has_whatsapp = TRUE;
CREATE INDEX idx_phone_telegram ON phone_numbers(user_id) WHERE has_telegram = TRUE;

-- Comentários
COMMENT ON TABLE phone_numbers IS 'Telefones dos usuários com suporte a múltiplos apps de mensagem';
COMMENT ON COLUMN phone_numbers.full_number IS 'Número completo no formato E.164 (+556798407322)';
COMMENT ON COLUMN phone_numbers.area_code IS 'DDD/código de área (obrigatório apenas para Brasil)';
COMMENT ON COLUMN phone_numbers.has_whatsapp IS 'Indica se este número tem WhatsApp ativo';
COMMENT ON COLUMN phone_numbers.has_telegram IS 'Indica se este número tem Telegram ativo';
COMMENT ON COLUMN phone_numbers.has_signal IS 'Indica se este número tem Signal ativo';
