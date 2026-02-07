# GitHub Copilot - Instruções do Projeto

## Visão Geral
Este é um projeto **Java 17+ com Quarkus 3.x** seguindo arquitetura de camadas (Controller → Service → Repository). Use sempre CDI do Quarkus (`@Inject`, `@ApplicationScoped`) e RESTEasy Reactive para APIs REST.

## 🖥️ Ambiente de Desenvolvimento (CRÍTICO)

### Ambiente LOCAL (macOS/Linux)
- **NÃO usa Docker** para executar a aplicação Quarkus localmente
- Aplicação roda via **terminal direto**: `./mvnw quarkus:dev`
- PostgreSQL roda em **Docker** (container `quarkus_postgres`)
- Aplicação conecta ao banco via `jdbc:postgresql://localhost:5432/quarkus_db`
- Porta local: `https://localhost:8443` (HTTPS com certificado auto-assinado)

### Ambiente PRODUÇÃO (VPS)
- **Usa Docker Compose** (`docker-compose.yml`)
- Aplicação e PostgreSQL em containers separados
- Deploy via Jenkins pipeline automático
- Network bridge para comunicação entre containers

### ⚠️ REGRA IMPORTANTE
**NUNCA assuma** que a aplicação está rodando em Docker localmente. Sempre pergunte ou verifique com `docker ps` e `ps aux | grep quarkus` para identificar o ambiente antes de sugerir comandos de restart ou debug.

## Estrutura de Pacotes OBRIGATÓRIA
```
br.com.aguideptbr/
├── features/          # Funcionalidades de negócio (organizadas por domínio)
│   ├── auth/          # Autenticação e segurança (feature)
│   ├── user/
│   │   ├── UserController.java
│   │   ├── UserService.java
│   │   ├── UserRepository.java
│   │   └── User.java (entidade)
│   └── [outra-feature]/
└── util/              # Utilitários compartilhados
```

---

### 📂 Organização de Arquivos e Diretórios

- **Arquivos de Produção e Estrutura:** O agente tem permissão total para criar e editar arquivos essenciais na raiz do projeto, como `Dockerfile`, `Jenkinsfile`, `pom.xml`, `.gitignore`, e arquivos de configuração.
- **Código Fonte:** A pasta `src/main/java/` é o core do projeto. O agente deve manipular, criar ou refatorar módulos dentro desta pasta conforme as solicitações de desenvolvimento.
- **Arquivos Temporários e de Rascunho (REGRA CRÍTICA):**
  - **Local Obrigatório:** `a_error_log_temp/`
  - Os arquivos de testes devem seguir esse padrão (`src/test/java/br/com/aguideptbr/features/[NOME_DA_FEATURE]/[NOME_ARQUIVO_JAVA]Test.java`),
  ou seja, salvar testes na estrutura correta dentro de `src/test/java/...`. respeitando a organização por features do projeto.
  - Os rascunhos de documentação (`*.md`), arquivos de texto para manipulação de dados ou logs de debug gerados pelo agente **DEVEM** ser criados exclusivamente dentro de `a_error_log_temp/`.
  - **Proibição:** Nunca criar arquivos de "suporte ao raciocínio" ou "testes rápidos" na raiz do projeto. Se não for um arquivo de configuração oficial ou código de produção, ele pertence à `a_error_log_temp/`.

  ## 🤖 Comportamento do Agente na Criação de Arquivos

