#!/bin/bash

# Script de teste para endpoint Google OAuth
# Uso: ./test-google-oauth.sh

echo "🧪 Testando endpoint POST /api/v1/auth/oauth/google"
echo ""

# URL da API local (altere se necessário)
API_URL="https://localhost:8443/api/v1/auth/oauth/google"

# Dados de exemplo (substitua com dados reais do Flutter)
curl -X POST "$API_URL" \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -k \
  -d '{
    "email": "machado.swe@gmail.com",
    "name": "Pereira",
    "surname": "Machado",
    "oauthProvider": "GOOGLE",
    "oauthId": "107123456789",
    "accessToken": "ya29.a0ATkoCc71WV59I...",
    "idToken": "eyJhbGciOiJSUzI1NiIs..."
  }' | jq .

echo ""
echo "✅ Teste concluído!"
echo ""
echo "📝 Resposta esperada:"
echo "{
  \"token\": \"eyJhbGciOiJSUzI1NiIs...\",
  \"type\": \"Bearer\",
  \"expiresIn\": 3600,
  \"user\": {
    \"id\": \"...\",
    \"name\": \"Pereira\",
    \"surname\": \"Machado\",
    \"email\": \"machado.swe@gmail.com\",
    \"role\": \"FREE\"
  }
}"
