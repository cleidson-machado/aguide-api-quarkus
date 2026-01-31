-- ========================================
-- QUERIES ÚTEIS - content_record TABLE
-- ========================================

-- ----------------------------------------
-- 1. SELECTS BÁSICOS
-- ----------------------------------------

-- 1.1 Listar TODAS as colunas da tabela content_record
-- Use quando precisar ver todos os dados completos
SELECT id, 
	title, 
	description, 
	url, 
	thumbnail_url, 
	channel_name, 
	content_type, 
	category_id, 
	category_name, 
	tags, 
	duration_seconds, 
	duration_iso, 
	definition, 
	caption, 
	view_count, 
	like_count, 
	comment_count, 
	default_language, 
	default_audio_language, 
	created_at, 
	updated_at
FROM public.content_record;

-- 1.2 Listar campos principais ordenados por ID (ordem de criação)
-- Útil para visualização simplificada em ordem cronológica de inserção
SELECT
	created_at,
	id, 
	title, 
	category_name,
	url, 
	thumbnail_url 
FROM public.content_record
ORDER BY id ASC;

-- 1.3 Listar TODOS os registros com todas as colunas
-- Atenção: Pode retornar muitos dados! Use com cuidado
SELECT * FROM public.content_record
ORDER BY id ASC;

-- 1.4 Listar campos essenciais ordenados por ID
-- Versão mais limpa para análise rápida
SELECT 
	id, 
	title, 
	category_name,
	url, 
	thumbnail_url 
FROM public.content_record
ORDER BY id ASC;

-- 1.5 Listar campos essenciais ordenados por data de criação (mais recentes primeiro)
-- Use para ver os conteúdos mais novos no topo
SELECT 
	id,
	created_at,
	title, 
	category_name,
	url, 
	thumbnail_url 
FROM public.content_record
ORDER BY created_at DESC;


-- ----------------------------------------
-- 2. FILTROS POR RANGE DE DATAS
-- ----------------------------------------

-- 2.1 Listar NOVOS registros (últimas 24 horas)
-- Retorna apenas conteúdos criados no último dia
SELECT 
	id,
	created_at,
	title, 
	category_name,
	url, 
	thumbnail_url 
FROM public.content_record
WHERE created_at > now() - interval '1 day'
ORDER BY created_at DESC;

-- 2.2 Listar registros ANTIGOS (mais de 2 dias)
-- Retorna conteúdos com mais de 2 dias de existência
SELECT 
	id,
	created_at,
	title, 
	category_name,
	url, 
	thumbnail_url 
FROM public.content_record
WHERE created_at <= now() - interval '2 day'
ORDER BY created_at DESC;

-- 2.3 Listar registros dos últimos 7 dias
-- Útil para relatórios semanais
SELECT 
	id,
	created_at,
	title, 
	category_name,
	url, 
	thumbnail_url 
FROM public.content_record
WHERE created_at > now() - interval '7 days'
ORDER BY created_at DESC;

-- 2.4 Listar registros dos últimos 30 dias
-- Útil para relatórios mensais
SELECT 
	id,
	created_at,
	title, 
	category_name,
	url, 
	thumbnail_url 
FROM public.content_record
WHERE created_at > now() - interval '30 days'
ORDER BY created_at DESC;

-- 2.5 Listar registros em período específico (entre duas datas)
-- Exemplo: Janeiro de 2026
SELECT 
	id,
	created_at,
	title, 
	category_name,
	url, 
	thumbnail_url 
FROM public.content_record
WHERE created_at BETWEEN '2026-01-01' AND '2026-01-31'
ORDER BY created_at DESC;


-- ----------------------------------------
-- 3. DELETE - LIMPEZA DE DADOS ANTIGOS
-- ⚠️ CUIDADO: SEMPRE TESTE COM SELECT ANTES!
-- ----------------------------------------

-- 3.1 VERIFICAR quantos registros serão deletados (SEMPRE EXECUTE PRIMEIRO!)
-- Esta query é ESSENCIAL antes de qualquer DELETE
SELECT COUNT(*) as total_to_delete
FROM public.content_record
WHERE created_at <= now() - interval '2 day';

-- 3.2 VISUALIZAR quais registros serão deletados
-- Use para conferir se está deletando os registros corretos
SELECT 
	id,
	created_at,
	title,
	category_name
FROM public.content_record
WHERE created_at <= now() - interval '2 day'
ORDER BY created_at DESC;

-- 3.3 DELETE de registros com mais de 2 dias
-- ⚠️ ATENÇÃO: Esta ação é IRREVERSÍVEL! Faça backup antes!
DELETE FROM public.content_record
WHERE created_at <= now() - interval '2 day';

-- 3.4 DELETE de registros com mais de 7 dias
-- Útil para limpeza semanal automática
DELETE FROM public.content_record
WHERE created_at <= now() - interval '7 days';

-- 3.5 DELETE de registros com mais de 30 dias
-- Útil para limpeza mensal
DELETE FROM public.content_record
WHERE created_at <= now() - interval '30 days';

-- 3.6 DELETE com RETURNING (ver o que foi deletado)
-- Retorna os registros deletados para conferência
DELETE FROM public.content_record
WHERE created_at <= now() - interval '2 day'
RETURNING id, created_at, title, category_name;

-- 3.7 DELETE em lotes pequenos (para grandes volumes)
-- Evita travamento do banco em tabelas muito grandes
-- Execute múltiplas vezes até retornar 0 linhas afetadas
DELETE FROM public.content_record
WHERE id IN (
    SELECT id 
    FROM public.content_record
    WHERE created_at <= now() - interval '2 day'
    LIMIT 1000
);


-- ----------------------------------------
-- 4. QUERIES DE ANÁLISE E ESTATÍSTICAS
-- ----------------------------------------

-- 4.1 Contar total de registros
SELECT COUNT(*) as total_records
FROM public.content_record;

-- 4.2 Contar registros por categoria
SELECT 
	category_name,
	COUNT(*) as total
FROM public.content_record
GROUP BY category_name
ORDER BY total DESC;

-- 4.3 Contar registros novos vs antigos
SELECT 
	CASE 
		WHEN created_at > now() - interval '1 day' THEN 'Novos (24h)'
		WHEN created_at > now() - interval '7 days' THEN 'Recentes (7 dias)'
		ELSE 'Antigos (7+ dias)'
	END as periodo,
	COUNT(*) as total
FROM public.content_record
GROUP BY periodo
ORDER BY total DESC;

-- 4.4 Registros mais antigos e mais novos
SELECT 
	'Mais antigo' as tipo,
	id,
	created_at,
	title
FROM public.content_record
ORDER BY created_at ASC
LIMIT 1

UNION ALL

SELECT 
	'Mais recente' as tipo,
	id,
	created_at,
	title
FROM public.content_record
ORDER BY created_at DESC
LIMIT 1;


-- ----------------------------------------
-- DICAS DE USO:
-- ----------------------------------------
-- • Sempre teste com SELECT COUNT(*) antes de DELETE
-- • Faça backup antes de executar DELETE em produção
-- • Use LIMIT para testes: "SELECT ... LIMIT 10"
-- • Para ver execução: "EXPLAIN ANALYZE SELECT ..."
-- • Ajuste os intervalos conforme sua necessidade
-- ----------------------------------------