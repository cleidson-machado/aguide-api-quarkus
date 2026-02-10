-- ========================================================================================================
-- INSERINDO 5 TELEFONES PARA O USUÁRIO aa736f39-4f54-4741-a6c4-6d7b0ba6e7cf
-- 3 números brasileiros + 2 números portugueses
-- ========================================================================================================

-- 1. Celular Brasil (Campo Grande/MS) - Principal + WhatsApp + Verificado
INSERT INTO public.phone_numbers (id, user_id, country_code, area_code, number, full_number, type, is_primary, is_verified, has_whatsapp, has_telegram, has_signal, created_at, updated_at, deleted_at)
VALUES ('b1a2c3d4-e5f6-4789-a0b1-c2d3e4f5a6b7'::uuid, 'aa736f39-4f54-4741-a6c4-6d7b0ba6e7cf'::uuid, '+55', '67', '984073221', '+5567984073221', 'MOBILE', true, true, true, false, false, NOW(), NOW(), NULL);

-- 2. Celular Brasil (São Paulo/SP) - WhatsApp + Telegram
INSERT INTO public.phone_numbers (id, user_id, country_code, area_code, number, full_number, type, is_primary, is_verified, has_whatsapp, has_telegram, has_signal, created_at, updated_at, deleted_at)
VALUES ('c2d3e4f5-a6b7-4890-b1c2-d3e4f5a6b7c8'::uuid, 'aa736f39-4f54-4741-a6c4-6d7b0ba6e7cf'::uuid, '+55', '11', '998765432', '+5511998765432', 'MOBILE', false, false, true, true, false, NOW(), NOW(), NULL);

-- 3. Fixo Brasil (Rio de Janeiro/RJ)
INSERT INTO public.phone_numbers (id, user_id, country_code, area_code, number, full_number, type, is_primary, is_verified, has_whatsapp, has_telegram, has_signal, created_at, updated_at, deleted_at)
VALUES ('d3e4f5a6-b7c8-4901-c2d3-e4f5a6b7c8d9'::uuid, 'aa736f39-4f54-4741-a6c4-6d7b0ba6e7cf'::uuid, '+55', '21', '33334444', '+552133334444', 'LANDLINE', false, false, false, false, false, NOW(), NOW(), NULL);

-- 4. Celular Portugal (Lisboa) - WhatsApp + Verificado
INSERT INTO public.phone_numbers (id, user_id, country_code, area_code, number, full_number, type, is_primary, is_verified, has_whatsapp, has_telegram, has_signal, created_at, updated_at, deleted_at)
VALUES ('e4f5a6b7-c8d9-4012-d3e4-f5a6b7c8d9e0'::uuid, 'aa736f39-4f54-4741-a6c4-6d7b0ba6e7cf'::uuid, '+351', NULL, '912345678', '+351912345678', 'MOBILE', false, true, true, false, false, NOW(), NOW(), NULL);

-- 5. Celular Portugal (Porto) - Telegram + Signal
INSERT INTO public.phone_numbers (id, user_id, country_code, area_code, number, full_number, type, is_primary, is_verified, has_whatsapp, has_telegram, has_signal, created_at, updated_at, deleted_at)
VALUES ('f5a6b7c8-d9e0-4123-e4f5-a6b7c8d9e0f1'::uuid, 'aa736f39-4f54-4741-a6c4-6d7b0ba6e7cf'::uuid, '+351', NULL, '936789012', '+351936789012', 'MOBILE', false, false, false, true, true, NOW(), NOW(), NULL);

-- ========================================================================================================
-- QUERY DE VERIFICAÇÃO
-- ========================================================================================================
SELECT
    full_number AS "Número Completo",
    type AS "Tipo",
    CASE WHEN is_primary THEN '⭐ SIM' ELSE '' END AS "Principal",
    CASE WHEN is_verified THEN '✓' ELSE '' END AS "Verificado",
    CASE WHEN has_whatsapp THEN '✓' ELSE '' END AS "WhatsApp",
    CASE WHEN has_telegram THEN '✓' ELSE '' END AS "Telegram",
    CASE WHEN has_signal THEN '✓' ELSE '' END AS "Signal",
    created_at AS "Criado em"
FROM public.phone_numbers
WHERE user_id = 'aa736f39-4f54-4741-a6c4-6d7b0ba6e7cf'::uuid
  AND deleted_at IS NULL
ORDER BY is_primary DESC, created_at;
