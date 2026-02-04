#!/bin/bash

# Script de Teste da API de Autenticação JWT
# Uso: ./test-auth-api.sh [base_url]

set -e

# Configurações
BASE_URL=${1:-"http://localhost:8080"}
API_URL="$BASE_URL/api/v1/auth"

# Cores para output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Dados de teste
TEST_EMAIL="teste$(date +%s)@example.com"
TEST_PASSWORD="senha123456"
TEST_NAME="Usuario"
TEST_SURNAME="Teste"

echo "🧪 Testando API de Autenticação JWT"
echo "📍 Base URL: $BASE_URL"
echo "=================================="
echo ""

# Função para printar sucesso
success() {
    echo -e "${GREEN}✅ $1${NC}"
}

# Função para printar erro
error() {
    echo -e "${RED}❌ $1${NC}"
}

# Função para printar info
info() {
    echo -e "${YELLOW}ℹ️  $1${NC}"
}

# 1. Testar Health Check
echo "1️⃣ Testando Health Check..."
HEALTH_RESPONSE=$(curl -s -w "\n%{http_code}" "$API_URL/health")
HEALTH_CODE=$(echo "$HEALTH_RESPONSE" | tail -n1)
HEALTH_BODY=$(echo "$HEALTH_RESPONSE" | head -n-1)

if [ "$HEALTH_CODE" -eq 200 ]; then
    success "Health check OK"
    echo "   Response: $HEALTH_BODY"
else
    error "Health check falhou (HTTP $HEALTH_CODE)"
    exit 1
fi
echo ""

# 2. Testar Registro de Usuário
echo "2️⃣ Testando Registro de Usuário..."
REGISTER_RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$API_URL/register" \
    -H "Content-Type: application/json" \
    -d "{
        \"name\": \"$TEST_NAME\",
        \"surname\": \"$TEST_SURNAME\",
        \"email\": \"$TEST_EMAIL\",
        \"password\": \"$TEST_PASSWORD\"
    }")

REGISTER_CODE=$(echo "$REGISTER_RESPONSE" | tail -n1)
REGISTER_BODY=$(echo "$REGISTER_RESPONSE" | head -n-1)

if [ "$REGISTER_CODE" -eq 201 ]; then
    success "Usuário registrado com sucesso"
    TOKEN=$(echo "$REGISTER_BODY" | grep -o '"token":"[^"]*' | cut -d'"' -f4)

    if [ -z "$TOKEN" ]; then
        error "Token não encontrado na resposta"
        exit 1
    fi

    info "Token recebido: ${TOKEN:0:50}..."
    info "Email: $TEST_EMAIL"
else
    error "Registro falhou (HTTP $REGISTER_CODE)"
    echo "   Response: $REGISTER_BODY"
    exit 1
fi
echo ""

# 3. Testar Login com Credenciais Corretas
echo "3️⃣ Testando Login (credenciais corretas)..."
LOGIN_RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$API_URL/login" \
    -H "Content-Type: application/json" \
    -d "{
        \"email\": \"$TEST_EMAIL\",
        \"password\": \"$TEST_PASSWORD\"
    }")

LOGIN_CODE=$(echo "$LOGIN_RESPONSE" | tail -n1)
LOGIN_BODY=$(echo "$LOGIN_RESPONSE" | head -n-1)

if [ "$LOGIN_CODE" -eq 200 ]; then
    success "Login bem-sucedido"
    TOKEN=$(echo "$LOGIN_BODY" | grep -o '"token":"[^"]*' | cut -d'"' -f4)
    info "Token atualizado: ${TOKEN:0:50}..."
else
    error "Login falhou (HTTP $LOGIN_CODE)"
    echo "   Response: $LOGIN_BODY"
    exit 1
fi
echo ""

# 4. Testar Login com Senha Incorreta
echo "4️⃣ Testando Login (senha incorreta)..."
WRONG_LOGIN_RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$API_URL/login" \
    -H "Content-Type: application/json" \
    -d "{
        \"email\": \"$TEST_EMAIL\",
        \"password\": \"senhaerrada123\"
    }")

WRONG_LOGIN_CODE=$(echo "$WRONG_LOGIN_RESPONSE" | tail -n1)

if [ "$WRONG_LOGIN_CODE" -eq 401 ]; then
    success "Login com senha incorreta rejeitado corretamente"
else
    error "Deveria rejeitar senha incorreta (HTTP $WRONG_LOGIN_CODE)"
fi
echo ""

# 5. Testar Acesso ao Endpoint /me COM Token
echo "5️⃣ Testando /me COM token válido..."
ME_RESPONSE=$(curl -s -w "\n%{http_code}" -X GET "$API_URL/me" \
    -H "Authorization: Bearer $TOKEN")

ME_CODE=$(echo "$ME_RESPONSE" | tail -n1)
ME_BODY=$(echo "$ME_RESPONSE" | head -n-1)

if [ "$ME_CODE" -eq 200 ]; then
    success "Acesso ao /me autorizado"
    echo "   User info: $ME_BODY"
else
    error "Acesso ao /me falhou (HTTP $ME_CODE)"
    echo "   Response: $ME_BODY"
fi
echo ""

# 6. Testar Acesso ao Endpoint /me SEM Token
echo "6️⃣ Testando /me SEM token..."
NO_TOKEN_RESPONSE=$(curl -s -w "\n%{http_code}" -X GET "$API_URL/me")

NO_TOKEN_CODE=$(echo "$NO_TOKEN_RESPONSE" | tail -n1)

if [ "$NO_TOKEN_CODE" -eq 401 ]; then
    success "Acesso sem token rejeitado corretamente"
else
    error "Deveria rejeitar requisição sem token (HTTP $NO_TOKEN_CODE)"
fi
echo ""

# 7. Testar Registro com Email Duplicado
echo "7️⃣ Testando registro com email duplicado..."
DUPLICATE_RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$API_URL/register" \
    -H "Content-Type: application/json" \
    -d "{
        \"name\": \"Outro\",
        \"surname\": \"Usuario\",
        \"email\": \"$TEST_EMAIL\",
        \"password\": \"outrasenha\"
    }")

DUPLICATE_CODE=$(echo "$DUPLICATE_RESPONSE" | tail -n1)

if [ "$DUPLICATE_CODE" -eq 409 ]; then
    success "Email duplicado rejeitado corretamente"
else
    error "Deveria rejeitar email duplicado (HTTP $DUPLICATE_CODE)"
fi
echo ""

# Resumo
echo "=================================="
echo "🎉 Todos os testes passaram!"
echo ""
echo "📝 Dados de teste criados:"
echo "   Email: $TEST_EMAIL"
echo "   Senha: $TEST_PASSWORD"
echo ""
echo "🔑 Último token gerado:"
echo "   ${TOKEN:0:80}..."
echo ""
echo "💡 Para testar manualmente:"
echo "   curl -X GET $API_URL/me \\"
echo "     -H \"Authorization: Bearer $TOKEN\""
echo ""
