#!/bin/bash
# ========================================
# Script de Validação Pré-Deploy
# ========================================
# Verifica se todas as configurações estão
# seguras para produção antes de fazer deploy
#
# Uso: ./validate-production-safety.sh
# ========================================

set -e # Para no primeiro erro

echo "🔍 =========================================="
echo "🔍  VALIDAÇÃO DE SEGURANÇA - PRODUÇÃO"
echo "🔍 =========================================="
echo ""

# Cores para output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

ERRORS=0

# ====================================
# 1. Verificar application-prod.properties
# ====================================
echo "📋 [1/6] Verificando application-prod.properties..."

if ! grep -q "quarkus.flyway.clean-at-start=false" src/main/resources/application-prod.properties; then
    echo -e "${RED}❌ ERRO: clean-at-start não está como false em produção!${NC}"
    ERRORS=$((ERRORS + 1))
else
    echo -e "${GREEN}✅ clean-at-start=false (correto)${NC}"
fi

if ! grep -q "quarkus.hibernate-orm.database.generation=none" src/main/resources/application-prod.properties; then
    echo -e "${RED}❌ ERRO: database.generation não está como 'none' em produção!${NC}"
    ERRORS=$((ERRORS + 1))
else
    echo -e "${GREEN}✅ database.generation=none (correto)${NC}"
fi

echo ""

# ====================================
# 2. Verificar migrations perigosas
# ====================================
echo "📋 [2/6] Verificando migrations por comandos destrutivos..."

DANGEROUS_PATTERNS=("DROP TABLE" "TRUNCATE" "DROP SCHEMA" "DELETE FROM app_user" "DELETE FROM content_record")

for pattern in "${DANGEROUS_PATTERNS[@]}"; do
    if grep -r "$pattern" src/main/resources/db/migration/*.sql 2>/dev/null; then
        echo -e "${RED}❌ ERRO: Encontrado '$pattern' nas migrations!${NC}"
        ERRORS=$((ERRORS + 1))
    fi
done

if [ $ERRORS -eq 0 ]; then
    echo -e "${GREEN}✅ Nenhum comando destrutivo encontrado${NC}"
fi

echo ""

# ====================================
# 3. Verificar se import.sql está comentado
# ====================================
echo "📋 [3/6] Verificando se import.sql está desativado..."

if grep -q "^INSERT INTO" src/main/resources/import.sql 2>/dev/null; then
    echo -e "${RED}❌ ERRO: import.sql ainda tem INSERTs ativos!${NC}"
    ERRORS=$((ERRORS + 1))
else
    echo -e "${GREEN}✅ import.sql está comentado/desativado${NC}"
fi

echo ""

# ====================================
# 4. Verificar se V1.0.6 existe
# ====================================
echo "📋 [4/6] Verificando migration do usuário admin..."

if [ ! -f "src/main/resources/db/migration/V1.0.6__Insert_admin_user.sql" ]; then
    echo -e "${RED}❌ ERRO: Migration V1.0.6 não encontrada!${NC}"
    ERRORS=$((ERRORS + 1))
else
    echo -e "${GREEN}✅ V1.0.6__Insert_admin_user.sql existe${NC}"

    if grep -q "ON CONFLICT (email) DO NOTHING" src/main/resources/db/migration/V1.0.6__Insert_admin_user.sql; then
        echo -e "${GREEN}✅ Migration é idempotente (usa ON CONFLICT)${NC}"
    else
        echo -e "${YELLOW}⚠️  ATENÇÃO: Migration pode não ser idempotente${NC}"
    fi
fi

echo ""

# ====================================
# 5. Verificar compilação
# ====================================
echo "📋 [5/6] Verificando se o projeto compila..."

if ./mvnw clean compile -q -DskipTests > /dev/null 2>&1; then
    echo -e "${GREEN}✅ Projeto compila sem erros${NC}"
else
    echo -e "${RED}❌ ERRO: Projeto não compila!${NC}"
    echo "Execute: ./mvnw clean compile"
    ERRORS=$((ERRORS + 1))
fi

echo ""

# ====================================
# 6. Verificar estrutura de security/
# ====================================
echo "📋 [6/6] Verificando chaves JWT..."

if [ -f "security/jwt-private.pem" ] && [ -f "security/jwt-public.pem" ]; then
    echo -e "${GREEN}✅ Chaves JWT existem${NC}"

    # Verificar permissões
    PRIVATE_PERMS=$(stat -f "%A" security/jwt-private.pem 2>/dev/null || stat -c "%a" security/jwt-private.pem 2>/dev/null)
    if [ "$PRIVATE_PERMS" = "600" ]; then
        echo -e "${GREEN}✅ Permissões da chave privada corretas (600)${NC}"
    else
        echo -e "${YELLOW}⚠️  ATENÇÃO: Chave privada deveria ter permissão 600${NC}"
        echo "   Execute: chmod 600 security/jwt-private.pem"
    fi
else
    echo -e "${RED}❌ ERRO: Chaves JWT não encontradas!${NC}"
    echo "   Execute: ./generate-jwt-keys.sh"
    ERRORS=$((ERRORS + 1))
fi

echo ""
echo "=========================================="

# ====================================
# RESULTADO FINAL
# ====================================
if [ $ERRORS -eq 0 ]; then
    echo -e "${GREEN}✅ VALIDAÇÃO COMPLETA: Sistema pronto para produção!${NC}"
    echo ""
    echo "Próximos passos:"
    echo "  1. Testar localmente: ./mvnw clean compile quarkus:dev"
    echo "  2. Verificar banco: Apenas protouser deve existir"
    echo "  3. Testar login com: contato@aguide.space / Kabala1975"
    echo "  4. Deploy em produção: git push origin main"
    exit 0
else
    echo -e "${RED}❌ VALIDAÇÃO FALHOU: $ERRORS erro(s) encontrado(s)!${NC}"
    echo ""
    echo "❌ NÃO faça deploy até corrigir os erros acima!"
    exit 1
fi
