-- =============================================
-- Insere Usuário Administrador Padrão
-- Versão: 1.0.6
-- Data: 2026-02-04
-- =============================================
--
-- ⭐ USUÁRIO "CORINGA" PARA TESTES PRÉ-PRODUÇÃO
--
-- Este script cria o único usuário de teste do sistema.
-- Será mantido até o projeto ir definitivamente para produção.
--
-- Credenciais de Acesso:
-- Email: contato@aguide.space
-- Nome: protouser
-- Senha: admin123 (hash BCrypt abaixo)
-- Role: ADMIN
--
-- ⚠️ IMPORTANTE:
-- - Este script usa INSERT ... ON CONFLICT DO NOTHING
-- - Se o usuário já existir, não faz nada (idempotente)
-- - Seguro para executar múltiplas vezes
-- - A senha está com hash BCrypt (cost 10)
-- - Substitui os usuários fake da V1.0.1 (João, Maria, Pedro)
--
-- 🔐 Hash gerado com BCrypt:
-- Senha: admin123
-- Hash: $2a$10$1b.v1jTmdr.c1XJXM10bsO.YwcpgZkXszAivtIL6VgfUQF2RhMIBy
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
    '$2a$10$1b.v1jTmdr.c1XJXM10bsO.YwcpgZkXszAivtIL6VgfUQF2RhMIBy', -- admin123
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
