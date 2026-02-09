# 🚀 Guia de Uso - Separação de Ambientes

Este documento explica como usar corretamente os **3 bancos de dados separados** para evitar perda de dados de produção.

---

## 📊 Visão Geral dos Ambientes

| Ambiente | Banco | Host | Profile | Pode Limpar? |
|----------|-------|------|---------|--------------|
| **Produção (VPS)** | `quarkus_db` | `quarkus_postgres` (Docker) | `prod` | ❌ **NUNCA** |
| **Desenvolvimento** | `quarkus_dev` | `localhost` | `dev` | ✅ Sim (seguro) |
| **Testes** | `quarkus_test` | `localhost` | `test` | ✅ Sim (limpo antes de testes) |

---

## 🖥️ Desenvolvimento Local (MacBook)

### 1. Inicialização pela Primeira Vez

```bash
# 1. Verificar se PostgreSQL está rodando
docker ps | grep quarkus_postgres
# Deve mostrar o container rodando com os 3 bancos: quarkus_dev, quarkus_test, quarkus_db

# 2. Se PostgreSQL não estiver rodando, inicie seu stack Docker

# 3. Carregar variáveis de ambiente
source .env

# 4. Verificar configuração
echo $QUARKUS_PROFILE  # Deve mostrar: dev
grep DB_DEV_NAME .env  # Deve mostrar: quarkus_dev

# 5. Executar a aplicação (usa quarkus_dev)
./mvnw quarkus:dev
```

**Resultado Esperado:**
- ✅ Aplicação conecta em `jdbc:postgresql://localhost:5432/quarkus_dev`
- ✅ Flyway cria as tabelas em `quarkus_dev`
- ✅ `quarkus_db` (produção) permanece intocado
- ✅ Acesso via `https://localhost:8443`

### 2. Uso Diário

```bash
# Sempre antes de iniciar desenvolvimento:
source .env && ./mvnw quarkus:dev
```

### 3. Resetar Banco de Desenvolvimento

Se quiser limpar e recriar o banco `quarkus_dev`:

```bash
# Opção 1: Deixar o Flyway limpar automaticamente
# (application-dev.properties já tem clean-at-start=true)
source .env && ./mvnw quarkus:dev

# Opção 2: Limpar manualmente via psql
docker exec -it quarkus_postgres psql -U quarkus -d quarkus_dev -c "DROP SCHEMA public CASCADE; CREATE SCHEMA public;"
source .env && ./mvnw quarkus:dev
```

**⚠️ Seguro:** Isso **NÃO** afeta `quarkus_db` (produção)!

---

## 🧪 Executar Testes

```bash
# Testes usam quarkus_test automaticamente
./mvnw test

# Ou com limpeza de build:
./mvnw clean test
```

**Resultado Esperado:**
- ✅ Testes conectam em `jdbc:postgresql://localhost:5432/quarkus_test`
- ✅ `quarkus_dev` e `quarkus_db` permanecem intocados
- ✅ Flyway cria schema limpo antes de cada teste

---

## 🚀 Deploy em Produção (VPS)

### 1. Validação Antes do Deploy

**SEMPRE** execute antes de fazer merge para `main`:

```bash
./validate-production-safety.sh
```

Se o script retornar erro (exit 1), **NÃO prossiga** com o deploy!

### 2. Deploy no VPS

```bash
# SSH no VPS
ssh user@your-vps

# Navegar para o diretório do projeto
cd /opt/apps/aguide-api-quarkus

# Pull das últimas mudanças
git pull origin main

# Verificar profile de produção
grep QUARKUS_PROFILE docker-compose.yml
# Deve mostrar: QUARKUS_PROFILE: prod

# Deploy seguro (não toca no PostgreSQL)
docker compose up -d --no-deps --build aguide-api

# Verificar logs
docker compose logs -f aguide-api
```

**Resultado Esperado:**
- ✅ Aplicação usa `QUARKUS_PROFILE=prod`
- ✅ Conecta em `jdbc:postgresql://quarkus_postgres:5432/quarkus_db`
- ✅ Flyway **NÃO** limpa banco (`clean-at-start=false`)
- ✅ Apenas migrations incrementais são aplicadas

---

## 🔍 Verificação de Configuração

### Verificar Profile Ativo

```bash
# Localmente (MacBook):
echo $QUARKUS_PROFILE  # Deve ser: dev

# No VPS (Docker):
docker compose exec aguide-api env | grep QUARKUS_PROFILE
# Deve mostrar: QUARKUS_PROFILE=prod
```

