#!/bin/bash
# ========================================================
# 🏥 VALIDAÇÃO DE SAÚDE DA BRANCH develop-data-objects
# ========================================================
# Este script verifica se a branch está pronta para:
# 1. Rodar no Jenkins (Jenkinsfile.test)
# 2. Fazer PR para main
# ========================================================

set -e  # Para na primeira falha

echo "================================================"
echo "🏥 VALIDAÇÃO DE SAÚDE DA BRANCH"
echo "================================================"
echo "📅 Data: $(date '+%Y-%m-%d %H:%M:%S')"
echo "🌿 Branch: $(git branch --show-current)"
echo "================================================"

# ========== 1. VERIFICAR BRANCH ATUAL ==========
echo ""
echo "🔍 [1/8] Verificando branch atual..."
CURRENT_BRANCH=$(git branch --show-current)
if [[ "$CURRENT_BRANCH" != "develop-data-objects" ]]; then
    echo "❌ ERRO: Branch atual é '$CURRENT_BRANCH', esperado 'develop-data-objects'"
    exit 1
fi
echo "✅ Branch correta: $CURRENT_BRANCH"

# ========== 2. VERIFICAR CONFIGURAÇÕES DE PRODUÇÃO ==========
echo ""
echo "🔍 [2/8] Verificando application-prod.properties..."

# Verifica quarkus.flyway.clean-at-start=false
if ! grep -q "^quarkus.flyway.clean-at-start=false" src/main/resources/application-prod.properties; then
    echo "❌ ERRO: quarkus.flyway.clean-at-start deve ser 'false' em produção!"
    exit 1
fi
echo "   ✅ quarkus.flyway.clean-at-start=false"

# Verifica quarkus.hibernate-orm.database.generation=none
if ! grep -q "^quarkus.hibernate-orm.database.generation=none" src/main/resources/application-prod.properties; then
    echo "❌ ERRO: quarkus.hibernate-orm.database.generation deve ser 'none' em produção!"
    exit 1
fi
echo "   ✅ quarkus.hibernate-orm.database.generation=none"

echo "✅ Configurações de produção SEGURAS"

# ========== 3. VERIFICAR VARIÁVEIS JWT DESNECESSÁRIAS ==========
echo ""
echo "🔍 [3/8] Verificando configurações JWT..."

# Verifica se JWT_SIGN_KEY_CONTENT foi removido
if grep -q "JWT_SIGN_KEY_CONTENT" src/main/resources/application.properties; then
    echo "❌ ERRO: JWT_SIGN_KEY_CONTENT ainda está presente em application.properties!"
    echo "   Esta variável não é necessária e causa problemas no Jenkins."
    exit 1
fi
echo "   ✅ JWT_SIGN_KEY_CONTENT removido"

# Verifica se as chaves JWT existem localmente
if [[ ! -f security/jwt-private.pem ]]; then
    echo "⚠️  AVISO: security/jwt-private.pem NÃO encontrado (ok para Jenkins)"
else
    echo "   ✅ security/jwt-private.pem existe"
fi

if [[ ! -f security/jwt-public.pem ]]; then
    echo "⚠️  AVISO: security/jwt-public.pem NÃO encontrado (ok para Jenkins)"
else
    echo "   ✅ security/jwt-public.pem existe"
fi

echo "✅ Configurações JWT corretas"

# ========== 4. VERIFICAR CONFIGURAÇÕES DE TESTE ==========
echo ""
echo "🔍 [4/8] Verificando src/test/resources/application.properties..."

# Verifica se AuthenticationFilter está desabilitado
if ! grep -q "quarkus.arc.exclude-types=br.com.aguideptbr.features.auth.AuthenticationFilter" src/test/resources/application.properties; then
    echo "❌ ERRO: AuthenticationFilter deve estar desabilitado em testes!"
    exit 1
fi
echo "   ✅ AuthenticationFilter desabilitado em testes"

# Verifica se JWT está desabilitado em testes
if ! grep -q "quarkus.smallrye-jwt.enabled=false" src/test/resources/application.properties; then
    echo "❌ ERRO: SmallRye JWT deve estar desabilitado em testes!"
    exit 1
fi
echo "   ✅ SmallRye JWT desabilitado em testes"

# Verifica se está usando quarkus_test
if ! grep -q "quarkus_test" src/test/resources/application.properties; then
    echo "❌ ERRO: Database de teste (quarkus_test) não configurado!"
    exit 1
fi
echo "   ✅ Database de teste (quarkus_test) configurado"

# Verifica Flyway clean-at-start em testes
if ! grep -q "quarkus.flyway.clean-at-start=true" src/test/resources/application.properties; then
    echo "❌ ERRO: Flyway clean-at-start deve ser 'true' em testes!"
    exit 1
