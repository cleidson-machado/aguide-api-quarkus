-- ========================================
-- CRIAÇÃO DAS TABELAS DE MENSAGERIA
-- Versão: 1.0.23
-- Data: 2026-04-05
-- Descrição: Sistema de mensagens entre usuários (1-1 e grupos)
-- ========================================

-- ========== TABELA: app_conversation ==========
-- Representa uma conversa (thread) entre usuários
-- Suporta: DIRECT (1-1), GROUP (grupos), CHANNEL (broadcast)
CREATE TABLE app_conversation (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    -- Informações básicas
    name VARCHAR(255),                              -- Nome do grupo/canal (null para DIRECT)
    description TEXT,                               -- Descrição do grupo/canal
    icon_url VARCHAR(512),                          -- Ícone/foto do grupo/canal

    -- Tipo de conversa
    conversation_type VARCHAR(20) NOT NULL DEFAULT 'DIRECT',

    -- Metadados
    last_message_at TIMESTAMP,                      -- Data da última mensagem (para ordenação do inbox)
    is_archived BOOLEAN NOT NULL DEFAULT FALSE,     -- Arquivada (deprecated - usar ConversationParticipant)
    is_pinned BOOLEAN NOT NULL DEFAULT FALSE,       -- Fixada (deprecated - usar ConversationParticipant)

    -- Auditoria
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,                           -- Soft delete

    -- Constraints
    CONSTRAINT check_conversation_type CHECK (conversation_type IN ('DIRECT', 'GROUP', 'CHANNEL'))
);

-- Índices para performance
CREATE INDEX idx_conversation_last_message ON app_conversation(last_message_at DESC) WHERE deleted_at IS NULL;
CREATE INDEX idx_conversation_type ON app_conversation(conversation_type) WHERE deleted_at IS NULL;

-- Comentários
COMMENT ON TABLE app_conversation IS 'Conversas entre usuários: diretas (1-1), grupos ou canais';
COMMENT ON COLUMN app_conversation.conversation_type IS 'DIRECT: 1-1, GROUP: múltiplos usuários, CHANNEL: broadcast';
COMMENT ON COLUMN app_conversation.last_message_at IS 'Data da última mensagem enviada (usado para ordenação do inbox)';


-- ========== TABELA: app_conversation_participant ==========
-- Relacionamento N-N entre conversas e usuários
-- Armazena metadados específicos de cada participante
CREATE TABLE app_conversation_participant (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    -- Relacionamentos
    conversation_id UUID NOT NULL,
    user_id UUID NOT NULL,
    last_read_message_id UUID,                      -- Última mensagem lida (para contador de não lidas)

    -- Metadados de leitura
    last_read_at TIMESTAMP,                         -- Data/hora da última leitura

    -- Permissões e funções
    is_admin BOOLEAN NOT NULL DEFAULT FALSE,        -- Administrador do grupo
    is_creator BOOLEAN NOT NULL DEFAULT FALSE,      -- Criador do grupo

    -- Preferências do participante
    is_archived BOOLEAN NOT NULL DEFAULT FALSE,     -- Usuário arquivou esta conversa
    is_pinned BOOLEAN NOT NULL DEFAULT FALSE,       -- Usuário fixou esta conversa
    is_muted BOOLEAN NOT NULL DEFAULT FALSE,        -- Usuário silenciou notificações

    -- Auditoria
    joined_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    left_at TIMESTAMP,                              -- Data de saída do grupo

    -- Constraints
    CONSTRAINT fk_participant_conversation FOREIGN KEY (conversation_id)
        REFERENCES app_conversation(id) ON DELETE CASCADE,
    CONSTRAINT fk_participant_user FOREIGN KEY (user_id)
        REFERENCES app_user(id) ON DELETE CASCADE,
    CONSTRAINT unique_participant UNIQUE (conversation_id, user_id)
);