### Verificar Banco Conectado

**Durante `./mvnw quarkus:dev`**, verifique os logs para:

```
Hibernate:

    drop table if exists users cascade
```

Se você ver `drop table`, verifique:

```bash
# Qual banco está configurado?
grep "quarkus.datasource.jdbc.url" src/main/resources/application-dev.properties

# Deve mostrar: quarkus_dev (NÃO quarkus_db!)
```

### Verificar Bancos Existentes

```bash
# Listar bancos no PostgreSQL local:
docker exec -it quarkus_postgres psql -U quarkus -c "\l"

# Resultado esperado:
#   quarkus_dev  | desenvolvimento
#   quarkus_test | testes
#   quarkus_db   | produção (NÃO USAR LOCALMENTE!)
```

---

## ⚠️ Troubleshooting

### Problema: "Banco de produção foi resetado!"

**Causa:** Conectou em `quarkus_db` localmente com `clean-at-start=true`

**Solução:**
1. Parar a aplicação imediatamente
2. Restaurar backup do banco
3. Verificar `.env`:
   ```bash
   grep DB_DEV_NAME .env
   # Deve ser: quarkus_dev (NÃO quarkus_db)
   ```
4. Verificar profile:
   ```bash
   echo $QUARKUS_PROFILE
   # Deve ser: dev (NÃO prod)
   ```

### Problema: "Não consigo conectar no PostgreSQL"

**Causa:** Container PostgreSQL não está rodando

**Solução:**
```bash
# Verificar se PostgreSQL está rodando
docker ps | grep quarkus_postgres

# Se não estiver, inicie seu stack Docker que contém o PostgreSQL
```

### Problema: "Testes falham com erro de conexão"

**Causa:** Banco `quarkus_test` não existe

**Solução:**
```bash
# Recriar banco de testes
docker exec -it quarkus_postgres psql -U quarkus -d postgres -c "DROP DATABASE IF EXISTS quarkus_test; CREATE DATABASE quarkus_test;"

# Executar testes novamente
./mvnw test
```

### Problema: "Flyway migration falhou"

**Causa:** Migration com erro de sintaxe ou constraint violada

**Solução:**
```bash
# Verificar última migration aplicada
docker exec -it quarkus_postgres psql -U quarkus -d quarkus_dev -c "SELECT * FROM flyway_schema_history ORDER BY installed_rank DESC LIMIT 5;"

# Corrigir migration e executar repair
./mvnw quarkus:dev
# Flyway executa repair-at-start automaticamente
```

---

## 📋 Checklists Rápidos

### ✅ Antes de `./mvnw quarkus:dev`
- [ ] `source .env` executado?
- [ ] `echo $QUARKUS_PROFILE` mostra `dev`?
- [ ] PostgreSQL local rodando (`docker ps`)?
- [ ] Banco é `quarkus_dev` (não `quarkus_db`)?

### ✅ Antes de `./mvnw test`
- [ ] Banco `quarkus_test` existe?
- [ ] PostgreSQL local rodando?

### ✅ Antes de fazer PR para `main`
- [ ] `./validate-production-safety.sh` passou?
- [ ] `application-prod.properties` tem `clean-at-start=false`?
- [ ] Migrations são incrementais (não-destrutivas)?
- [ ] Testou localmente com `quarkus_dev`?

### ✅ Antes de deploy no VPS
- [ ] Pull mais recente de `main`?
- [ ] `docker-compose.yml` usa `QUARKUS_PROFILE=prod`?
- [ ] Backup do banco foi feito?
- [ ] Validação passou no CI/CD?

---

## 🔗 Documentação Completa

- [.env.example](.env.example) - Template de configuração
- [validate-production-safety.sh](validate-production-safety.sh) - Validação pré-deploy
- [.github/copilot-instructions.md](.github/copilot-instructions.md) - Guia completo do projeto
- [INCIDENT_PROD_DB_RESET_2026-02-09.md](a_error_log_temp/INCIDENT_PROD_DB_RESET_2026-02-09.md) - Histórico do problema

---

## 🆘 Suporte

Em caso de dúvidas ou problemas:

1. Consulte este guia
2. Execute `./validate-production-safety.sh`
3. Revise [.github/copilot-instructions.md](.github/copilot-instructions.md)
4. Verifique os logs: `docker compose logs -f`

**Regra de Ouro:** Em caso de dúvida, **NÃO execute comandos em produção** sem validar primeiro!