fi
echo "   ✅ Flyway clean-at-start=true em testes"

echo "✅ Configurações de teste corretas"

# ========== 5. VERIFICAR MIGRATIONS ==========
echo ""
echo "🔍 [5/8] Verificando migrations..."

# Conta migrations
MIGRATION_COUNT=$(ls -1 src/main/resources/db/migration/*.sql 2>/dev/null | wc -l | xargs)
if [[ "$MIGRATION_COUNT" -eq 0 ]]; then
    echo "❌ ERRO: Nenhuma migration encontrada!"
    exit 1
fi
echo "   ✅ $MIGRATION_COUNT migrations encontradas"

# Verifica migrations destrutivas
DESTRUCTIVE_MIGRATIONS=$(grep -r "DROP TABLE\|TRUNCATE\|DROP SCHEMA" src/main/resources/db/migration/*.sql 2>/dev/null | wc -l | xargs)
if [[ "$DESTRUCTIVE_MIGRATIONS" -gt 0 ]]; then
    echo "⚠️  AVISO: $DESTRUCTIVE_MIGRATIONS migration(s) com comandos destrutivos encontrada(s)!"
    echo "   Verifique se são realmente necessárias para produção."
    grep -n "DROP TABLE\|TRUNCATE\|DROP SCHEMA" src/main/resources/db/migration/*.sql 2>/dev/null
else
    echo "   ✅ Nenhuma migration destrutiva encontrada"
fi

echo "✅ Migrations verificadas"

# ========== 6. VERIFICAR GIT STATUS ==========
echo ""
echo "🔍 [6/8] Verificando estado do Git..."

# Verifica se há modificações não commitadas
if [[ -n $(git status --porcelain) ]]; then
    echo "⚠️  AVISO: Há modificações não commitadas:"
    git status --short
    echo ""
    echo "   Sugestão: Commit suas alterações antes de fazer PR"
else
    echo "   ✅ Nenhuma modificação não commitada"
fi

# Verifica se está sincronizado com remote
git fetch origin develop-data-objects 2>/dev/null || true
LOCAL_COMMIT=$(git rev-parse HEAD)
REMOTE_COMMIT=$(git rev-parse origin/develop-data-objects 2>/dev/null || echo "unknown")

if [[ "$LOCAL_COMMIT" != "$REMOTE_COMMIT" ]]; then
    echo "⚠️  AVISO: Branch local DIFERENTE do remote"
    echo "   Local:  $LOCAL_COMMIT"
    echo "   Remote: $REMOTE_COMMIT"
    echo "   Sugestão: git push origin develop-data-objects"
else
    echo "   ✅ Branch sincronizada com remote"
fi

echo "✅ Estado do Git verificado"

# ========== 7. COMPILAÇÃO ==========
echo ""
echo "🔍 [7/8] Testando compilação..."
echo "   Executando: ./mvnw clean compile -DskipTests"
echo ""

if ./mvnw clean compile -DskipTests -q; then
    echo "✅ Compilação bem-sucedida"
else
    echo "❌ ERRO: Compilação falhou!"
    exit 1
fi

# ========== 8. VERIFICAR DOCKER COMPOSE (se existir) ==========
echo ""
echo "🔍 [8/8] Verificando docker-compose.yml..."

if [[ -f docker-compose.yml ]]; then
    # Verifica se está usando QUARKUS_PROFILE=prod
    if grep -q "QUARKUS_PROFILE.*prod" docker-compose.yml; then
        echo "   ✅ QUARKUS_PROFILE=prod configurado"
    else
        echo "⚠️  AVISO: QUARKUS_PROFILE pode não estar configurado como 'prod'"
    fi
else
    echo "   ℹ️  docker-compose.yml não encontrado (ok para desenvolvimento)"
fi

echo "✅ Docker Compose verificado"

# ========== RESUMO FINAL ==========
echo ""
echo "================================================"
echo "✅ BRANCH SAUDÁVEL E PRONTA!"
echo "================================================"
echo "📋 Todas as verificações passaram:"
echo "   ✅ Branch: develop-data-objects"
echo "   ✅ Configurações de produção: SEGURAS"
echo "   ✅ Configurações de teste: CORRETAS"
echo "   ✅ Migrations: VALIDADAS"
echo "   ✅ JWT: CONFIGURADO CORRETAMENTE"
echo "   ✅ Compilação: SUCESSO"
echo ""
echo "🚀 Próximos passos:"
echo "   1. Executar testes: ./mvnw test"
echo "   2. Push para GitHub: git push origin develop-data-objects"
echo "   3. Jenkins executará Jenkinsfile.test automaticamente"
echo "   4. Se testes passarem, criar PR: develop-data-objects → main"
echo "================================================"

exit 0
