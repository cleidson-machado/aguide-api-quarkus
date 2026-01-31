# 🗄️ Guia de Migrations com Flyway

## 📋 Visão Geral

Este projeto utiliza **Flyway** para controle de versão do schema do banco de dados PostgreSQL. As migrations são executadas **automaticamente** quando a aplicação inicia.

## ⚙️ Configuração Atual

### ✅ Produção (`QUARKUS_PROFILE=prod`)
```properties
quarkus.flyway.migrate-at-start=true          # Executa migrations ao iniciar
quarkus.flyway.baseline-on-migrate=true       # Cria histórico se não existir
quarkus.flyway.clean-at-start=false           # NUNCA limpa o banco em produção
quarkus.hibernate-orm.database.generation=none # Flyway controla 100% do schema
```

### 🛠️ Desenvolvimento (`QUARKUS_PROFILE=dev`)
```properties
quarkus.flyway.clean-at-start=true            # Limpa e recria tudo sempre
quarkus.flyway.migrate-at-start=true          # Executa migrations
```

## 📂 Estrutura de Migrations

```
src/main/resources/db/migration/
├── V1.0.0__Create_tables.sql           # Criação inicial das tabelas
├── V1.0.1__Insert_test_data.sql        # Dados de teste
├── V1.0.2__Add_audit_timestamps.sql    # Campos de auditoria
└── V1.0.3__Add_published_at_column.sql # Nova coluna publishedAt
```

## 📝 Convenções de Nomenclatura

### Formato Obrigatório:
```
V[major].[minor].[patch]__[Description].sql
  ↑                        ↑
  Obrigatório             Dois underscores
```

### Exemplos Válidos:
- ✅ `V1.0.0__Create_tables.sql`
- ✅ `V1.0.3__Add_published_at_column.sql`
- ✅ `V2.0.0__Refactor_user_schema.sql`

### Exemplos INVÁLIDOS:
- ❌ `V1.0.0_Create_tables.sql` (um underscore apenas)
- ❌ `v1.0.0__Create_tables.sql` (v minúsculo)
- ❌ `V1__Create_tables.sql` (versão incompleta)
- ❌ `Create_tables.sql` (sem versão)

## 🚀 Como Criar Nova Migration

### 1. Crie o arquivo SQL:
```bash
touch src/main/resources/db/migration/V1.0.4__Add_user_avatar_column.sql
```

### 2. Escreva o SQL:
```sql
-- ========================================
-- ADICIONA COLUNA avatar_url
-- Versão: 1.0.4
-- Data: 2026-01-31
-- Descrição: Adiciona coluna para armazenar URL do avatar do usuário
-- ========================================

ALTER TABLE app_user
ADD COLUMN avatar_url VARCHAR(2048);

-- Índice se necessário
CREATE INDEX idx_app_user_avatar ON app_user(avatar_url);

-- Comentário para documentação
COMMENT ON COLUMN app_user.avatar_url IS 'URL da imagem de avatar do usuário';
```

### 3. Teste Localmente:
```bash
# Em modo dev (limpa e recria)
QUARKUS_PROFILE=dev ./mvnw quarkus:dev

# Verifique os logs:
# "Migrating schema ... to version 1.0.4 - Add user avatar column"
```

### 4. Commit e Push:
```bash
git add src/main/resources/db/migration/V1.0.4__Add_user_avatar_column.sql
git commit -m "feat: adiciona coluna avatar_url na tabela app_user"
git push origin develop-data-objects
```

## 🔄 Pipeline Jenkins - Fluxo de Deployment

```
┌─────────────────────────────────────────────────────────────┐
│ 1. Checkout      → Baixa código do Git                      │
│ 2. Build Maven   → Compila (migrations NÃO executam aqui)   │
│ 3. SonarQube     → Análise de código                         │
│ 4. Build Docker  → Cria imagem com código atualizado         │
│ 5. Deploy        → Sobe container (migrations executam aqui) │
│ 6. Verificar     → Valida que migrations foram aplicadas     │
└─────────────────────────────────────────────────────────────┘
```

### ⚠️ Momento da Execução das Migrations:

As migrations **NÃO** executam durante o build Maven. Elas executam:

✅ **Quando o container Docker inicia** (`docker compose up -d`)
✅ Ao iniciar em dev mode (`./mvnw quarkus:dev`)
❌ Durante `mvn package`
❌ Durante testes com `-DskipTests`

## 🐳 Como o Docker Executa Migrations

### No VPS (Produção):

1. **Jenkins faz deploy:**
   ```bash
   docker compose -f docker-compose.yml up -d
   ```

