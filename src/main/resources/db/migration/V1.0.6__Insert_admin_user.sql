-- =============================================
-- Insere Usuário Administrador Padrão
-- Versão: 1.0.6
-- Data: 2026-02-04
-- =============================================
--
-- Este script cria o usuário administrador principal
-- do sistema com as seguintes credenciais:
--
-- Email: contato@aguide.space
-- Nome: protouser
-- Senha: Kabala1975 (hash BCrypt abaixo)
-- Role: ADMIN
--
-- ⚠️ IMPORTANTE:
-- - Este script usa INSERT ... ON CONFLICT DO NOTHING
-- - Se o usuário já existir, não faz nada (idempotente)
-- - Seguro para executar múltiplas vezes
-- - A senha está com hash BCrypt (cost 10)
--
-- 🔐 Hash gerado com BCrypt:
-- Senha: Kabala1975
-- Hash: $2a$10$XbKDPVvF8UJk5xJ6vN5YUe7bZqP1gKJhGj5gHLQzW8vF5Rn3GHmKW
-- =============================================

-- Insere usuário administrador (idempotente)
INSERT INTO app_user (
    id,
    name,
    surname,
    email,
    password_hash,
    role,
    created_at,
    updated_at,
    deleted_at,
    oauth_provider,
    oauth_id
) VALUES (
    gen_random_uuid(),
    'protouser',
    'Admin',
    'contato@aguide.space',
    '$2a$10$XbKDPVvF8UJk5xJ6vN5YUe7bZqP1gKJhGj5gHLQzW8vF5Rn3GHmKW', -- Kabala1975
    'ADMIN',
    NOW(),
    NOW(),
    NULL,
    NULL,
    NULL
)
ON CONFLICT (email) DO NOTHING;

-- Comentário de auditoria
COMMENT ON TABLE app_user IS 'Tabela de usuários do sistema. Gerenciada via Flyway migrations.';