1. **Identificação de Escopo:** Antes de criar um arquivo, o agente deve classificar:
   - *É essencial para o funcionamento do pipeline ou deploy?* (Ex: `pom.xml`, `Dockerfile`, `Jenkinsfile`) -> **Raiz**.
   - *É um teste, rascunho, dump de dados ou arquivo auxiliar?* -> **a_error_log_temp/**.
2. **Limpeza Automática:** Ao sugerir novos scripts de teste, o agente deve nomeá-los como `a_error_log_temp/test_nome_do_recurso.sh` por padrão.

---

## Convenções de Código

### ✅ Encapsulamento de Campos (Sonar: java:S1104) - CRÍTICO

**REGRA FUNDAMENTAL:** Campos de classe **NUNCA** devem ser `public` (exceto em entidades Panache).

#### ❌ PROIBIDO (viola java:S1104):
```java
public class ErrorResponse {
    public String error;        // ❌ Campo público
    public String message;      // ❌ Campo público
    public LocalDateTime timestamp; // ❌ Campo público
}

public class LoginRequest {
    public String email;        // ❌ Campo público
    public String password;     // ❌ Campo público
}
```

#### ✅ CORRETO (encapsulamento adequado):

**Para DTOs e Classes Utilitárias:**
```java
public class ErrorResponse {
    private String error;       // ✅ Privado
    private String message;     // ✅ Privado
    private LocalDateTime timestamp; // ✅ Privado

    // Construtor
    public ErrorResponse(String error, String message) {
        this.error = error;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

    // Getters obrigatórios (Jackson precisa para serialização JSON)
    public String getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}

public class LoginRequest {
    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email inválido")
    private String email;       // ✅ Privado

    @NotBlank(message = "Senha é obrigatória")
    private String password;    // ✅ Privado

    // Getters e Setters (necessários para Bean Validation e Jackson)
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
```

**Para Constantes:**
```java
public class Constants {
    // ✅ Constantes podem ser public static final
    public static final String TOKEN_TYPE = "Bearer";
    public static final int MAX_ATTEMPTS = 3;
}
```

**Exceção - Entidades Panache:**
```java
@Entity
@Table(name = "users")
public class UserModel extends PanacheEntity {
    // ✅ Panache permite campos public por convenção do framework
    public String name;
    public String email;

    // Mas métodos com lógica devem existir
    public boolean isActive() {
        return deletedAt == null;
    }
}
```

#### 🎯 Benefícios do Encapsulamento:
- **Controle de Acesso:** Define quem pode ler/escrever dados
- **Validação:** Permite adicionar lógica nos setters
- **Debugging:** Facilita rastreamento de mudanças via breakpoints
- **Manutenibilidade:** Mudanças internas não afetam código externo
- **Conformidade Sonar:** Atende java:S1104 e melhora qualidade do código

#### 📋 Checklist ao Criar Classes:
- [ ] Todos os campos são `private` (exceto constantes `static final` e entidades Panache)?
- [ ] Getters estão presentes para todos os campos que precisam ser acessados externamente?
- [ ] Setters estão presentes apenas para campos mutáveis?
- [ ] Bean Validation funciona com getters/setters (`@NotBlank`, `@Email`, etc.)?
- [ ] Jackson consegue serializar/desserializar com getters/setters?

### ✅ Convenção de nomes (Sonar: java:S117)
- **Variáveis locais e parâmetros** devem usar **camelCase** (ex.: `titleText`).
- **Evite snake_case** em variáveis e parâmetros (ex.: `title_txt`).
- **Constantes** podem usar **UPPER_SNAKE_CASE** (ex.: `TOKEN_TYPE`).

### 1. Controllers REST
- Usar `@Path("/api/v1/recurso")` na classe
- Métodos anotados com `@GET`, `@POST`, `@PUT`, `@DELETE`
- Retornar `Response` ou `Uni<Response>` (reactive)
- Validar entrada com Bean Validation (`@Valid`)
- Logs obrigatórios: entrada de request e erros
```java
@Path("/api/v1/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserController {
    @Inject UserService userService;
    @Inject Logger log;

    @GET
    public Response findAll() {
        log.info("GET /api/v1/users - Listing all users");
        return Response.ok(userService.findAll()).build();
    }
}
```

### 2. Services
- Anotados com `@ApplicationScoped`
- Contém lógica de negócio
- Injeta repositories com `@Inject`
- Transações com `@Transactional` quando necessário
```java
@ApplicationScoped
public class UserService {
    @Inject UserRepository userRepository;
    @Inject Logger log;

    @Transactional
    public User create(User user) {
        // lógica de negócio
    }
}
```

### 3. Repositories
- Estender `PanacheRepository<Entity>` ou usar `PanacheEntity`
- Métodos de consulta customizados seguem padrão `findByXxx`
- Não colocar lógica de negócio aqui
```java
@ApplicationScoped
public class UserRepository implements PanacheRepository<User> {
    public User findByEmail(String email) {
        return find("email", email).firstResult();
    }
}
```

### 4. Entidades
- Herdar de `PanacheEntity` (id gerado automaticamente) OU usar `PanacheEntityBase` com `@Id` customizado
- Usar `@Entity`, `@Table`, `@Column`
- Sempre incluir campos de auditoria:
```java
@Entity
@Table(name = "users")
public class User extends PanacheEntity {
    @Column(nullable = false, length = 100)
    public String name;

    @Column(unique = true, nullable = false)
    public String email;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    public LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    public LocalDateTime updatedAt;
}
```

## Tratamento de Exceções
- Usar `@ServerExceptionMapper` para tratamento global
- Nunca expor stacktraces para o cliente em produção
- Retornar JSON estruturado:
```java
{
  "error": "User not found",
  "message": "Usuário com ID 123 não encontrado",
  "timestamp": "2026-01-31T10:30:00Z"
}
```

## Logging
- Injetar `Logger` do JBoss: `@Inject Logger log;`
- Níveis: `log.info()` para operações normais, `log.error()` para erros, `log.debug()` para debug
- Sempre logar: início de operações importantes, erros com stacktrace, dados sensíveis NÃO devem ser logados
- **Proibido usar `System.out/err`** (Sonar: Replace this use of System.out by a logger)

## Configurações
- Usar `application.properties` para configurações comuns
- Usar `application-dev.properties` e `application-prod.properties` para ambientes específicos
- Acessar configs com `@ConfigProperty(name = "key") String value;`

---

## ⚠️ PROTEÇÃO DO BANCO DE DADOS DE PRODUÇÃO (CRÍTICO)

### 🚨 REGRAS INVIOLÁVEIS - BANCO DE DADOS PRINCIPAL

O banco de dados de produção (`jdbc:postgresql://quarkus_postgres:5432/quarkus_db`) **JAMAIS** deve ser destruído ou recriado. Esta é uma regra **ABSOLUTA** e **NÃO NEGOCIÁVEL**.

#### 🔴 Configurações PROIBIDAS em Produção:
```properties
# ❌ NUNCA USE ISSO EM PRODUÇÃO:
quarkus.flyway.clean-at-start=true
quarkus.hibernate-orm.database.generation=drop
quarkus.hibernate-orm.database.generation=drop-and-create
quarkus.hibernate-orm.database.generation=create
quarkus.hibernate-orm.database.generation=create-drop
```

#### ✅ Configurações OBRIGATÓRIAS para Produção:
```properties
# ✅ SEMPRE USE EM PRODUÇÃO (application-prod.properties):
quarkus.hibernate-orm.database.generation=none
quarkus.flyway.clean-at-start=false
quarkus.flyway.migrate-at-start=true
quarkus.flyway.baseline-on-migrate=true
```

#### ✅ Configurações PERMITIDAS para Desenvolvimento:
```properties
# ✅ PERMITIDO EM application-dev.properties:
quarkus.hibernate-orm.database.generation=none
quarkus.flyway.clean-at-start=true  # OK para develop branch
quarkus.flyway.migrate-at-start=true
```

### 🛡️ Proteção por Branch

#### Branch `main` (PRODUÇÃO):
- **SEMPRE** usar profile `prod` no `docker-compose.yml`: `QUARKUS_PROFILE=prod`
- **NUNCA** permitir `clean-at-start=true` em merges para main
- **VERIFICAR** `application-prod.properties` antes de cada merge
- **APENAS** migrations incrementais não-destrutivas são permitidas

#### Branch `develop-data-objects` (DESENVOLVIMENTO):
- **PERMITIDO** usar `clean-at-start=true` para desenvolvimento
- **PERMITIDO** recriar banco de dados localmente para testes
- **OBRIGATÓRIO** revisar configurações antes de fazer PR para main

### ✅ Checklist Antes de Merge develop → main

**ANTES de criar PR de develop para main, VERIFICAR:**

1. [ ] `application-prod.properties` tem `quarkus.flyway.clean-at-start=false`
2. [ ] `application-prod.properties` tem `quarkus.hibernate-orm.database.generation=none`
3. [ ] `docker-compose.yml` usa `QUARKUS_PROFILE=prod`
4. [ ] Nenhuma migration contém `DROP DATABASE`, `DROP SCHEMA` ou `TRUNCATE`
5. [ ] Todas as migrations são incrementais (apenas `ALTER TABLE ADD`, `CREATE INDEX`, etc.)
6. [ ] Testou a migration localmente sem `clean-at-start`

### 📋 Formato de Migrations Seguras

✅ **PERMITIDO** (não-destrutivo):
```sql
-- V1.0.5__Add_status_column.sql
ALTER TABLE content_records ADD COLUMN status VARCHAR(20);
UPDATE content_records SET status = 'ACTIVE' WHERE status IS NULL;
ALTER TABLE content_records ALTER COLUMN status SET NOT NULL;

CREATE INDEX idx_content_status ON content_records(status);
```

❌ **PROIBIDO** (destrutivo):
```sql
-- ❌ NUNCA FAÇA ISSO EM PRODUÇÃO:
DROP TABLE content_records;
TRUNCATE TABLE users;
DROP SCHEMA public CASCADE;
ALTER TABLE content_records DROP COLUMN important_data;
```

### 🚨 O Que Acontece Se Violar Esta Regra?

**CONSEQUÊNCIAS CATASTRÓFICAS:**
- Perda total de dados de produção
- Downtime da aplicação
- Perda de confiança dos usuários
- Impossibilidade de recuperação (sem backup)

### 🔧 Como Recuperar Se Banco Foi Destruído?

1. **Parar imediatamente** a aplicação
2. **Restaurar** do último backup disponível
3. **Verificar** as configurações antes de reiniciar
4. **Nunca** fazer deploy sem revisar configs

### 📝 Ao Criar Novas Features

**SEMPRE pergunte:**
- "Esta migration é incremental e não-destrutiva?"
- "Testei sem `clean-at-start=true`?"
- "A configuração de produção está protegida?"

**NUNCA assuma:**
- Que o Hibernate vai "gerenciar" o schema em produção
- Que `clean-at-start` está desabilitado por padrão
- Que o profile correto será usado automaticamente

### 🤖 GitHub Actions e CI/CD (CRÍTICO)

**PROBLEMA IDENTIFICADO:**
O GitHub Actions pode causar perda de dados se não validar o profile antes do deploy!

**Verificações OBRIGATÓRIAS no workflow de deploy:**
```yaml
- name: ⚠️ Verificar configuração de produção
  run: |
    grep -q "quarkus.flyway.clean-at-start=false" src/main/resources/application-prod.properties || exit 1
    grep -q "quarkus.hibernate-orm.database.generation=none" src/main/resources/application-prod.properties || exit 1
    echo "✅ Configurações de produção verificadas"

- name: ⚠️ Validar docker-compose.yml no VPS
  script: |
    cd /opt/apps/aguide-api-quarkus
    grep -q "QUARKUS_PROFILE=prod" docker-compose.yml || (echo "❌ PROFILE INCORRETO!" && exit 1)
    echo "✅ Profile de produção confirmado"
```

**NUNCA no deploy de produção:**
- ❌ `docker compose down` sem verificar volumes persistentes
- ❌ `docker compose build --no-cache` sem validar configurações
- ❌ Deploy sem confirmar `QUARKUS_PROFILE=prod`
- ❌ Rebuild de banco de dados (usar apenas migrations)

**Comando SEGURO para deploy:**
```bash
cd /opt/apps/aguide-api-quarkus
git pull origin main
# Verifica profile antes de qualquer operação
grep -q "QUARKUS_PROFILE=prod" docker-compose.yml || exit 1
# Apenas atualiza o serviço da aplicação (não toca no postgres)
docker compose up -d --no-deps --build aguide-api
docker system prune -f
```

---

## Migrations de Banco de Dados
- Usar Flyway em `src/main/resources/db/migration/`
- Nomenclatura: `V[major].[minor].[patch]__[Description].sql`
- Exemplo: `V1.0.3__Add_user_role_column.sql`
- **NUNCA modificar migrations já aplicadas**
- **PostgreSQL em Produção e Testes**: Mesmas migrations são usadas em ambos ambientes (quarkus_db e quarkus_test)
- **SEMPRE usar `ON CONFLICT DO NOTHING`** para INSERTs de dados iniciais (idempotência)

## Testes
- Localização: `src/test/java/br/com/aguideptbr/features/[feature]/`
- Usar `@QuarkusTest` para testes de integração
- Usar `RestAssured` para testar endpoints
- Cobertura mínima desejada: 80%

### Boas práticas de testes unitários (FOCO)
- **Foque na regra de negócio** (Service) e nos fluxos críticos.
- **Isole dependências** com mocks (Repository, gateways externos).
- **Testes negativos são obrigatórios**: validar erros/exceções esperadas.
- **Evite testes fracos** (getters/setters sem lógica e duplicação da implementação).
- **Determinismo**: sem dependência de data/hora real, rede, ordem de execução.
- **Se o teste precisar de `@QuarkusTest`**, provavelmente é integração, não unitário.

### Quando criar testes unitários
- Regras com múltiplas ramificações (if/else, validações, autorização).
- Cálculos, transformações e normalizações.
- Bugs recorrentes (testes evitam regressão).
- Casos de erro esperados (ex.: senha inválida, recurso inexistente).

### Configuração de Testes (CRÍTICO)
**SEMPRE criar `src/test/resources/application.properties` com:**
```properties
# Desabilita AuthenticationFilter em testes
quarkus.arc.exclude-types=br.com.aguideptbr.features.auth.AuthenticationFilter

# Desabilita JWT em testes (evita erro de chave pública não encontrada)
quarkus.smallrye-jwt.enabled=false

# Usa PostgreSQL com banco dedicado para testes (quarkus_test)
quarkus.datasource.db-kind=postgresql
quarkus.datasource.jdbc.url=${QUARKUS_DATASOURCE_JDBC_URL:jdbc:postgresql://quarkus_postgres:5432/quarkus_test}
quarkus.datasource.username=${QUARKUS_DATASOURCE_USERNAME:quarkus}
quarkus.datasource.password=${QUARKUS_DATASOURCE_PASSWORD:quarkus123}

# Flyway em testes - USA MESMAS MIGRATIONS DE PRODUÇÃO
quarkus.flyway.clean-at-start=true
quarkus.flyway.migrate-at-start=true
# Location padrão: classpath:db/migration (não precisa especificar)
```

**Importante sobre Migrations:**
- Produção e testes usam **PostgreSQL** (quarkus_db e quarkus_test)
- **MESMAS migrations** são usadas em ambos ambientes
- Flyway executa `clean-at-start=true` em testes para garantir ambiente limpo
- Não é necessário criar migrations separadas ou adaptar sintaxe

**Importante sobre JWT em Testes:**
- **SEMPRE** configurar `quarkus.smallrye-jwt.enabled=false` em testes
- Isso desabilita completamente a extensão SmallRye JWT, evitando tentativas de carregar chaves
- Combinado com `quarkus.arc.exclude-types` do AuthFilter, garante que testes rodem sem autenticação

### Regras de Testes
✅ **PERMITIDO:**
- Desabilitar filtros de autenticação via `quarkus.arc.exclude-types`
- Usar PostgreSQL com banco dedicado `quarkus_test` (isolado de produção)
- RestAssured sem headers de autenticação em testes
- Flyway `clean-at-start=true` para garantir ambiente limpo a cada teste

❌ **PROIBIDO:**
- Hardcoded tokens/senhas no código de teste
- Usar `-DskipTests` no Jenkins/CI (testes são barreira de qualidade)
- Pular testes para "resolver rápido" problemas de autenticação
- Conectar em `quarkus_db` (produção) durante testes - SEMPRE usar `quarkus_test`
- Criar migrations separadas para testes (usar as mesmas de produção)

## Segurança

### Autenticação JWT (CRÍTICO - Lições Aprendidas)
- **Implementação MANUAL de JWT**: Não usar SmallRye JWT Builder (`io.smallrye.jwt.build.Jwt`)
- **Razão**: SmallRye JWT tem problemas de parsing com chaves RSA PKCS#8 geradas por OpenSSL
- **Solução Atual**: Assinatura JWT manual usando `java.security.Signature` em `JWTService.java`
- **Formato da Chave**: PKCS#8 inline no `application.properties` via `mp.jwt.sign.key-content`

#### Geração de Chaves JWT (Comando Correto)
```bash
# Gera chave privada RSA 2048 bits em formato PKCS#8
openssl genpkey -algorithm RSA -out security/jwt-private.pem -pkeyopt rsa_keygen_bits:2048

# Extrai chave pública
openssl rsa -pubout -in security/jwt-private.pem -out security/jwt-public.pem

# Define permissões corretas
chmod 600 security/jwt-private.pem
chmod 644 security/jwt-public.pem
```

#### Estrutura do Token JWT
- **Header:** `{"alg": "RS256", "typ": "JWT"}`
- **Payload:** Claims (iss, sub, upn, email, name, surname, groups, iat, exp)
- **Signature:** SHA256withRSA usando chave privada
- **Formato Final:** `base64url(header).base64url(payload).base64url(signature)`

#### Configuração de Segurança
- `AuthenticationFilter` valida tokens JWT em requests
- `@RolesAllowed` para controle de acesso baseado em roles
- **Nunca comitar:** chaves privadas, credenciais, tokens
- **Chaves em Produção:** Usar variáveis de ambiente ou secrets manager

#### Credenciais de Teste (Desenvolvimento)
- Email: `contato@aguide.space`
- Senha: `admin123`
- Role: `ADMIN`
- Hash BCrypt: `$2a$10$1b.v1jTmdr.c1XJXM10bsO.YwcpgZkXszAivtIL6VgfUQF2RhMIBy`

**Documentação Completa:** Ver `a_error_log_temp/SAGA_JWT_AUTHENTICATION_FIX.md`

## Docker
- Dockerfiles em `src/main/docker/`
- Preferir `Dockerfile.jvm` para desenvolvimento
- `Dockerfile.native` para produção (GraalVM)

## CI/CD
- Jenkins configurado (ver `Jenkinsfile`)
- SonarQube integrado para análise de código
- Build Maven: `./mvnw clean package`

## O QUE NÃO FAZER
❌ Criar arquivos temporários na raiz do projeto
❌ Colocar lógica de negócio em Controllers ou Repositories
❌ Usar anotações do Spring (usar Quarkus CDI)
❌ Esquecer `@Transactional` em métodos que modificam dados
❌ Criar packages fora de `br.com.aguideptbr`
❌ Ignorar tratamento de exceções
❌ Logar informações sensíveis (senhas, tokens)
❌ Hardcoded credenciais/tokens em testes
❌ Pular testes no CI/CD com `-DskipTests`
❌ Usar banco real (PostgreSQL) em testes unitários
❌ **JAMAIS** usar `quarkus.flyway.clean-at-start=true` em produção
❌ **JAMAIS** usar `quarkus.hibernate-orm.database.generation` diferente de `none` em produção
❌ **JAMAIS** criar migrations destrutivas (`DROP TABLE`, `TRUNCATE`) para produção
❌ **JAMAIS** fazer merge develop→main sem verificar configurações de banco de dados
❌ **JAMAIS** assumir que o profile correto será usado automaticamente

## Recursos do Quarkus a Utilizar
✅ Dev Mode: `./mvnw quarkus:dev` (hot reload automático)
✅ Dev Services: bancos de dados automaticamente em containers
✅ Panache: simplificação de JPA/Hibernate
✅ RESTEasy Reactive: performance melhorada
✅ SmallRye Health: endpoints `/q/health`
✅ OpenAPI/Swagger: `/q/swagger-ui`

## Comandos Git e Interação com o Usuário

- Sempre que o agente for sugerir comandos Git que possam alterar o estado da branch local ou remota, como `git commit`, `git push`, `git reset`, `git rebase`, `git pull --rebase`, `git push --force` ou similares, ele deve **obrigatoriamente perguntar ao usuário desenvolvedor** se pode prosseguir com a execução desses comandos.
- O agente deve alertar o usuário sobre o potencial risco de "bagunçar" a branch atual, explicando que esses comandos podem modificar o histórico ou o conteúdo da branch local e remota.
- Somente após a confirmação explícita do usuário, o agente deve sugerir ou executar comandos Git que alterem a branch local ou remota.
- Para comandos Git que não alterem o estado da branch (como `git status`, `git log`, `git diff`), o agente pode sugerir ou executar sem necessidade de confirmação.

### Adição de Arquivos ao Stage (git add)

- **Em hipótese alguma** o agente deve sugerir comandos de adição em lote como `git add .`, `git add -A`, ou `git add --all`.
- Todos os arquivos devem ser adicionados individualmente usando `git add <caminho-do-arquivo>` após serem explicitamente listados e revisados com o usuário.
- Isso evita a inclusão acidental de arquivos temporários, logs, credenciais ou outros artefatos indesejados no commit.

Exemplo de comportamento esperado:

Usuário: "Adicione minhas alterações e faça commit."

Agente: "Vou adicionar os seguintes arquivos individualmente:
- `src/main/java/br/com/aguideptbr/features/user/UserService.java`
- `src/test/java/br/com/aguideptbr/features/user/UserServiceTest.java`

Confirma a adição desses arquivos ao stage?"

Usuário: "Sim."

Agente:
``bash
git add src/main/java/br/com/aguideptbr/features/user/UserService.java
git add src/test/java/br/com/aguideptbr/features/user/UserServiceTest.java
``

Agora vou fazer commit das suas alterações. Isso irá modificar o histórico da branch local. Deseja continuar?

Usuário: "Sim."

Agente:
``bash
git commit -m "feat(user): implementa nova funcionalidade X"
``

---
**Importante:** Ao gerar código, sempre verificar se está seguindo estas diretrizes. Em caso de dúvida, consultar o arquivo `DEVELOPMENT_GUIDE.md` na raiz do projeto.
