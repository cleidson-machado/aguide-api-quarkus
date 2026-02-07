#!/bin/bash

# ============================================
# Script de Validação Pré-Push
# Verifica se VPS está pronto ANTES de push
# ============================================

set -e

echo "🔍 ================================================"
echo "🔍  VALIDAÇÃO PRÉ-PUSH - VPS Ready Check"
echo "🔍 ================================================"
echo ""

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Configurações (EDITE AQUI)
VPS_HOST="${VPS_HOST:-seu-usuario@seu-vps-ip}"
VPS_PROJECT_DIR="${VPS_PROJECT_DIR:-/opt/apps/aguide-api-quarkus}"

# ============================================
# Função de ajuda
# ============================================
show_help() {
    echo "Uso: ./validate-vps-ready.sh"
    echo ""
    echo "Variáveis de ambiente (opcionais):"
    echo "  VPS_HOST         - SSH host (ex: root@192.168.1.100)"
    echo "  VPS_PROJECT_DIR  - Diretório do projeto no VPS"
    echo ""
    echo "Exemplo:"
    echo "  VPS_HOST=root@meu-vps.com ./validate-vps-ready.sh"
    echo ""
}

# ============================================
# Validações LOCAIS
# ============================================
echo "📋 [1/5] Validando repositório LOCAL..."

if [ ! -d ".git" ]; then
    echo -e "${RED}❌ Não está em um repositório Git${NC}"
    exit 1
fi

if [ ! -f ".gitignore" ]; then
    echo -e "${RED}❌ .gitignore não encontrado${NC}"
    exit 1
fi

if ! grep -q "^security/" .gitignore; then
    echo -e "${RED}❌ Pasta security/ não está no .gitignore${NC}"
    exit 1
fi

if [ -d "security" ]; then
    if git ls-files security/ | grep -q .; then
        echo -e "${RED}❌ ERRO: Arquivos de security/ estão commitados no Git!${NC}"
        echo "   Execute: git rm -r --cached security/"
        exit 1
    fi
fi

echo -e "${GREEN}✅ Repositório local OK${NC}"
echo ""

# ============================================
# Validações GIT STATUS
# ============================================
echo "📋 [2/5] Verificando Git status..."

if [ -n "$(git status --porcelain)" ]; then
    echo -e "${YELLOW}⚠️  Há alterações não commitadas:${NC}"
    git status --short
    echo ""
    read -p "Deseja continuar mesmo assim? (y/N): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        echo -e "${YELLOW}❌ Validação cancelada${NC}"
        exit 1
    fi
else
    echo -e "${GREEN}✅ Working tree limpo${NC}"
fi
echo ""

# ============================================
# Perguntar credenciais VPS
# ============================================
echo "📋 [3/5] Configuração de acesso ao VPS..."

if [ "$VPS_HOST" = "seu-usuario@seu-vps-ip" ]; then
    echo -e "${YELLOW}⚠️  Variável VPS_HOST não configurada${NC}"
    echo ""
    read -p "Digite o host SSH do VPS (ex: root@192.168.1.100): " VPS_INPUT

    if [ -z "$VPS_INPUT" ]; then
        echo -e "${RED}❌ Host SSH é obrigatório${NC}"
        exit 1
    fi

    VPS_HOST="$VPS_INPUT"
fi

echo "🔌 Testando conexão SSH com $VPS_HOST..."

if ! ssh -o ConnectTimeout=5 -o BatchMode=yes "$VPS_HOST" "echo '✅ Conexão OK'" 2>/dev/null; then
    echo -e "${RED}❌ Não foi possível conectar via SSH${NC}"
    echo ""
    echo "Tente manualmente:"
    echo "  ssh $VPS_HOST"
    echo ""
    echo "Se pedir senha, configure chave SSH:"
    echo "  ssh-copy-id $VPS_HOST"
    exit 1
fi

echo -e "${GREEN}✅ Conexão SSH OK${NC}"
echo ""

# ============================================
# Validações no VPS (CRÍTICO!)
# ============================================
echo "📋 [4/5] Validando VPS (via SSH)..."

echo "🔍 Verificando estrutura de diretórios..."
ssh "$VPS_HOST" bash << EOF
set -e

# Verifica diretório do projeto
if [ ! -d "$VPS_PROJECT_DIR" ]; then
    echo "❌ Diretório $VPS_PROJECT_DIR não existe"
    exit 1
fi

cd $VPS_PROJECT_DIR

# Verifica pasta security
if [ ! -d "security" ]; then
    echo "❌ Pasta security/ não existe"
    echo "   Crie com: mkdir -p security"
    exit 1
fi

