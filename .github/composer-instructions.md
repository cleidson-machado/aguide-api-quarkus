# Cursor Composer — Instruções do Projeto (mobile-rest-api)

Documento canônico para o **Agent/Composer** no Cursor IDE neste repositório.  
Espelha e estende [copilot-instructions.md](copilot-instructions.md), alinhado a [AGENTS.md](../AGENTS.md) na raiz.

**Prioridade em conflito:** regras de banco/ produção > constructor injection > exemplos antigos no código legado.

---

## 1. Papel do Composer neste projeto

Você atua como engenheiro backend no **mobile-rest-api** (Quarkus 3.23.3, Java 17+, PostgreSQL, Maven `./mvnw`).

### 1.1 O que fazer sempre

- Ler código existente na feature antes de criar padrões novos (`br.com.aguideptbr.features.*`).
- Executar verificação quando alterar lógica: `./mvnw compile` e, quando pertinente, `./mvnw test` ou testes focados.
- Respeitar **isolamento de bancos** (seção 2) antes de sugerir ou rodar Quarkus/Flyway.
- Usar **injeção por construtor** em Controller/Service/Repository (exceção: `@Provider` JAX-RS).
- Pedir confirmação explícita antes de: commit, push, migrations novas, mudanças em CI/Docker/prod, dependências no `pom.xml`.
- Commits **somente** quando o usuário pedir; nunca `git add .` / `git add -A`.
- Branches: não commitar em `main`/`master`; sugerir branch `feat/`, `fix/`, `chore/` e **perguntar** antes de criar/trocar branch.

### 1.2 Skills e agentes do repositório

Antes de implementar no domínio correspondente, **ler o arquivo inteiro**:

| Domínio | Skill Cursor (`.cursor/skills/`) |
|--------|----------------------------------|
| Git, PR, CI/CD, deploy | `git-cicd` |
| Quarkus, features, testes | `quarkus-java` |
| PostgreSQL, Flyway, entidades | `quarkus-db-specialist` |

Agentes legados Copilot: [usermessage-backend.agent.md](agents/usermessage-backend.agent.md), [usermessage-implementation/SKILL.md](skills/usermessage-implementation/SKILL.md).

No chat Cursor, o usuário pode pedir: *“Siga o Quarkus DB Specialist para…”* — nesse caso, abra o SKILL e siga o checklist.

### 1.3 Documentos irmãos

- **Resumo operacional:** [AGENTS.md](../AGENTS.md)
- **Referência Copilot (detalhe histórico/incidentes):** [copilot-instructions.md](copilot-instructions.md)
- **Templates de código:** [DEVELOPMENT_GUIDE.md](../DEVELOPMENT_GUIDE.md), [QUICK_START.md](../QUICK_START.md)

---

## 2. Separação de bancos de dados (CRÍTICO)

### 2.1 Contexto

O projeto teve **perdas em produção** por profile/Flyway apontando para o banco errado. Existem **3 bancos**:

| Banco | Uso | Profile | Flyway `clean-at-start` |
|-------|-----|---------|---------------------------|
| `quarkus_db` | **Produção (VPS only)** | `prod` | **false (obrigatório)** |
| `quarkus_dev` | Dev local (Mac/Linux) | `dev` | true permitido |
| `quarkus_test` | `./mvnw test` | test (via `src/test/resources`) | false (DB gerenciado externamente) |

Configs: [application-prod.properties](../src/main/resources/application-prod.properties), [application-dev.properties](../src/main/resources/application-dev.properties), [src/test/resources/application.properties](../src/test/resources/application.properties).

### 2.2 Regras invioláveis

**Nunca:**

- Conectar a `quarkus_db` na máquina local.
- `clean-at-start=true` com `QUARKUS_PROFILE=prod`.
- Commitar `.env` ou conteúdo de `security/`.
- Rodar `./mvnw quarkus:dev` sem `source .env` e sem checar profile.

**Sempre:**

```bash
source .env
echo $QUARKUS_PROFILE          # dev para local
grep DB_DEV_NAME .env          # quarkus_dev
docker ps | grep postgres
./mvnw quarkus:dev
```

Testes: `./mvnw test` → `quarkus_test` apenas.

Antes de PR/merge: `./validate-production-safety.sh` e `clean-at-start=false` em prod.

Incidentes: [a_error_log_temp/INCIDENT_PROD_DB_RESET_2026-02-09.md](../a_error_log_temp/INCIDENT_PROD_DB_RESET_2026-02-09.md), [a_error_log_temp/INCIDENT_CONTAINER_RESTART_LOOP_2026-02-19.md](../a_error_log_temp/INCIDENT_CONTAINER_RESTART_LOOP_2026-02-19.md).

---

## 3. Ambiente de execução

### 3.1 Local (macOS/Linux)

- Quarkus **fora** do Docker: `./mvnw quarkus:dev`
- PostgreSQL no Docker (`quarkus_postgres`)
- JDBC dev: `localhost:5432/quarkus_dev`
- HTTPS local comum: `https://localhost:8443` (keystore em `security/` se SSL local ativo)

### 3.2 Produção (VPS)

