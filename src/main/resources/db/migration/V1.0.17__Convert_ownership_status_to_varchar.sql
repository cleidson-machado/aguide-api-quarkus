-- =========================================================
-- Migration: Convert ownership_status from ENUM to VARCHAR
-- Version: V1.0.17
-- Author: System
-- Date: 2026-02-19
-- Description: Converte coluna ownership_status de tipo ENUM customizado
--              para VARCHAR para compatibilidade com Hibernate/JPA
-- =========================================================

-- Passo 1: Remover o DEFAULT (que depende do tipo ENUM)
ALTER TABLE content_ownership
    ALTER COLUMN ownership_status DROP DEFAULT;

-- Passo 2: Alterar coluna para VARCHAR usando CAST
ALTER TABLE content_ownership
    ALTER COLUMN ownership_status TYPE VARCHAR(20)
    USING ownership_status::VARCHAR;

-- Passo 3: Restaurar DEFAULT como VARCHAR
ALTER TABLE content_ownership
    ALTER COLUMN ownership_status SET DEFAULT 'PENDING';

-- Passo 4: Adicionar constraint de validação (garante apenas valores válidos)
ALTER TABLE content_ownership
    ADD CONSTRAINT chk_ownership_status
    CHECK (ownership_status IN ('PENDING', 'VERIFIED', 'REJECTED'));

-- Passo 5: Agora podemos dropar o tipo ENUM (não há mais dependências)
DROP TYPE IF EXISTS ownership_status;

-- =========================================================
-- Notas:
-- - VARCHAR(20) é suficiente para 'PENDING', 'VERIFIED', 'REJECTED'
-- - CHECK constraint garante apenas valores válidos
-- - DEFAULT foi replicado como VARCHAR
