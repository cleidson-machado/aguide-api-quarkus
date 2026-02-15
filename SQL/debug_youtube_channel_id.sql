-- ========================================
-- 🔍 DIAGNÓSTICO: YouTube Channel ID
-- ========================================

-- 1. Verificar se a coluna youtube_channel_id existe
SELECT
    column_name,
    data_type,
    character_maximum_length,
    is_nullable
FROM information_schema.columns
WHERE table_name = 'app_user'
  AND column_name LIKE '%youtube%'
ORDER BY column_name;

-- Se retornar:
-- youtube_channel_id   | character varying | 255 | YES  ✅ COLUNA EXISTE!
-- youtube_channel_title | character varying | 255 | YES
-- youtube_user_id      | character varying | 255 | YES

-- Se não retornar youtube_channel_id → PROBLEMA: Coluna não existe!

-- ========================================

-- 2. Verificar quais migrations foram executadas
SELECT
    installed_rank,
    version,
    description,
    type,
    script,
    installed_on,
    success
FROM flyway_schema_history
WHERE description LIKE '%youtube%'
ORDER BY installed_rank DESC;

-- Deve retornar:
-- V1.0.12 | Add youtube fields to users
-- V1.0.13 | Add youtube channel id and resize user id  ← DEVE EXISTIR!

-- Se V1.0.13 não aparecer → Migration não foi executada!

-- ========================================

-- 3. Verificar estrutura completa da tabela app_user
\d app_user;

-- ========================================

-- 4. Verificar dados do usuário (substitua o email)
SELECT
    id,
    name,
    email,
    youtube_user_id,
    youtube_channel_id,     -- ← ESTE DEVE TER VALOR!
    youtube_channel_title,
    created_at
FROM app_user
WHERE email = 'cleidson.mac@gmail.com';  -- Substituir pelo email real

-- Se youtube_channel_id estiver NULL mas os outros campos preenchidos
-- → PROBLEMA DE CÓDIGO, não de banco!

-- ========================================

-- 5. SOLUÇÃO TEMPORÁRIA (se a coluna não existir)
-- Execute isso se a migration V1.0.13 não rodou:

-- ALTER TABLE app_user
-- ADD COLUMN youtube_channel_id VARCHAR(255) NULL;
--
-- ALTER TABLE app_user
-- ALTER COLUMN youtube_user_id TYPE VARCHAR(255);
--
-- CREATE INDEX idx_app_user_youtube_channel_id ON app_user(youtube_channel_id);
--
-- COMMENT ON COLUMN app_user.youtube_channel_id IS 'YouTube Channel ID (formato: UCxxxxx ou UXxxxxx)';

-- ========================================

-- 6. Verificar todos os campos YouTube de todos os usuários
SELECT
    email,
    youtube_user_id,
    youtube_channel_id,
    youtube_channel_title,
    CASE
        WHEN youtube_user_id IS NOT NULL
         AND youtube_channel_id IS NULL THEN '❌ PROBLEMA: User ID existe mas Channel ID está NULL'
        WHEN youtube_user_id IS NOT NULL
         AND youtube_channel_id IS NOT NULL THEN '✅ OK: Ambos preenchidos'
        ELSE '⚪ Nenhum dado YouTube'
    END AS status
FROM app_user
WHERE youtube_user_id IS NOT NULL
   OR youtube_channel_id IS NOT NULL
   OR youtube_channel_title IS NOT NULL
ORDER BY created_at DESC
LIMIT 10;
