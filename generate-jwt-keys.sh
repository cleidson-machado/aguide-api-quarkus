#!/bin/bash

# Script para gerar chaves RSA para assinatura de JWT
# Uso: ./generate-jwt-keys.sh

set -e

SECURITY_DIR="security"

echo "🔐 Gerando chaves RSA para JWT..."

# Cria diretório se não existir
mkdir -p $SECURITY_DIR

# Gera chave privada RSA (2048 bits) em formato PKCS#8
echo "📝 Gerando chave privada (PKCS#8)..."
openssl genrsa -out $SECURITY_DIR/jwt-private-temp.pem 2048
openssl pkcs8 -topk8 -inform PEM -in $SECURITY_DIR/jwt-private-temp.pem -out $SECURITY_DIR/jwt-private.pem -nocrypt
rm $SECURITY_DIR/jwt-private-temp.pem

# Extrai chave pública
echo "📝 Extraindo chave pública..."
openssl rsa -in $SECURITY_DIR/jwt-private.pem -pubout -out $SECURITY_DIR/jwt-public.pem

# Define permissões restritivas
chmod 600 $SECURITY_DIR/jwt-private.pem
chmod 644 $SECURITY_DIR/jwt-public.pem

echo ""
echo "✅ Chaves RSA geradas com sucesso!"
echo "   📁 Localização: $SECURITY_DIR/"
echo "   🔒 Privada: $SECURITY_DIR/jwt-private.pem (600)"
echo "   🔓 Pública: $SECURITY_DIR/jwt-public.pem (644)"
echo ""
echo "⚠️  IMPORTANTE:"
echo "   - $SECURITY_DIR/*.pem já está protegido pelo .gitignore"
echo "   - docker-compose.yml já mapeia o volume automaticamente"
echo "   - NO VPS: execute este script no diretório do projeto"
echo ""
echo "📋 Próximos passos:"
echo "   1. Validar: ls -lh $SECURITY_DIR/"
echo "   2. Verificar que NÃO está no Git: git status"
echo "   3. Pronto para deploy!"
echo ""
