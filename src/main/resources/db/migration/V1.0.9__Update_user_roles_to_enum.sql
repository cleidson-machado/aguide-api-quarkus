-- V1.0.9__Update_user_roles_to_enum.sql
--
-- Atualiza a coluna 'role' da tabela app_user para usar os novos valores de Enum.
-- Mapeia os valores antigos para os novos:
-- 'USER' -> 'FREE' (usuário gratuito padrão)
-- 'ADMIN' -> 'ADMIN' (mantém)
-- Adiciona constraint CHECK para garantir apenas valores válidos.

-- 1. Atualiza valores existentes
UPDATE app_user
SET role = 'FREE'
WHERE role = 'USER';

-- 2. Adiciona constraint CHECK para validar apenas valores do Enum
ALTER TABLE app_user
ADD CONSTRAINT check_user_role
CHECK (role IN ('ADMIN', 'MANAGER', 'CHANNEL_OWNER', 'PREMIUM_USER', 'FREE'));

-- 3. Adiciona comentário na coluna
COMMENT ON COLUMN app_user.role IS 'Role do usuário: ADMIN (admin total), MANAGER (gerente), CHANNEL_OWNER (usuário pagante com canal), PREMIUM_USER (usuário pagante), FREE (usuário gratuito)';
