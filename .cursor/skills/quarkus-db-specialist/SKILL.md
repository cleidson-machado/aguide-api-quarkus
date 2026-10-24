---
name: quarkus-db-specialist
description: Evolui schema PostgreSQL, migrations Flyway, entidades Panache e camada REST alinhada ao modelo do mobile-rest-api. Usar ao criar/alterar tabelas, índices, SQL, mapeamento JPA, seeds de dev/test ou diagnosticar conflitos Flyway/Hibernate.
---

# Quarkus DB Specialist — mobile-rest-api

Perfil **Dev Sênior PostgreSQL + Quarkus**. Schema é dono do **Flyway**; Hibernate só mapeia (`database.generation=none`).

## Guardrail de ambiente

| Banco | Uso |
|-------|-----|
| `quarkus_dev` | Dev local — pode resetar |
| `quarkus_test` | `./mvnw test` |
| `quarkus_db` | **Produção VPS — nunca conectar localmente** |

Migrations em prod: **somente não destrutivas**. Pedir confirmação explícita antes de **criar ou editar** arquivo em `src/main/resources/db/migration/`.

Versão atual de referência no repo: até `V1.0.24__...` — próxima: **`V1.0.25__...`**.

---

## Convenções deste projeto

- Pacote: `br.com.aguideptbr.features.<feature>/`
- Tabelas: prefixo **`app_`** (ex.: `app_user`, `app_conversation`)
- PK: **UUID** (`gen_random_uuid()` no SQL; `@GeneratedValue(strategy = GenerationType.UUID)` no Java)
- Auditoria: `created_at`, `updated_at`, `deleted_at` (soft delete)
- Queries ativas: `deletedAt is null` / `WHERE deleted_at IS NULL`
- Índices parciais: `WHERE deleted_at IS NULL` quando filtrar registros ativos
- Injeção: construtor; DTOs encapsulados

Pasta migrations: `src/main/resources/db/migration/`

---

## Flyway — nomenclatura e regras

```
V{major}.{minor}.{patch}__{PascalCase_Description}.sql
```

Exemplos reais do repo: `V1.0.23__Create_messaging_tables.sql`, `V1.0.24__Add_mute_clear_and_user_block.sql`.

**Permitido:** `CREATE TABLE`, `ALTER ADD COLUMN`, `CREATE INDEX`, `UPDATE` controlado, `INSERT ... ON CONFLICT DO NOTHING`, `COMMENT ON`.

**Proibido em produção:** `DROP TABLE/COLUMN`, `TRUNCATE`, `DROP SCHEMA/DATABASE`.

**Nunca** alterar migration já aplicada — criar nova versão. Checksum quebrado → corrigir com nova migration, não reescrever histórico.

### Esqueleto — nova tabela

```sql
CREATE TABLE app_<entidade> (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    -- colunas de negócio
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP
);

CREATE INDEX idx_<entidade>_<coluna>
    ON app_<entidade>(<coluna>) WHERE deleted_at IS NULL;

COMMENT ON TABLE app_<entidade> IS '...';
```

### Esqueleto — nova coluna

```sql
ALTER TABLE app_<entidade> ADD COLUMN IF NOT EXISTS <coluna> <tipo>;
UPDATE app_<entidade> SET <coluna> = <default> WHERE <coluna> IS NULL;
-- NOT NULL só depois do backfill
```

### Dados iniciais (idempotente)

```sql
INSERT INTO app_<entidade> (id, ...)
VALUES (gen_random_uuid(), ...)
ON CONFLICT (<coluna_unica>) DO NOTHING;
```

---

## Entidade Panache (alinhada ao SQL)

- `@Table(name = "app_<entidade>")`
- Relacionamentos: preferir `LAZY`; `@ManyToOne` no lado da FK
- `@JsonIgnore` em associações que não devem vazar no JSON
- Helper opcional: `isActive()` → `deletedAt == null`

---

## Repository

- `PanacheRepositoryBase<Model, UUID>`
- Métodos `findBy...` sempre considerando soft delete quando fizer sentido
- Soft delete: `entity.deletedAt = LocalDateTime.now(); persist(entity);`

---

## Service e REST

- `@Transactional` apenas em CUD
- Conflitos de negócio: `409`; não encontrado: `404`
- Path: `/api/v1/<recurso>`
- Paginação: Panache `Page` + `PaginatedResponse` quando listagens forem grandes

---

## Seeds e scripts ad hoc

- **Produção:** sem seed manual no `quarkus_db`.
- Dev/test: scripts em **`a_error_log_temp/`** com UUIDs fixos e `ON CONFLICT DO NOTHING`.
- DELETE de dados de teste: ordem **folha → raiz** respeitando FKs; preferir soft delete em dados “de negócio”.

---

## SQL avançado (quando necessário)

- Agregações + filtros: CTEs ou `COUNT(...) FILTER (WHERE ...)`
- Índice composto quando `WHERE` e `ORDER BY` divergem
- Evitar índice só em coluna booleana/enumerada de baixa cardinalidade

Para messaging/ranking, inspecionar migrations `V1.0.20`–`V1.0.24` como referência de estilo.

---

## Diagnóstico de conflitos

| Sintoma | Causa provável | Ação |
|---------|----------------|------|
| Checksum mismatch | Migration aplicada foi editada | Nova migration; não editar a antiga |
| Column does not exist | Entity à frente do banco | Adicionar migration |
| JPQL inválido | Nome de campo ≠ coluna | `@Column(name=...)` |
| Timeout Flyway em CI | `clean-at-start=true` indevido | Prod/test: `clean-at-start=false` |

Prod obrigatório (`application-prod.properties`):

```properties
quarkus.flyway.clean-at-start=false
quarkus.flyway.migrate-at-start=true
quarkus.hibernate-orm.database.generation=none
```

---

## Workflow E2E (checklist)

```
- [ ] Próxima versão V1.0.X definida
- [ ] SQL: tabela → FKs → índices → comments
- [ ] *Model.java alinhado coluna a coluna
- [ ] *Repository.java (queries + soft delete)
- [ ] DTOs + validação
- [ ] *Service.java (@Transactional CUD)
- [ ] *Controller.java /api/v1/...
- [ ] Seed em a_error_log_temp/ se necessário (dev/test)
- [ ] ./mvnw compile && testes da feature
- [ ] validate-production-safety / grep destrutivo nas migrations
```

---

## Validação CI

GitHub Actions rejeita migrations com `DROP TABLE`, `TRUNCATE`, etc. Alinhar SQL antes do PR.

Script local: `./validate-production-safety.sh`

---

## Referências

- Migrations existentes: `src/main/resources/db/migration/`
- Skill Java geral: [../quarkus-java/SKILL.md](../quarkus-java/SKILL.md)
- Git/deploy: [../git-cicd/SKILL.md](../git-cicd/SKILL.md)
- Documentação legada (referência): `.github/skills/quarkus-db-specialist/SKILL.md`
