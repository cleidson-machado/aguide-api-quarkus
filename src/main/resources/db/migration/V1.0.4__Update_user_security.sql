-- ========================================
-- ATUALIZAÇÃO DE SEGURANÇA - TABELA app_user
-- Versão: 1.0.4
-- Descrição: Adiciona campos de segurança JWT e OAuth2
-- ========================================

-- ========== RENOMEIA COLUNA passwd PARA password_hash ==========
ALTER TABLE app_user
RENAME COLUMN passwd TO password_hash;

-- ========== ADICIONA CAMPOS DE SEGURANÇA ==========

-- Role do usuário (USER, ADMIN, etc)
ALTER TABLE app_user
ADD COLUMN IF NOT EXISTS role VARCHAR(20) DEFAULT 'USER';

-- Timestamps de auditoria
ALTER TABLE app_user
ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT NOW();

ALTER TABLE app_user
ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT NOW();

-- ========== ADICIONA CAMPOS PARA OAuth2 (Fase 2 - Futuro) ==========

-- Provedor OAuth2 (google, microsoft, linkedin)
ALTER TABLE app_user
ADD COLUMN IF NOT EXISTS oauth_provider VARCHAR(50);

-- ID do usuário no provedor OAuth2
ALTER TABLE app_user
ADD COLUMN IF NOT EXISTS oauth_id VARCHAR(255);

-- ========== ÍNDICES PARA PERFORMANCE ==========

-- Índice para busca por provedor OAuth2
CREATE INDEX IF NOT EXISTS idx_app_user_oauth
ON app_user(oauth_provider, oauth_id);

-- ========== ATUALIZA CONSTRAINT DE EMAIL ==========

-- Garante que email seja único e não nulo
ALTER TABLE app_user
ALTER COLUMN email SET NOT NULL;

-- ========== ATUALIZA CONSTRAINTS DE CAMPOS ==========

-- password_hash pode ser NULL para usuários OAuth2
ALTER TABLE app_user
ALTER COLUMN password_hash DROP NOT NULL;

-- ========== COMENTÁRIOS PARA DOCUMENTAÇÃO ==========

COMMENT ON COLUMN app_user.password_hash IS
'Hash BCrypt da senha do usuário. NULL para usuários OAuth2.';

COMMENT ON COLUMN app_user.role IS
'Role do usuário para controle de acesso (USER, ADMIN, etc).';

COMMENT ON COLUMN app_user.oauth_provider IS
'Provedor OAuth2 (google, microsoft, linkedin). NULL para autenticação local.';

COMMENT ON COLUMN app_user.oauth_id IS
'ID do usuário no provedor OAuth2. NULL para autenticação local.';

COMMENT ON COLUMN app_user.created_at IS
'Data e hora de criação do registro.';

COMMENT ON COLUMN app_user.updated_at IS
'Data e hora da última atualização do registro.';

-- ========== DADOS DE TESTE (OPCIONAL - REMOVER EM PRODUÇÃO) ==========

-- Atualiza usuários existentes com role padrão
UPDATE app_user
SET role = 'USER'
WHERE role IS NULL;

-- Atualiza timestamps de usuários existentes
UPDATE app_user
SET created_at = NOW(), updated_at = NOW()
WHERE created_at IS NULL;
