-- ========================================
-- Adiciona campos de canal ao content_record
-- Versão: 1.0.11
-- Data: 2026-02-11
-- ========================================

-- Adiciona coluna channel_id (TEXT) para armazenar o ID do canal
ALTER TABLE content_record
ADD COLUMN IF NOT EXISTS channel_id TEXT;

-- Adiciona coluna channel_owner_link_id (TEXT) para armazenar o link do proprietário do canal
ALTER TABLE content_record
ADD COLUMN IF NOT EXISTS channel_owner_link_id TEXT;

-- Cria índices para melhorar performance de buscas por canal
CREATE INDEX IF NOT EXISTS idx_content_record_channel_id ON content_record(channel_id);
CREATE INDEX IF NOT EXISTS idx_content_record_channel_owner_link_id ON content_record(channel_owner_link_id);

-- Comentários descritivos
COMMENT ON COLUMN content_record.channel_id IS 'ID único do canal (ex: YouTube channel ID)';
COMMENT ON COLUMN content_record.channel_owner_link_id IS 'Link ou ID do proprietário do canal no sistema';