- Docker Compose: [docker-compose.yml](../docker-compose.yml)
- Jenkins: `Jenkinsfile`, `Jenkinsfile.test`, `Jenkinsfile.production`
- **Não assumir** que a API local está em container — verificar:

```bash
docker ps | grep mobile-rest-api
ps aux | grep quarkus
```

---

## 4. Arquitetura

### 4.1 Camadas (por feature)

```
src/main/java/br/com/aguideptbr/features/<feature>/
├── *Controller.java
├── *Service.java
├── *Repository.java
├── *Model.java
└── dto/
```

- Controller: HTTP, validação `@Valid`, logs de entrada, **sem** regra de negócio.
- Service: `@ApplicationScoped`, regras, `@Transactional` só em CUD.
- Repository: Panache, queries; **sem** `@Transactional`, **sem** regra de negócio.

### 4.2 Convenções de API

- Prefixo: `/api/v1/...`
- UUID como PK (`PanacheEntityBase` + `@GeneratedValue`)
- Soft delete: `deletedAt`; consultas ativas: `deletedAt is null`
- Paginação: Panache `Page` + [PaginatedResponse](../src/main/java/br/com/aguideptbr/util/PaginatedResponse.java)

### 4.3 Organização de arquivos

- Código produção: `src/main/java/`
- Testes: `src/test/java/br/com/aguideptbr/features/<feature>/`
- Rascunhos, logs de debug do agente, MD temporários: **`a_error_log_temp/`** — não na raiz do projeto.

---

## 5. Código — regras obrigatórias

### 5.1 Injeção de dependências (Sonar java:S6813)

**Constructor injection** em Controller, Service, Repository, utilitários:

```java
@ApplicationScoped
public class UserService {
    private final Logger log;
    private final UserRepository userRepository;

    public UserService(Logger log, UserRepository userRepository) {
        this.log = log;
        this.userRepository = userRepository;
    }
}
```

`@ConfigProperty` no construtor. **Exceção:** classes `@Provider` (filters, mappers) — field injection + `@SuppressWarnings("java:S6813")` + comentário RESTEasy.

Referência: [UserController.java](../src/main/java/br/com/aguideptbr/features/user/UserController.java).

### 5.2 Encapsulamento (Sonar java:S1104)

- DTOs: campos `private`, getters/setters.
- Entidades Panache: campos `public` permitidos pela convenção do framework.
- Constantes: `public static final` OK.

### 5.3 Naming (Sonar java:S117)

- Variáveis/parâmetros: `camelCase`
- Constantes: `UPPER_SNAKE_CASE`

### 5.4 REST Controller (padrão alvo)

```java
@Path("/api/v1/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserController {
    private final UserService userService;
    private final Logger log;

    public UserController(UserService userService, Logger log) {
        this.userService = userService;
        this.log = log;
    }

    @GET
    public Response findAll() {
        log.info("GET /api/v1/users - Listing all users");
        return Response.ok(userService.findAll()).build();
    }
}
```

Status HTTP corretos; erros estruturados via service/exceções mapeadas.

### 5.5 Service

```java
@ApplicationScoped
public class UserService {
    private final UserRepository userRepository;
    private final Logger log;

    public UserService(UserRepository userRepository, Logger log) {
        this.userRepository = userRepository;
        this.log = log;
    }

    @Transactional
    public UserModel create(/* ... */) {
        // regra de negócio
    }
}
```

### 5.6 Repository

```java
@ApplicationScoped
public class UserRepository implements PanacheRepositoryBase<UserModel, UUID> {
    public UserModel findByEmail(String email) {
        return find("email = ?1 and deletedAt is null", email).firstResult();
    }
}
```

### 5.7 Entidade (auditoria + soft delete)

Campos típicos: `id` (UUID), `createdAt`, `updatedAt`, `deletedAt`. Hibernate **não** gera schema: `quarkus.hibernate-orm.database.generation=none`.

---

## 6. Autenticação e segurança

- Filtro JWT custom: [AuthenticationFilter.java](../src/main/java/br/com/aguideptbr/features/auth/AuthenticationFilter.java)
- Rotas públicas: `/api/v1/auth/*`, `/q/health`, `/q/swagger-ui` (dev)
- Chaves em `security/` (gitignored): `jwt-private.pem`, `jwt-public.pem`
- Geração: `./generate-jwt-keys.sh` ou OpenSSL (ver copilot-instructions)
- **Não** logar senhas/tokens; **não** commitar segredos
- Testes: excluir filtro via `quarkus.arc.exclude-types` em [src/test/resources/application.properties](../src/test/resources/application.properties)

Credenciais de dev documentadas no copilot-instructions — usar só em ambiente local.

---

## 7. Banco de dados e Flyway

- Pasta: `src/main/resources/db/migration/`
- Nome: `V{major}.{minor}.{patch}__{PascalCase_Description}.sql`
- **Nunca** editar migration já aplicada; criar nova versão.
- Produção: apenas mudanças **não destrutivas** (`ALTER ADD`, índices, `UPDATE` seguro).
- Proibido em prod: `DROP TABLE`, `TRUNCATE`, `DROP COLUMN` crítico.
- INSERTs seed: idempotência (`ON CONFLICT DO NOTHING`, `IF NOT EXISTS`).
- Prod: `clean-at-start=false`, `migrate-at-start=true`, `baseline-on-migrate=true`.

