# 🚀 INÍCIO RÁPIDO - Desenvolvimento Seguro

## ✅ STATUS: Configuração Completa!

Os **3 bancos de dados** já estão criados no PostgreSQL local:
- ✅ `quarkus_db` (produção - VPS apenas)
- ✅ `quarkus_dev` (desenvolvimento - MacBook)
- ✅ `quarkus_test` (testes - MacBook)

**PostgreSQL local:** Container `quarkus_postgres` já rodando no Docker Desktop

---

## 🎯 Próximos Passos

### 1. Verificar PostgreSQL Local

```bash
# Verificar se PostgreSQL está rodando
docker ps | grep quarkus_postgres

# Se não estiver, inicie seu stack Docker que contém o PostgreSQL
```

### 2. Carregar Variáveis de Ambiente

**SEMPRE execute isso antes de usar a aplicação:**

```bash
cd /Users/cleidson/RestAPIsApps/GoBack_Java_Quarkus/mobile-rest-api
source .env
```

### 3. Verificar Configuração

```bash
# Verificar profile (deve ser 'dev')
echo $QUARKUS_PROFILE

# Verificar banco (deve ser 'quarkus_dev')
grep DB_DEV_NAME .env
```

**Resultado esperado:**
```
dev
DB_DEV_NAME=quarkus_dev
```

### 4. Executar Aplicação em Dev Mode

```bash
./mvnw quarkus:dev
```

**✅ Seguro:** Agora a aplicação conecta em `quarkus_dev`, **NÃO** em `quarkus_db`!

Acesse: `https://localhost:8443`

### 5. Executar Testes

```bash
./mvnw test
```

**✅ Seguro:** Testes usam `quarkus_test`, **NÃO** afetam `quarkus_dev` nem `quarkus_db`!

---

## 🔍 Como Verificar se Está Correto

### Durante `./mvnw quarkus:dev`, verifique os logs:

Procure por linhas como:
```
HikkaraPool: Using datasource: jdbc:postgresql://localhost:5432/quarkus_dev
```

**✅ CORRETO:** Mostra `quarkus_dev`
**❌ ERRADO:** Se mostrar `quarkus_db`, **PARE IMEDIATAMENTE** e verifique `.env`!

---

## ⚠️ Comandos que Você DEVE Usar Sempre

### Desenvolvimento Local:
```bash
source .env && ./mvnw quarkus:dev
```

### Testes:
```bash
./mvnw test
```

### Limpar e Compilar:
```bash
./mvnw clean package
```

---

## 🚨 O Que NÃO Fazer

### ❌ NUNCA execute sem carregar .env:
```bash
# ❌ ERRADO (pode conectar no banco errado):
./mvnw quarkus:dev
```

### ✅ SEMPRE carregue .env primeiro:
```bash
# ✅ CORRETO:
source .env && ./mvnw quarkus:dev
```

---

## 📋 Checklist Diário

Antes de começar a trabalhar:

- [ ] `cd /Users/cleidson/RestAPIsApps/GoBack_Java_Quarkus/mobile-rest-api`
- [ ] `source .env`
- [ ] `echo $QUARKUS_PROFILE` mostra `dev`?
- [ ] `docker ps | grep postgres` mostra container rodando?
- [ ] Agora sim: `./mvnw quarkus:dev`

---

## 🆘 Problemas Comuns

### Problema: "Connection refused" ao iniciar aplicação

**Solução:**
```bash
# Verificar se PostgreSQL está rodando:
docker ps | grep quarkus_postgres

# Se não estiver, inicie seu stack Docker que contém o PostgreSQL
```

### Problema: "Banco quarkus_db foi resetado!"

**Causa:** Conectou no banco errado sem `source .env`

**Solução:**
```bash
# 1. Pare a aplicação (Ctrl+C)
# 2. Verifique a configuração:
source .env
echo $QUARKUS_PROFILE  # Deve ser: dev
grep DB_DEV_NAME .env  # Deve mostrar: quarkus_dev

# 3. Reinicie corretamente:
./mvnw quarkus:dev
```

### Problema: Testes falhando

**Solução:**
```bash
# Limpar banco de testes:
docker exec quarkus_postgres psql -U quarkus -d quarkus_test -c "DROP SCHEMA public CASCADE; CREATE SCHEMA public;"

# Executar testes novamente:
./mvnw test
```

---

## 📚 Documentação Completa

Para mais detalhes:

1. **[SETUP_GUIDE_DATABASES.md](SETUP_GUIDE_DATABASES.md)** - Guia completo
2. **[SOLUCAO_BANCOS_SEPARADOS.md](a_error_log_temp/SOLUCAO_BANCOS_SEPARADOS.md)** - Resumo da solução
3. **[.github/copilot-instructions.md](.github/copilot-instructions.md)** - Documentação do projeto

---

## ✅ Tudo Pronto!

Agora você pode desenvolver localmente com **TOTAL SEGURANÇA**:

✅ `quarkus_dev` é seu banco de desenvolvimento (pode limpar à vontade)
✅ `quarkus_test` é seu banco de testes (limpo automaticamente)
✅ `quarkus_db` é seu banco de produção (**NUNCA** será tocado localmente!)

**Bora codar! 🚀**
