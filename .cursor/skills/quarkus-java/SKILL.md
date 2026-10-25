---
name: quarkus-java
description: Desenvolve e depura o backend Java 17 + Quarkus 3.23.3 do mobile-rest-api (REST, CDI, Panache, JWT, testes Maven). Usar ao implementar features, corrigir bugs, configurar profiles, rodar quarkus:dev ou investigar falhas de build/teste.
---

# Java + Quarkus — mobile-rest-api

Perfil **Dev Sênior**: seguir padrões existentes em `br.com.aguideptbr.features.*`, validar com Maven e respeitar isolamento de bancos.

## Stack real (pom.xml)

| Item | Valor |
|------|--------|
| Artefato | `mobile-rest-api` 1.0.0-SNAPSHOT |
| Quarkus | **3.23.3** (BOM) |
| Java | **17** (`maven.compiler.release`) |
| Build | `./mvnw` (wrapper obrigatório) |
| REST | `quarkus-resteasy` + Jackson |
| ORM | Hibernate Panache + PostgreSQL JDBC |
| Migrations | Flyway (`db/migration`) |
| Segurança | SmallRye JWT + filtro custom `AuthenticationFilter` |
| Testes | JUnit 5, Mockito, RestAssured, JaCoCo em `verify` |
| Nativo | profile Maven `native` (`skipITs` false quando `-Dnative`) |

---

## Estrutura por feature

```
src/main/java/br/com/aguideptbr/features/<feature>/
  *Controller.java    @Path("/api/v1/...")
  *Service.java        @ApplicationScoped, regras, @Transactional (CUD)
  *Repository.java     PanacheRepositoryBase<Model, UUID>
  *Model.java          @Entity, tabela app_*
  dto/
src/test/java/.../features/<feature>/   espelha a feature
```

Features atuais incluem, entre outras: `auth`, `user`, `phone`, `content`, `ownership`, `engagement`, `userchoice`, `userposition`, `usermessage`.

Utilitários compartilhados: `br.com.aguideptbr.util` (ex.: paginação).

---

## Regras de código (não negociáveis)

1. **Injeção por construtor** em Controller/Service/Repository; campos `private final`.
2. Exceção: classes JAX-RS `@Provider` (filtros/mappers) — field injection documentada + `@SuppressWarnings("java:S6813")`.
3. **DTOs:** campos privados + getters/setters; validação Bean Validation (`@Valid` no controller).
4. **Entidades Panache:** campos public permitidos; auditoria `createdAt`, `updatedAt`, `deletedAt`.
5. **Soft delete:** nunca `repository.delete()` para regra de negócio; filtrar `deletedAt is null`.
6. **Controller fino:** sem regra de negócio; log de entrada com JBoss `Logger`.
7. API versionada: **`/api/v1/`**.

---

## Configuração e profiles

Arquivos:

- `src/main/resources/application.properties` — base (datasource, Flyway, JWT paths)
- `application-dev.properties` — dev local
- `application-prod.properties` — VPS
- `src/test/resources/application.properties` — `@QuarkusTest`

Pontos críticos:

- Hibernate **não** gera schema: `quarkus.hibernate-orm.database.generation=none`
- Flyway migra no start; dev pode `clean-at-start=true`; prod **nunca**
- JWT: chaves em `security/jwt-*.pem` (gitignored)
- `quarkus.smallrye-jwt.enabled=true` em runtime; testes excluem `AuthenticationFilter` via `quarkus.arc.exclude-types`

### Bancos (sempre checar antes de subir app)

| Ambiente | Banco | Como |
|----------|-------|------|
| Dev local | `quarkus_dev` | `source .env`, `QUARKUS_PROFILE=dev` |
| Testes | `quarkus_test` | automático em `src/test/resources` |
| Produção | `quarkus_db` | **somente VPS** |

```bash
source .env && echo $QUARKUS_PROFILE && grep DB_DEV_NAME .env
docker ps | grep postgres
./mvnw quarkus:dev
```

Local: app fora do Docker; HTTPS comum em `https://localhost:8443`. Produção: HTTP interno `:8083`, SSL no NGINX.

---

## Comandos do dia a dia

```bash
./mvnw compile                    # após mudanças Java
./mvnw test                       # suite (quarkus_test)
./mvnw test -Dtest=ClasseTest     # focado
./mvnw verify                     # testes + JaCoCo → target/site/jacoco/
./mvnw quarkus:dev                # hot reload
./mvnw package                    # artefato em target/quarkus-app/
```

Dockerfiles: `src/main/docker/` (`Dockerfile.jvm` usual).

---

## Autenticação (contexto)

- Filtro: `features/auth/AuthenticationFilter.java`
- Rotas públicas típicas: `/api/v1/auth/*`, health, swagger em dev
- `@RolesAllowed` depende de JWT parseado (SmallRye) + filtro custom para 401 JSON
- Não logar tokens/senhas; não hardcodar segredos em testes

---

## Testes

- Integração: `@QuarkusTest` + RestAssured
- Unitário: foco em **Service** com mocks de repository
- Casos negativos obrigatórios (401/403/404/409 conforme regra)
- Porta de teste configurada (ex.: 8081) em `src/test/resources/application.properties`
- Meta de cobertura desejada no projeto: ~80% (JaCoCo)

---

## Troubleshooting

| Problema | Ação |
|----------|------|
| Flyway checksum mismatch | Não editar migration aplicada; nova versão `V1.0.X` |
| Column does not exist | Conferir entity `@Column` vs migration mais recente |
| 401 em teste | Filtro excluído? JWT de teste configurado? |
| App subiu no banco errado | `.env`, profile, URL JDBC |
| `quarkus:dev` NPE console | `quarkus.console.enabled=false` já no base properties |
| CORS em dev | `quarkus.http.cors.*` no application.properties |

Logs: JBoss Logger; evitar `System.out`.

---

## Nova feature (ordem sugerida)

1. Buscar feature similar e copiar o **padrão**, não inventar camada paralela.
2. Migration (se persistência nova) — skill `quarkus-db-specialist` + OK do usuário.
3. Model → Repository → Service → DTOs → Controller.
4. Testes na pasta espelhada.
5. `./mvnw compile` && `./mvnw test -Dtest=...`

Rascunhos do agente: apenas `a_error_log_temp/`.

---

## Referências

- `AGENTS.md`
- `.github/composer-instructions.md`
- `DEVELOPMENT_GUIDE.md`
