#!/bin/bash

# Script para gerar chaves RSA para assinatura de JWT
# Uso: ./generate-jwt-keys.sh

set -e

SECURITY_DIR="security"

echo "🔐 Gerando chaves RSA para JWT..."

# Cria diretório se não existir
mkdir -p $SECURITY_DIR

# Gera chave privada RSA (2048 bits)
echo "📝 Gerando chave privada..."
openssl genrsa -out $SECURITY_DIR/jwt-private.pem 2048

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
echo "   - Adicione $SECURITY_DIR/*.pem ao .gitignore"
echo "   - NO VPS: copie as chaves para /opt/apps/aguide-api-quarkus/security/"
echo "   - Configure o volume no docker-compose.yml"
echo ""
