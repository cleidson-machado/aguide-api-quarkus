# mobile-rest-api

This project uses Quarkus, the Supersonic Subatomic Java Framework.

and is generated using the [Quarkus Maven Plugin](https://quarkus.io/guides/maven-tooling).

If you want to learn more about Quarkus, please visit its website: <https://quarkus.io/>.

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:

```shell script
./mvnw quarkus:dev
```

> **_NOTE:_**  Quarkus now ships with a Dev UI, which is available in dev mode only at <http://localhost:8080/q/dev/>.

## Packaging and running the application

The application can be packaged using:

```shell script
./mvnw package
```

It produces the `quarkus-run.jar` file in the `target/quarkus-app/` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/quarkus-app/lib/` directory.

The application is now runnable using `java -jar target/quarkus-app/quarkus-run.jar`.

If you want to build an _über-jar_, execute the following command:

```shell script
./mvnw package -Dquarkus.package.jar.type=uber-jar
```

The application, packaged as an _über-jar_, is now runnable using `java -jar target/*-runner.jar`.

## Creating a native executable

You can create a native executable using:

```shell script
./mvnw package -Dnative
```

Or, if you don't have GraalVM installed, you can run the native executable build in a container using:

```shell script
./mvnw package -Dnative -Dquarkus.native.container-build=true
```

You can then execute your native executable with: `./target/mobile-rest-api-1.0.0-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult <https://quarkus.io/guides/maven-tooling>.

## Related Guides

- Hibernate ORM with Panache ([guide](https://quarkus.io/guides/hibernate-orm-panache)): Simplify your persistence code for Hibernate ORM via the active record or the repository pattern
- RESTEasy Classic ([guide](https://quarkus.io/guides/resteasy)): REST endpoint framework implementing Jakarta REST and more
- JDBC Driver - PostgreSQL ([guide](https://quarkus.io/guides/datasource)): Connect to the PostgreSQL database via JDBC

## Provided Code

### Hibernate ORM

Create your first JPA entity

[Related guide section...](https://quarkus.io/guides/hibernate-orm)

[Related Hibernate with Panache section...](https://quarkus.io/guides/hibernate-orm-panache)


### RESTEasy JAX-RS

Easily start your RESTful Web Services

[Related guide section...](https://quarkus.io/guides/getting-started#the-jax-rs-resources)

---

## ✅ Cobertura de Testes (JaCoCo + SonarQube)

Este projeto gera cobertura com **JaCoCo** e publica no **SonarQube**.

### Como gerar o relatório local

```bash
./mvnw verify
```

Arquivos gerados:
- **XML (para Sonar):** `target/site/jacoco/jacoco.xml`
- **HTML (visualização local):** `target/site/jacoco/index.html`

> O **HTML é opcional** e serve apenas para leitura local. O Sonar usa o XML.

### Configuração no SonarQube (UI)

Em **Project Settings → JaCoCo**, preencha:

```
target/site/jacoco/jacoco.xml
```

O segundo campo pode ficar em branco.

### Configuração via pipeline (Jenkins)

No pipeline, já enviamos o caminho do XML:

```
-Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml
```

> Se usar a configuração via Jenkins, a configuração na UI é opcional (evite duplicidade).


---

## 🔧 Troubleshooting - Problemas Comuns no Ambiente Local

Esta seção documenta problemas recorrentes no ambiente de desenvolvimento local e suas soluções.

### ❌ Problema 1: "Port already bound: 8080: Address already in use"

**Sintoma:**
```
io.quarkus.runtime.QuarkusBindException: Port already bound: 8080: Address already in use
BUILD FAILURE
```

**Causa:**
Outra instância do Quarkus (ou outro processo) está usando a porta 8080.

**Soluções:**

#### Opção 1: Matar o processo na porta 8080 (macOS/Linux)
```bash
# Descobrir qual processo está usando a porta
lsof -i :8080

# Matar o processo (substituir <PID> pelo número encontrado)
kill -9 <PID>

# Ou matar diretamente
lsof -ti :8080 | xargs kill -9
```

#### Opção 2: Usar outra porta
```bash
# Rodar na porta 8081 por exemplo
./mvnw quarkus:dev -Dquarkus.http.port=8081
```

---

### ❌ Problema 2: "Acquisition timeout while waiting for new connection" (Database)

**Sintoma:**
```
FlywaySqlException: Unable to obtain connection from database:
Acquisition timeout while waiting for new connection
BUILD FAILURE
```

**Causas:**
1. Container PostgreSQL não está rodando
2. Conexões antigas travadas no pool
3. Múltiplas instâncias tentando conectar ao mesmo banco

**Soluções:**

#### Solução 1: Verificar se o PostgreSQL está rodando
```bash
# Verificar containers
docker ps

# Se não estiver rodando, subir o docker-compose
docker compose up -d quarkus_postgres

# Verificar logs do postgres
docker compose logs -f quarkus_postgres
```

#### Solução 2: Reiniciar completamente o PostgreSQL
```bash
# Parar containers
docker compose down

# Subir novamente
docker compose up -d

# Aguardar 5 segundos para o postgres inicializar
sleep 5

# Testar conexão
docker compose exec quarkus_postgres pg_isready -U quarkus
```

#### Solução 3: Limpar conexões travadas
```bash
# Conectar ao postgres
docker compose exec quarkus_postgres psql -U quarkus -d quarkus_db

# Listar conexões ativas
SELECT pid, usename, application_name, state
FROM pg_stat_activity
WHERE datname = 'quarkus_db';

# Matar conexões específicas (se necessário)
SELECT pg_terminate_backend(pid)
FROM pg_stat_activity
WHERE datname = 'quarkus_db' AND pid <> pg_backend_pid();

# Sair do psql
\q
```

---

### 🧹 Limpeza Completa do Ambiente (Problema Persistente)

Se os problemas persistirem após as soluções acima, faça uma limpeza completa:

```bash
# 1. Parar TUDO que está rodando
docker compose down
pkill -f quarkus  # Matar processos Java/Quarkus

# 2. Limpar build do Maven
./mvnw clean

# 3. Remover diretório target (cache de compilação)
rm -rf target/

# 4. Limpar cache do Quarkus
rm -rf ~/.m2/repository/.cache/quarkus/

# 5. Reiniciar PostgreSQL
docker compose up -d quarkus_postgres
sleep 5

# 6. Verificar saúde do banco
docker compose exec quarkus_postgres pg_isready -U quarkus

# 7. Rodar novamente
./mvnw quarkus:dev
```

---

### 🔍 Diagnóstico Avançado

#### Verificar múltiplas instâncias Quarkus rodando
```bash
# Listar processos Java
ps aux | grep quarkus

# Matar todos os processos Quarkus
pkill -f quarkus
# OU
pkill -f "mvnw quarkus:dev"
```

#### Verificar portas em uso
```bash
# Listar todas as portas em uso pelo projeto
lsof -i :8080  # Porta HTTP padrão
lsof -i :8443  # Porta HTTPS (dev)
lsof -i :8083  # Porta HTTP (prod)
lsof -i :5432  # PostgreSQL
lsof -i :5005  # Debug port

# Ver TODOS os processos Java
jps -l
```

#### Verificar conexões ao PostgreSQL
```bash
# Ver quantas conexões estão ativas
docker compose exec quarkus_postgres psql -U quarkus -d quarkus_db -c \
  "SELECT count(*) FROM pg_stat_activity WHERE datname = 'quarkus_db';"

# Ver detalhes das conexões
docker compose exec quarkus_postgres psql -U quarkus -d quarkus_db -c \
  "SELECT pid, usename, application_name, client_addr, state, state_change
   FROM pg_stat_activity
   WHERE datname = 'quarkus_db';"
```

---

### ⚙️ Configuração de Connection Pool (Prevenir Timeouts)

Se o problema de timeout for recorrente, ajuste o pool de conexões em `application-dev.properties`:

```properties
# Aumentar timeout de aquisição de conexão (padrão: 5 segundos)
quarkus.datasource.jdbc.acquisition-timeout=10

# Reduzir tamanho máximo do pool (evita esgotar conexões do postgres)
quarkus.datasource.jdbc.max-size=10

# Tempo mínimo de conexão no pool
quarkus.datasource.jdbc.min-size=2

# Timeout de conexão inicial
quarkus.datasource.jdbc.initial-size=2
```

---

### 📋 Checklist Antes de Rodar `quarkus:dev`

- [ ] Nenhum processo Quarkus rodando: `pkill -f quarkus`
- [ ] Porta 8080 livre: `lsof -i :8080` (deve retornar vazio)
- [ ] PostgreSQL rodando: `docker ps | grep postgres`
- [ ] PostgreSQL saudável: `docker compose exec quarkus_postgres pg_isready`
- [ ] Diretório `target/` limpo (opcional): `./mvnw clean`

---

### 🚨 Solução de Último Recurso

Se NADA funcionar:

```bash
# 1. Parar e remover TUDO do Docker
docker compose down -v  # ⚠️ Remove volumes (perde dados!)
docker system prune -af

# 2. Limpar completamente o projeto
./mvnw clean
rm -rf target/
rm -rf ~/.m2/repository/.cache/

# 3. Recriar banco do zero
docker compose up -d
sleep 10

# 4. Rodar
./mvnw quarkus:dev
```

⚠️ **ATENÇÃO**: `docker compose down -v` **APAGA TODOS OS DADOS** do banco! Use apenas em ambiente de desenvolvimento.

---

## 🛠️ Comandos Úteis para Desenvolvimento Local

### 🧹 Limpeza de Cache e Build

#### Limpar build do Maven
```bash
# Limpar apenas target/
./mvnw clean

# Limpar e compilar novamente
./mvnw clean compile

# Limpar e empacotar
./mvnw clean package
```

#### Limpar cache do Quarkus
```bash
# Cache de aplicação
rm -rf ~/.m2/repository/.cache/quarkus/

# Cache de extensões
rm -rf ~/.m2/repository/.quarkus/

# Ou limpar tudo do .m2 cache (mais agressivo)
rm -rf ~/.m2/repository/.cache/
```

#### Limpar Dev Services (containers temporários)
```bash
# Quarkus cria containers temporários para testes
# Listar containers criados pelo Quarkus
docker ps -a --filter "label=quarkus-dev-service"

# Remover todos os dev services
docker rm -f $(docker ps -aq --filter "label=quarkus-dev-service")

# Remover volumes de dev services
docker volume prune -f
```

#### Forçar rebuild completo
```bash
# Limpar tudo e recompilar sem cache
./mvnw clean install -U

# -U: Força atualização de snapshots e releases
```

---

### 🚀 Comandos de Desenvolvimento

#### Rodar em modo dev com opções úteis
```bash
# Dev mode padrão
./mvnw quarkus:dev

# Dev mode sem testes contínuos
./mvnw quarkus:dev -Dquarkus.test.continuous-testing=disabled

# Dev mode com debug remoto desabilitado
./mvnw quarkus:dev -Ddebug=false

# Dev mode em outra porta
./mvnw quarkus:dev -Dquarkus.http.port=8081

# Dev mode com profile específico
./mvnw quarkus:dev -Dquarkus.profile=dev

# Dev mode sem live reload
./mvnw quarkus:dev -Dquarkus.live-reload.instrumentation=false
```

#### Rodar testes
```bash
# Rodar todos os testes
./mvnw test

# Rodar testes de uma classe específica
./mvnw test -Dtest=UserResourceTest

# Rodar testes com coverage (Jacoco)
./mvnw verify

# Rodar testes pulando integração
./mvnw test -DskipITs

# Testes contínuos (modo dev)
./mvnw quarkus:test
```

#### Build otimizado
```bash
# Build rápido (skip testes)
./mvnw clean package -DskipTests

# Build com testes
./mvnw clean package

# Build com análise de código
./mvnw clean verify

# Atualizar dependências
./mvnw clean install -U
```

---

### 🔍 Diagnóstico e Informações

#### Ver informações do projeto
```bash
# Listar extensões instaladas
./mvnw quarkus:list-extensions

# Ver árvore de dependências
./mvnw dependency:tree

# Ver dependências desatualizadas
./mvnw versions:display-dependency-updates

# Ver plugins desatualizados
./mvnw versions:display-plugin-updates

# Analisar dependências (encontrar conflitos)
./mvnw dependency:analyze
```

#### Verificar configuração
```bash
# Listar todas as propriedades de configuração
./mvnw quarkus:info

# Ver configuração efetiva
./mvnw quarkus:config

# Validar application.properties
./mvnw validate
```

---

### 📦 Gerenciamento de Dependências

#### Adicionar extensões
```bash
# Adicionar extensão do Quarkus
./mvnw quarkus:add-extension -Dextensions="hibernate-validator"

# Adicionar múltiplas extensões
./mvnw quarkus:add-extension -Dextensions="rest-client,jsonb"

# Listar extensões disponíveis
./mvnw quarkus:list-extensions --installable
```

#### Remover extensões
```bash
# Remover extensão
./mvnw quarkus:remove-extension -Dextensions="hibernate-validator"
```

#### Atualizar Quarkus
```bash
# Atualizar para última versão do Quarkus
./mvnw quarkus:update

# Atualizar para versão específica
./mvnw io.quarkus:quarkus-maven-plugin:3.23.3:update
```

---

### 🐳 Docker e Containers

#### Build de imagens
```bash
# Build JVM Docker image
./mvnw clean package
docker build -f src/main/docker/Dockerfile.jvm -t mobile-rest-api:jvm .

# Build Native Docker image
./mvnw package -Dnative -Dquarkus.native.container-build=true
docker build -f src/main/docker/Dockerfile.native -t mobile-rest-api:native .

# Build com multi-stage (mais eficiente)
docker build -f src/main/docker/Dockerfile.jvm -t mobile-rest-api:latest .
```

#### Gerenciar containers do projeto
```bash
# Subir serviços (postgres)
docker compose up -d

# Ver logs em tempo real
docker compose logs -f

# Logs de serviço específico
docker compose logs -f quarkus_postgres

# Reiniciar serviço específico
docker compose restart quarkus_postgres

# Parar sem remover volumes
docker compose stop

# Parar e remover containers (mantém volumes)
docker compose down

# Parar e remover TUDO (⚠️ perde dados!)
docker compose down -v

# Ver status dos serviços
docker compose ps
```

---

### 💾 Backup e Restore do Banco

#### Backup
```bash
# Backup completo do banco
docker compose exec -T quarkus_postgres pg_dump -U quarkus quarkus_db > backup_$(date +%Y%m%d_%H%M%S).sql

# Backup apenas schema (sem dados)
docker compose exec -T quarkus_postgres pg_dump -U quarkus --schema-only quarkus_db > schema_backup.sql

# Backup apenas dados
docker compose exec -T quarkus_postgres pg_dump -U quarkus --data-only quarkus_db > data_backup.sql

# Backup compactado
docker compose exec -T quarkus_postgres pg_dump -U quarkus quarkus_db | gzip > backup_$(date +%Y%m%d).sql.gz
```

#### Restore
```bash
# Restore de backup
cat backup_20260202_120000.sql | docker compose exec -T quarkus_postgres psql -U quarkus quarkus_db

# Restore de backup compactado
gunzip -c backup_20260202.sql.gz | docker compose exec -T quarkus_postgres psql -U quarkus quarkus_db

# Recriar banco do zero com Flyway
docker compose down
docker compose up -d quarkus_postgres
sleep 5
./mvnw quarkus:dev  # Flyway cria tudo automaticamente
```

---

### 🔧 Ferramentas de Desenvolvimento

#### Dev UI (Interface Web)
```bash
# Acessar Dev UI quando quarkus:dev estiver rodando
open http://localhost:8080/q/dev/

# Funcionalidades disponíveis:
# - Ver configurações
# - Executar migrations Flyway
# - Inspecionar banco de dados
# - Ver métricas de build
# - Executar testes
# - Inspecionar beans CDI
```

#### Hot Reload
```bash
# Hot reload está ativo por padrão em dev mode
# Basta salvar arquivos .java e o Quarkus recompila automaticamente

# Desabilitar hot reload (caso necessário)
./mvnw quarkus:dev -Dquarkus.live-reload.instrumentation=false

# Forçar reload manual
# Pressione 's' no terminal do quarkus:dev
```

#### Continuous Testing
```bash
# Habilitar testes contínuos (rodam ao salvar)
./mvnw quarkus:dev  # Pressione 'r' para habilitar

# Rodar todos os testes
# Pressione 'r' no terminal

# Rodar testes falhados
# Pressione 'f' no terminal

# Ver comandos disponíveis
# Pressione 'h' no terminal
```

---

### 🎯 Scripts de Automação Úteis

#### Script de reset completo (salvar como `reset-dev.sh`)
```bash
#!/bin/bash
echo "🧹 Limpando ambiente de desenvolvimento..."

# Parar processos
pkill -f quarkus
docker compose down

# Limpar build
./mvnw clean
rm -rf target/

# Limpar cache
rm -rf ~/.m2/repository/.cache/quarkus/

# Reiniciar banco
docker compose up -d quarkus_postgres
sleep 5

# Verificar saúde
docker compose exec quarkus_postgres pg_isready -U quarkus

echo "✅ Ambiente limpo! Executar: ./mvnw quarkus:dev"
```

#### Script de backup rápido (salvar como `backup-db.sh`)
```bash
#!/bin/bash
BACKUP_DIR="backups"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
BACKUP_FILE="${BACKUP_DIR}/backup_${TIMESTAMP}.sql"

mkdir -p ${BACKUP_DIR}

echo "💾 Criando backup: ${BACKUP_FILE}"
docker compose exec -T quarkus_postgres pg_dump -U quarkus quarkus_db > ${BACKUP_FILE}

echo "✅ Backup criado: ${BACKUP_FILE}"
echo "📊 Tamanho: $(du -h ${BACKUP_FILE} | cut -f1)"
```

#### Dar permissão de execução
```bash
chmod +x reset-dev.sh backup-db.sh
```

---

### 📋 Atalhos do Terminal (durante quarkus:dev)

Quando `./mvnw quarkus:dev` estiver rodando, você pode usar:

| Tecla | Ação |
|-------|------|
| `r` | Rodar todos os testes |
| `f` | Rodar apenas testes falhados |
| `v` | Abrir Dev UI no navegador |
| `s` | Forçar reload da aplicação |
| `i` | Toggle de instrumentação |
| `l` | Toggle de live reload |
| `h` | Mostrar ajuda |
| `q` | Sair do dev mode |

---

### 📚 Referências 1
- Adoção parcial do GitFlow para organização de branches em 09/02/2026
- Branches com padrão: feature/, bugfix/, docs/ para organizar o desenvolvimento
- Rebase ao invés de merge (opcional): git rebase main antes de mergear mantém histórico limpo
- Tags para releases: git tag -a v1.0.0 -m "Release 1.0.0" → rastreia versões
- Commits descritivos: git commit -m "feat: adiciona autenticação" (use Conventional Commits)

### Deletar branch local e remota de develop (fora dela):
- Antes de deletar, certifique-se de estar em outra branch (ex: main)
git checkout main
- Local
git branch -D develop
- Remoata
git push origin --delete develop

### Workflow Completo Recomendado

# 1. Desenvolvimento (testar manualmente)
source .env
./mvnw quarkus:dev
# Testar endpoints no Swagger: https://localhost:8443/q/swagger-ui

# 2. Validar testes (antes de commit)
./mvnw test
./mvnw clean
./mvnw clean verify
./mvnw clean package
./test.sh

# 3. Build completo (antes de PR)
./mvnw clean verify

### 📚 Referências 2

- [Quarkus Dev Mode](https://quarkus.io/guides/maven-tooling#dev-mode)
- [Quarkus Datasource Configuration](https://quarkus.io/guides/datasource#configuration-reference)
- [Flyway Database Migrations](https://quarkus.io/guides/flyway)
- [PostgreSQL Connection Pooling](https://www.postgresql.org/docs/current/runtime-config-connection.html)