**Composer:** criar migration só com acordo do usuário; seguir skill Quarkus DB Specialist.

---

## 8. Exceções, logging, configuração

- Mappers globais (`@ServerExceptionMapper` / mappers JAX-RS); sem stack trace ao cliente em prod.
- Logger JBoss via construtor; proibido `System.out` / `System.err`.
- Config: `application.properties` + profile dev/prod; `@ConfigProperty` no construtor.

---

## 9. Testes

- Integração: `@QuarkusTest`, RestAssured, pacote espelhando a feature.
- Foco unitário: regras em **Service**, mocks de repository, casos negativos.
- Meta de cobertura desejada: 80% (JaCoCo via `./mvnw verify`).
- **Proibido:** `-DskipTests` em CI; hardcode de tokens; apontar testes para `quarkus_db`.
- Mesmas migrations de prod no `quarkus_test`.

---

## 10. CI/CD, Docker e SSL/TLS (produção)

### 10.1 Deploy seguro

- Validar `application-prod.properties` e `QUARKUS_PROFILE=prod` no compose.
- Evitar `docker compose down` em prod sem revisar volumes.
- Atualização típica: `docker compose up -d --no-deps --build aguide-api` após checks.

Workflow exemplo: [.github/workflows/deploy.yml](workflows/deploy.yml).

### 10.2 SSL no VPS (lição crítica)

Arquitetura correta:

```
Cliente (HTTPS) → NGINX (terminação SSL) → aguide-api (HTTP :8083)
```

- **Sem** SSL dentro do container Quarkus em produção.
- Env: `QUARKUS_HTTP_PORT=8083`, `QUARKUS_HTTP_SSL_PORT` **vazio**, `QUARKUS_HTTP_INSECURE_REQUESTS=enabled`.
- Portas: `127.0.0.1:8083:8083`
- Volume: `./security:/deployments/security:ro` (JWT PEMs; `keystore.p12` opcional em prod)
- Health: `curl http://localhost:8083/api/v1/auth/health`

Dev local pode usar HTTPS :8443 com keystore; prod usa HTTP interno.

Checklist completo e exemplo de `docker-compose.yml`: seção equivalente em [copilot-instructions.md](copilot-instructions.md) (Docker e SSL/TLS).

---

## 11. Git e entrega (Cursor)

- `git status`, `git diff`, `git log`: livre.
- `commit`, `push`, `rebase`, `reset`, `push --force`: **só após confirmação**.
- Stage arquivo a arquivo: `git add caminho/arquivo` — listar arquivos antes.
- Conventional Commits: `type(scope): descrição imperativa`.
- Nunca commitar: `.env`, `security/*`, `target/`, artefatos de build.

---

## 12. O que NÃO fazer (resumo)

| Área | Proibições |
|------|------------|
| DB | `quarkus_db` local, `clean-at-start` em prod, migrations destrutivas, alterar migration aplicada |
| Docker/SSL | SSL no container prod, expor 8083 sem localhost, profile dev no VPS |
| Código | Lógica no controller, `@Inject` em fields (exceto `@Provider`), Spring, delete físico |
| Testes/CI | skip tests, credenciais hardcoded |
| Repo | arquivos temp na raiz, `git add .`, commit sem pedido |

Lista expandida: final de [copilot-instructions.md](copilot-instructions.md).

---

## 13. Arquivos de configuração chave

| Arquivo | Função |
|---------|--------|
| [pom.xml](../pom.xml) | Quarkus 3.23.3, dependências |
| [application.properties](../src/main/resources/application.properties) | Base |
| [application-dev.properties](../src/main/resources/application-dev.properties) | Dev |
| [application-prod.properties](../src/main/resources/application-prod.properties) | Prod |
| [src/test/resources/application.properties](../src/test/resources/application.properties) | Testes |
| [docker-compose.yml](../docker-compose.yml) | VPS |
| [.env.example](../.env.example) | Template env local |
| [validate-production-safety.sh](../validate-production-safety.sh) | Gate pré-merge |

---

## 14. Critérios de “pronto”

- Padrões da feature existente respeitados.
- Constructor injection + DTOs encapsulados.
- Soft delete e queries filtradas.
- `./mvnw compile` OK; testes relevantes OK.
- Sem vazamento de segredo/PII em logs ou respostas.
- Mudanças de contrato API ou migration explicitadas ao usuário.

---

## 15. Recursos Quarkus

- Dev: `./mvnw quarkus:dev`, Dev UI `/q/dev/` (dev)
- Health: `/q/health`
- OpenAPI: `/q/swagger-ui` (dev)
- Guias: https://quarkus.io/guides/

---

**Última atualização:** 2026-10-24  
**Mantido por:** equipe de desenvolvimento  
**Assistentes Cursor:** leia este arquivo + [AGENTS.md](../AGENTS.md) no início de tarefas não triviais.
