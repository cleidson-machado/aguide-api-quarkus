-- usermessage_phase1_seed_two_users.sql
-- Purpose: Generate controlled REST round-1 traffic data for 2 existing users.
-- IMPORTANT:
-- 1) Review and replace placeholders before execution.
-- 2) This script does NOT create users.
-- 3) Execute manually in dev/test only, never against production.

BEGIN;

DO $$
DECLARE
    -- Replace these with the 2 existing test users.
    v_user_a UUID := '00000000-0000-0000-0000-000000000001';
    v_user_b UUID := '00000000-0000-0000-0000-000000000002';

    -- Optional fixed conversation id. Replace if desired.
    v_forced_conversation_id UUID := '00000000-0000-0000-0000-000000000010';

    v_conversation_id UUID;
    v_message_count INT := 150; -- Keep between 120 and 200

    i INT;
    v_sender UUID;
    v_sent_at TIMESTAMP;
    v_content TEXT;
BEGIN
    IF v_user_a = v_user_b THEN
        RAISE EXCEPTION 'USER_A and USER_B must be different';
    END IF;

    IF NOT EXISTS (SELECT 1 FROM app_user u WHERE u.id = v_user_a AND u.deleted_at IS NULL) THEN
        RAISE EXCEPTION 'USER_A does not exist or is deleted: %', v_user_a;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM app_user u WHERE u.id = v_user_b AND u.deleted_at IS NULL) THEN
        RAISE EXCEPTION 'USER_B does not exist or is deleted: %', v_user_b;
    END IF;

    -- Try to reuse an existing active DIRECT conversation with exactly these 2 active participants.
    SELECT c.id
      INTO v_conversation_id
      FROM app_conversation c
     WHERE c.deleted_at IS NULL
       AND c.conversation_type = 'DIRECT'
       AND EXISTS (
            SELECT 1
              FROM app_conversation_participant p1
             WHERE p1.conversation_id = c.id
               AND p1.user_id = v_user_a
               AND p1.left_at IS NULL)
       AND EXISTS (
            SELECT 1
              FROM app_conversation_participant p2
             WHERE p2.conversation_id = c.id
               AND p2.user_id = v_user_b
               AND p2.left_at IS NULL)
       AND 2 = (
            SELECT COUNT(*)
              FROM app_conversation_participant px
             WHERE px.conversation_id = c.id
               AND px.left_at IS NULL)
     LIMIT 1;

    -- If none exists, create one and attach both users.
    IF v_conversation_id IS NULL THEN
        v_conversation_id := v_forced_conversation_id;

        INSERT INTO app_conversation (
            id,
            name,
            description,
            icon_url,
            conversation_type,
            last_message_at,
            is_archived,
            is_pinned,
            created_at,
            updated_at,
            deleted_at
        ) VALUES (
            v_conversation_id,
            NULL,
            NULL,
            NULL,
            'DIRECT',
            NULL,
            FALSE,
            FALSE,
            NOW(),
            NOW(),
            NULL
        ) ON CONFLICT (id) DO NOTHING;

        INSERT INTO app_conversation_participant (
            id,
            conversation_id,
            user_id,
            last_read_message_id,
            last_read_at,
            is_admin,
            is_creator,
            is_archived,
            is_pinned,
            is_muted,
            joined_at,
            left_at
        ) VALUES
        (
            gen_random_uuid(),
            v_conversation_id,
            v_user_a,
            NULL,
            NULL,
            FALSE,
            FALSE,
            FALSE,
            FALSE,
            FALSE,
            NOW(),
            NULL
        ),
        (
            gen_random_uuid(),
            v_conversation_id,
            v_user_b,
            NULL,
            NULL,
            FALSE,
            FALSE,
            FALSE,
            FALSE,
            FALSE,
            NOW(),
            NULL
        ) ON CONFLICT DO NOTHING;
    END IF;

    -- Generate messages in 3 temporal blocks:
    -- Block A (recent): last 24h -> 80 messages
    -- Block B (intermediate): last 7 days -> 45 messages
    -- Block C (older): last 30 days -> 25 messages
    -- Total: 150
    FOR i IN 1..v_message_count LOOP
        IF (i % 2) = 0 THEN
            v_sender := v_user_a;
        ELSE
            v_sender := v_user_b;
        END IF;

        IF i <= 80 THEN
            -- Last 24h
            v_sent_at := NOW() - INTERVAL '24 hours' + ((i * 1080) || ' seconds')::INTERVAL;
        ELSIF i <= 125 THEN
            -- Last 7 days
            v_sent_at := NOW() - INTERVAL '7 days' + (((i - 80) * 12096) || ' seconds')::INTERVAL;
        ELSE
            -- Last 30 days
            v_sent_at := NOW() - INTERVAL '30 days' + (((i - 125) * 103680) || ' seconds')::INTERVAL;
        END IF;

        v_content := CASE (i % 12)
            WHEN 0 THEN 'Bom dia! Tudo certo por ai?' || ' [seed-' || i || ']'
            WHEN 1 THEN 'O app abriu normal no meu celular.' || ' [seed-' || i || ']'
            WHEN 2 THEN 'Vou testar a conversa com mais volume.' || ' [seed-' || i || ']'
            WHEN 3 THEN 'Perfeito, manda o resultado depois.' || ' [seed-' || i || ']'
            WHEN 4 THEN 'Conferi a inbox e a ordenacao parece ok.' || ' [seed-' || i || ']'
            WHEN 5 THEN 'Agora vou validar a paginacao no chat.' || ' [seed-' || i || ']'
            WHEN 6 THEN 'Recebi, vou seguir com os testes manuais.' || ' [seed-' || i || ']'
            WHEN 7 THEN 'Vamos comparar os timestamps no frontend.' || ' [seed-' || i || ']'
            WHEN 8 THEN 'A busca por texto retornou itens esperados.' || ' [seed-' || i || ']'
            WHEN 9 THEN 'Marquei algumas mensagens como lidas.' || ' [seed-' || i || ']'
            WHEN 10 THEN 'Vou enviar mais algumas para fechar volume.' || ' [seed-' || i || ']'
            ELSE 'Fechado, seguimos para a proxima validacao.' || ' [seed-' || i || ']'
        END;

        INSERT INTO app_user_message (
            id,
            conversation_id,
            sender_id,
            txt_content,
            message_type,
            is_read,
            read_at,
            parent_message_id,
            is_edited,
            edited_at,
            sent_at,
            created_at,
            updated_at,
            deleted_at
        ) VALUES (
            gen_random_uuid(),
            v_conversation_id,
            v_sender,
            v_content,
            'TEXT',
            (random() < 0.70),
            CASE WHEN random() < 0.70 THEN v_sent_at + INTERVAL '2 minutes' ELSE NULL END,
            NULL,
            FALSE,
            NULL,
            v_sent_at,
            v_sent_at,
            v_sent_at,
            NULL
        );
    END LOOP;

    -- Keep conversation last_message_at aligned with newest message.
    UPDATE app_conversation c
       SET last_message_at = (
           SELECT MAX(m.sent_at)
             FROM app_user_message m
            WHERE m.conversation_id = c.id
              AND m.deleted_at IS NULL
       ),
           updated_at = NOW()
     WHERE c.id = v_conversation_id;

    RAISE NOTICE 'SEED DONE | conversationId=% | messages=% | users=(%,%)',
        v_conversation_id, v_message_count, v_user_a, v_user_b;
END $$;

COMMIT;

-- Validation queries (run manually after execution):
-- 1) Total and temporal window
-- SELECT
--   COUNT(*) AS total_messages,
--   MIN(sent_at) AS first_message_at,
--   MAX(sent_at) AS last_message_at
-- FROM app_user_message
-- WHERE conversation_id = '<conversation-id>'::uuid
--   AND deleted_at IS NULL;

-- 2) Read vs unread split
-- SELECT
--   SUM(CASE WHEN is_read THEN 1 ELSE 0 END) AS read_count,
--   SUM(CASE WHEN NOT is_read THEN 1 ELSE 0 END) AS unread_count
-- FROM app_user_message
-- WHERE conversation_id = '<conversation-id>'::uuid
--   AND deleted_at IS NULL;

-- 3) Pagination smoke check (page=0, size=20 equivalent)
-- SELECT id, sender_id, txt_content, sent_at
-- FROM app_user_message
-- WHERE conversation_id = '<conversation-id>'::uuid
--   AND deleted_at IS NULL
-- ORDER BY sent_at DESC
-- LIMIT 20 OFFSET 0;
