#!/bin/bash

# ========================================
# Script de Configuração do .env
# ========================================
# Gera chaves seguras e atualiza o arquivo .env
# Uso: ./setup-env.sh

set -e  # Exit on error

CYAN='\033[0;36m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${CYAN}"
echo "========================================="
echo "  Configuração de Variáveis de Ambiente"
echo "========================================="
echo -e "${NC}"

# Verificar se .env já existe
if [ -f ".env" ]; then
    echo -e "${YELLOW}⚠️  Arquivo .env já existe!${NC}"
    read -p "Deseja sobrescrever? (s/N): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Ss]$ ]]; then
        echo -e "${RED}❌ Operação cancelada${NC}"
        exit 1
    fi
fi

# Copiar template
echo -e "${CYAN}📋 Copiando .env.example para .env...${NC}"
cp .env.example .env

# Gerar chave HMAC segura
echo -e "${CYAN}🔐 Gerando chave HMAC-SHA256 segura...${NC}"
HMAC_SECRET=$(openssl rand -hex 32)

if [ -z "$HMAC_SECRET" ]; then
    echo -e "${RED}❌ Erro ao gerar chave HMAC!${NC}"
    exit 1
fi

echo -e "${GREEN}✅ Chave gerada: ${HMAC_SECRET:0:16}...${NC}"

# Atualizar .env com a chave gerada
echo -e "${CYAN}📝 Atualizando .env com chave HMAC...${NC}"

# macOS usa sed diferente do Linux
if [[ "$OSTYPE" == "darwin"* ]]; then
    # macOS
    sed -i '' "s/your-secret-key-for-ownership-validation-minimum-32-chars/$HMAC_SECRET/" .env
    sed -i '' "s/your_keystore_password_here/quarkus/" .env
else
    # Linux
    sed -i "s/your-secret-key-for-ownership-validation-minimum-32-chars/$HMAC_SECRET/" .env
    sed -i "s/your_keystore_password_here/quarkus/" .env
fi

# Configurar permissões (somente owner pode ler/escrever)
chmod 600 .env

echo -e "${GREEN}✅ Arquivo .env configurado com sucesso!${NC}"
echo ""
echo -e "${YELLOW}📋 Próximos passos:${NC}"
echo "1. Edite o arquivo .env e configure suas credenciais de banco:"
echo "   - DB_DEV_NAME, DB_DEV_USERNAME, DB_DEV_PASSWORD"
echo "   - DB_TEST_NAME, DB_TEST_USERNAME, DB_TEST_PASSWORD"
echo ""
echo "2. Carregue as variáveis de ambiente:"
echo -e "   ${CYAN}source .env${NC}"
echo ""
echo "3. Inicie a aplicação:"
echo -e "   ${CYAN}./mvnw quarkus:dev${NC}"
echo ""
echo -e "${GREEN}🔒 Segurança: O arquivo .env está protegido (permissão 600)${NC}"
echo -e "${GREEN}⚠️  NUNCA commite o arquivo .env no Git!${NC}"
