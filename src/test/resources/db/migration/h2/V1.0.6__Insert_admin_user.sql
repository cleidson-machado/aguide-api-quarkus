-- =============================================
-- Insere Usuário Administrador Padrão (H2)
-- Versão: 1.0.6
-- Data: 2026-02-04
-- =============================================

-- H2 não suporta ON CONFLICT, então usamos MERGE
MERGE INTO app_user (
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
) KEY(email) VALUES (
    RANDOM_UUID(),
    'protouser',
    'Admin',
    'contato@aguide.space',
    '$2a$10$XbKDPVvF8UJk5xJ6vN5YUe7bZqP1gKJhGj5gHLQzW8vF5Rn3GHmKW',
    'ADMIN',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    NULL,
    NULL,
    NULL
);