2. **Container aguide-api inicia:**
   ```bash
   # Variáveis de ambiente no docker-compose.yml:
   QUARKUS_PROFILE: prod
   QUARKUS_DATASOURCE_JDBC_URL: jdbc:postgresql://quarkus_postgres:5432/quarkus_db
   ```

3. **Quarkus inicia e Flyway executa:**
   ```
   [io.quarkus] (main) Starting Flyway migrations...
   [org.flywaydb.core] (main) Successfully validated 4 migrations
   [org.flywaydb.core] (main) Current version of schema: 1.0.2
   [org.flywaydb.core] (main) Migrating schema to version 1.0.3 - Add published at column
   [org.flywaydb.core] (main) Successfully applied 1 migration to schema
   ```

## 🔍 Como Verificar se Migration Foi Aplicada

### No VPS via Jenkins:
O pipeline agora inclui verificação automática:
```bash
docker logs aguide-api --tail 50 | grep -i "flyway\|migration"
```

### Manualmente no VPS:
```bash
# Conectar no container
docker exec -it aguide-api /bin/bash

# Ver logs do Quarkus
docker logs aguide-api | grep -i flyway

# Conectar no PostgreSQL
docker exec -it quarkus_postgres psql -U quarkus -d quarkus_db

# Verificar tabela de histórico do Flyway
SELECT * FROM flyway_schema_history ORDER BY installed_rank DESC;
```

### Query para ver migrations aplicadas:
```sql
SELECT
    installed_rank,
    version,
    description,
    type,
    script,
    installed_on,
    success
FROM flyway_schema_history
ORDER BY installed_rank DESC;
```

### Resultado esperado após V1.0.3:
```
 installed_rank | version |       description        | success | installed_on
----------------+---------+--------------------------+---------+-------------
              4 | 1.0.3   | Add published at column  | t       | 2026-01-31
              3 | 1.0.2   | Add audit timestamps     | t       | 2025-10-05
              2 | 1.0.1   | Insert test data         | t       | 2025-10-05
              1 | 1.0.0   | Create tables            | t       | 2025-10-05
```

## ⚠️ REGRAS IMPORTANTES

### ✅ PERMITIDO:
- Criar novas migrations (V1.0.4, V1.0.5, etc.)
- Adicionar colunas nullable
- Criar novos índices
- Adicionar tabelas

### ❌ PROIBIDO:
- **NUNCA** modificar migrations já aplicadas (V1.0.0 a V1.0.3)
- **NUNCA** renomear arquivos de migration
- **NUNCA** deletar migrations aplicadas
- **NUNCA** usar `clean-at-start=true` em produção

## 🐛 Troubleshooting

### Migration falhou no VPS:

1. **Verificar logs:**
   ```bash
   docker logs aguide-api --tail 100
   ```

2. **Verificar se banco está acessível:**
   ```bash
   docker exec aguide-api env | grep DATASOURCE
   ```

3. **Validar sintaxe SQL:**
   ```bash
   # Testar localmente primeiro
   QUARKUS_PROFILE=dev ./mvnw quarkus:dev
   ```

### Migration ficou travada (status = pending):

1. **Conectar no PostgreSQL:**
   ```bash
   docker exec -it quarkus_postgres psql -U quarkus -d quarkus_db
   ```

2. **Verificar status:**
   ```sql
   SELECT * FROM flyway_schema_history WHERE success = false;
   ```

3. **Corrigir manualmente:**
   ```sql
   -- Se a migration falhou parcialmente, corrija e marque como bem-sucedida
   -- ⚠️ CUIDADO: Só faça isso se souber o que está fazendo!
   UPDATE flyway_schema_history SET success = true WHERE version = '1.0.3';
   ```

4. **Reiniciar container:**
   ```bash
   docker restart aguide-api
   ```

### Forçar re-execução de migration (DEV APENAS):

```bash
# Limpar banco e rodar tudo de novo
QUARKUS_PROFILE=dev ./mvnw quarkus:dev

# Ou via Flyway CLI
./mvnw flyway:clean flyway:migrate
```

## 📚 Comandos Úteis

### Verificar migrations pendentes:
```bash
./mvnw flyway:info
```

### Validar migrations:
```bash
./mvnw flyway:validate
```

### Ver histórico completo:
```bash
./mvnw flyway:info -X
```

## 🔗 Referências

- [Flyway Documentation](https://flywaydb.org/documentation/)
- [Quarkus Flyway Guide](https://quarkus.io/guides/flyway)
- [PostgreSQL ALTER TABLE](https://www.postgresql.org/docs/current/sql-altertable.html)

---

**Última atualização:** 31 de Janeiro de 2026
**Versão atual do schema:** V1.0.3