# Verifica chave privada
if [ ! -f "security/jwt-private.pem" ]; then
    echo "❌ Arquivo security/jwt-private.pem NÃO EXISTE"
    echo "   Crie com: ./generate-jwt-keys.sh"
    exit 1
fi

# Verifica chave pública
if [ ! -f "security/jwt-public.pem" ]; then
    echo "❌ Arquivo security/jwt-public.pem NÃO EXISTE"
    echo "   Crie com: ./generate-jwt-keys.sh"
    exit 1
fi

# Verifica permissões da chave privada
PERM=\$(stat -c "%a" security/jwt-private.pem 2>/dev/null || stat -f "%OLp" security/jwt-private.pem 2>/dev/null)
if [ "\$PERM" != "600" ]; then
    echo "⚠️  Permissões incorretas em jwt-private.pem (atual: \$PERM, esperado: 600)"
    echo "   Corrija com: chmod 600 security/jwt-private.pem"
    exit 1
fi

# Verifica formato da chave
if ! head -n 1 security/jwt-private.pem | grep -q "BEGIN PRIVATE KEY"; then
    echo "❌ Formato inválido em jwt-private.pem"
    echo "   Deve começar com: -----BEGIN PRIVATE KEY-----"
    exit 1
fi

if ! head -n 1 security/jwt-public.pem | grep -q "BEGIN PUBLIC KEY"; then
    echo "❌ Formato inválido em jwt-public.pem"
    echo "   Deve começar com: -----BEGIN PUBLIC KEY-----"
    exit 1
fi

# Verifica que não está no Git
if git ls-files security/ | grep -q .; then
    echo "❌ CRÍTICO: Arquivos de security/ estão commitados no Git do VPS!"
    exit 1
fi

echo "✅ Todas as verificações do VPS passaram"
EOF

if [ $? -ne 0 ]; then
    echo ""
    echo -e "${RED}❌ Validação do VPS FALHOU${NC}"
    echo ""
    echo "🔧 COMO CORRIGIR:"
    echo "   1. SSH no VPS: ssh $VPS_HOST"
    echo "   2. Navegar: cd $VPS_PROJECT_DIR"
    echo "   3. Gerar chaves: ./generate-jwt-keys.sh"
    echo "   4. Validar: ls -lh security/"
    echo "   5. Rodar este script novamente"
    echo ""
    exit 1
fi

echo -e "${GREEN}✅ VPS está pronto para receber deploy${NC}"
echo ""

# ============================================
# Validação Docker/Jenkins (Opcional)
# ============================================
echo "📋 [5/5] Verificando ambiente Docker no VPS..."

ssh "$VPS_HOST" bash << 'EOF'
# Verifica se Docker está instalado
if ! command -v docker &> /dev/null; then
    echo "⚠️  Docker não encontrado (pode estar OK se usar outro método)"
else
    echo "✅ Docker instalado: $(docker --version)"
fi

# Verifica se docker-compose existe
if [ -f "docker-compose.yml" ]; then
    echo "✅ docker-compose.yml encontrado"
else
    echo "⚠️  docker-compose.yml não encontrado"
fi
EOF

echo ""

# ============================================
# RESUMO FINAL
# ============================================
echo "🎉 ================================================"
echo "🎉  VALIDAÇÃO COMPLETA - VPS PRONTO!"
echo "🎉 ================================================"
echo ""
echo -e "${GREEN}✅ Repositório local OK${NC}"
echo -e "${GREEN}✅ Pasta security/ protegida pelo .gitignore${NC}"
echo -e "${GREEN}✅ Conexão SSH com VPS OK${NC}"
echo -e "${GREEN}✅ Chaves JWT criadas no VPS${NC}"
echo -e "${GREEN}✅ Permissões corretas${NC}"
echo -e "${GREEN}✅ Formato de chaves válido${NC}"
echo ""
echo "🚀 PRÓXIMOS PASSOS:"
echo "   1. Fazer commit das suas alterações (se houver)"
echo "   2. Push para branch develop ou criar PR"
echo "   3. Merge para main (vai acionar Jenkins automaticamente)"
echo "   4. Aguardar Jenkins fazer deploy"
echo "   5. Validar login: curl -X POST https://seu-dominio.com/api/v1/auth/login"
echo ""
echo "📋 COMANDO DE TESTE (após deploy):"
echo "   curl -X POST https://seu-dominio.com/api/v1/auth/login \\"
echo "     -H 'Content-Type: application/json' \\"
echo "     -d '{\"email\":\"contato@aguide.space\",\"password\":\"Kabala1975\"}'"
echo ""
echo -e "${GREEN}✅ Você pode fazer push com segurança!${NC}"