-- Índices para performance
CREATE INDEX idx_participant_user ON app_conversation_participant(user_id) WHERE left_at IS NULL;
CREATE INDEX idx_participant_conversation ON app_conversation_participant(conversation_id) WHERE left_at IS NULL;
CREATE INDEX idx_participant_unread ON app_conversation_participant(user_id, conversation_id, last_read_message_id);
CREATE INDEX idx_participant_archived ON app_conversation_participant(user_id, is_archived) WHERE is_archived = FALSE;
CREATE INDEX idx_participant_pinned ON app_conversation_participant(user_id, is_pinned) WHERE is_pinned = TRUE;

-- Comentários
COMMENT ON TABLE app_conversation_participant IS 'Relacionamento N-N entre conversas e usuários com metadados por participante';
COMMENT ON COLUMN app_conversation_participant.last_read_message_id IS 'Última mensagem lida (para calcular mensagens não lidas)';
COMMENT ON COLUMN app_conversation_participant.is_creator IS 'Criador tem permissões especiais (deletar grupo, promover admins)';


-- ========== TABELA: app_user_message ==========
-- Representa uma mensagem em uma conversa
-- Suporta: texto, imagens, links, vídeos, arquivos e threads (respostas)
CREATE TABLE app_user_message (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    -- Relacionamentos
    conversation_id UUID NOT NULL,
    sender_id UUID NOT NULL,
    parent_message_id UUID,                         -- Mensagem pai (para threads/respostas)

    -- Conteúdo
    txt_content TEXT,                               -- Texto da mensagem ou legenda
    message_type VARCHAR(20) NOT NULL DEFAULT 'TEXT',

    -- Status de leitura
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    read_at TIMESTAMP,

    -- Metadados
    sent_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_edited BOOLEAN NOT NULL DEFAULT FALSE,
    edited_at TIMESTAMP,

    -- Auditoria
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,                           -- Soft delete

    -- Constraints
    CONSTRAINT fk_message_conversation FOREIGN KEY (conversation_id)
        REFERENCES app_conversation(id) ON DELETE CASCADE,
    CONSTRAINT fk_message_sender FOREIGN KEY (sender_id)
        REFERENCES app_user(id) ON DELETE CASCADE,
    CONSTRAINT fk_message_parent FOREIGN KEY (parent_message_id)
        REFERENCES app_user_message(id) ON DELETE SET NULL,
    CONSTRAINT check_message_type CHECK (message_type IN ('TEXT', 'IMAGE', 'LINK', 'VIDEO', 'FILE'))
);

-- Índices para performance
CREATE INDEX idx_message_conversation_sent ON app_user_message(conversation_id, sent_at DESC) WHERE deleted_at IS NULL;
CREATE INDEX idx_message_sender ON app_user_message(sender_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_message_parent ON app_user_message(parent_message_id) WHERE parent_message_id IS NOT NULL AND deleted_at IS NULL;
CREATE INDEX idx_message_read ON app_user_message(conversation_id, is_read) WHERE is_read = FALSE AND deleted_at IS NULL;

-- Índice para busca de texto (GIN para full-text search)
CREATE INDEX idx_message_content_search ON app_user_message USING gin(to_tsvector('english', txt_content)) WHERE deleted_at IS NULL;

-- Comentários
COMMENT ON TABLE app_user_message IS 'Mensagens em conversas (1-1, grupos, canais)';
COMMENT ON COLUMN app_user_message.message_type IS 'TEXT, IMAGE, LINK, VIDEO, FILE';
COMMENT ON COLUMN app_user_message.parent_message_id IS 'Mensagem pai para threads/respostas (null se mensagem original)';
COMMENT ON COLUMN app_user_message.is_read IS 'Status simplificado de leitura (para grupos, considerar apenas 1-1)';
COMMENT ON COLUMN app_user_message.txt_content IS 'Texto da mensagem ou legenda para imagens/vídeos';
