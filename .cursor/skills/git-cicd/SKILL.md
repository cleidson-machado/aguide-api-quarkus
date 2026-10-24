---
name: git-cicd
description: Orienta fluxo Git (branches, commits, merge, rebase, conflitos) e CI/CD do mobile-rest-api (GitHub Actions, Jenkins, deploy Docker no VPS). Usar quando o usuário pedir ajuda com git, PR, pipeline, deploy, validação pré-produção ou conflitos de merge.
---

# Git e CI/CD — mobile-rest-api

Perfil **Dev Sênior**: priorizar segurança de dados em produção, mudanças pequenas e reversíveis, e alinhamento com o que o repositório já executa.

## Quando acionar

- Branch strategy, commits, rebase/merge, resolução de conflitos, revisão de histórico
- Alteração ou diagnóstico de `.github/workflows/`, `Jenkinsfile*`, deploy VPS
- Checklist antes de merge em `main`/`master`
- Entender por que CI passou/falhou ou como espelhar o pipeline localmente

## Mapa de automação neste repo

| Artefato | Papel |
|----------|--------|
| `.github/workflows/deploy.yml` | CI em push/PR para `main`/`master`: build Maven, validação prod, deploy SSH no VPS |
| `Jenkinsfile` | Pipeline Jenkins (branch configurável; build + Sonar + Docker) |
| `Jenkinsfile.test` | Branches de teste/desenvolvimento |
| `Jenkinsfile.production` | Produção (`main`, webhook GitHub) |
| `validate-production-safety.sh` | Validação local antes de deploy/merge |
| `docker-compose.yml` | Runtime produção no VPS (`QUARKUS_PROFILE=prod`) |

**Diretório no VPS (deploy):** `/opt/apps/aguide-api-quarkus`  
**Serviço Docker da API:** `aguide-api`

---

## Git — fluxo diário

### Branches

- Não commitar direto em `main`/`master`.
- Padrão: `<tipo>/<slug>` — `feat/...`, `fix/...`, `chore/...`.
- Sincronizar com a base antes do PR (`git fetch` + rebase ou merge conforme política do time).
- **Perguntar** ao usuário antes de criar/trocar branch ou executar comandos que alteram histórico remoto.

### Commits

- **Conventional Commits:** `tipo(escopo): descrição no imperativo`  
  Tipos: `feat`, `fix`, `refactor`, `perf`, `test`, `docs`, `chore`.
- Um commit = uma intenção.
- **Nunca** `git add .`, `git add -A` — stage arquivo a arquivo após listar o diff.
- Commits **somente** quando o usuário pedir explicitamente.

### Comandos que exigem confirmação

`commit`, `push`, `pull --rebase`, `reset`, `rebase`, `push --force`, `merge` que reescreve histórico.

Livres para diagnóstico: `status`, `diff`, `log`, `branch -vv`, `fetch`.

### Conflitos de merge/rebase

1. `git status` — listar arquivos em conflito.
2. Abrir cada arquivo; resolver marcadores `<<<<<<<` mantendo intenção de **ambos** lados quando possível.
3. Preferir resolver na camada correta (ex.: migration vs Java — não duplicar versão Flyway).
4. `./mvnw compile` e testes relacionados após resolver.
5. Continuar merge/rebase; **não** force-push em branch compartilhada sem acordo.

### PR (resumo)

- Escopo único; descrição: **o quê**, **por quê**, **como testar**.
- Mencionar migration, mudança de contrato API ou config de segurança se houver.
- Rodar localmente: `./validate-production-safety.sh` antes de merge que vá para produção.

---

## GitHub Actions (`deploy.yml`)

### Job `build`

- Java 17 (Temurin), cache Maven.
- `./mvnw clean package -DskipTests` — **CI de build não roda testes** neste workflow.

### Job `validate-config` (só `main`/`master`)

Bloqueia deploy se:

- `application-prod.properties` não tiver `quarkus.flyway.clean-at-start=false`
- não tiver `quarkus.hibernate-orm.database.generation=none`
- migrations contiverem `DROP TABLE`, `TRUNCATE`, `DROP SCHEMA`, `DROP DATABASE`

### Job `deploy`

Via SSH no VPS:

1. Confirmar `QUARKUS_PROFILE=prod` no `docker-compose.yml`
2. `git pull origin main`
3. **Deploy seguro:** `docker compose up -d --no-deps --build aguide-api` (não derrubar Postgres)
4. Logs + `docker system prune -f`

**Referência de boas práticas:** evitar `docker compose down` em produção sem revisar volumes; não rebuild agressivo sem validar profile.

---

## Jenkins

- **Build rápido:** `./mvnw clean package -DskipTests` (vários stages).
- **Qualidade:** stage Sonar executa `mvn verify sonar:sonar` **com testes** — barreira de qualidade diferente do GitHub Actions build.
- **Produção:** `Jenkinsfile.production` restrito à branch `main`.
- Alguns Jenkinsfiles antigos ainda usam `docker compose down` / rebuild completo — ao propor mudanças de deploy, **alinhar ao padrão seguro** do GitHub Actions (`--no-deps --build aguide-api`).

Antes de editar Jenkinsfile: confirmar com o usuário (guardrail do projeto).

---

## Validação local obrigatória (produção)

```bash
./validate-production-safety.sh
```

Cobre, entre outros: Flyway/Hibernate em prod, compose, migrations destrutivas.

Complementar manualmente:

```bash
grep -E "clean-at-start|database.generation" src/main/resources/application-prod.properties
grep QUARKUS_PROFILE docker-compose.yml
```

---

## Checklist rápido pré-merge → produção

- [ ] Migrations apenas incrementais e idempotentes onde aplicável
- [ ] `application-prod.properties` intacto (`clean-at-start=false`, `generation=none`)
- [ ] Nenhum segredo (`.env`, `security/`) no commit
- [ ] `./mvnw test` ou `./mvnw verify` executado localmente quando a mudança afeta lógica
- [ ] `./validate-production-safety.sh` OK

---

## Diagnóstico CI/CD (senior)

| Sintoma | Verificar |
|---------|-----------|
| Deploy OK mas API 502 | Logs `aguide-api`, health `http://localhost:8083/api/v1/auth/health`, SSL no NGINX vs HTTP interno |
| Flyway falhou no start | Ordem de versões `V1.0.*`, checksum, migration editada após apply |
| Build GH passa, qualidade ruim | Jenkins/Sonar roda testes; GH build usa `-DskipTests` |
| Testes passam local, falham CI | `quarkus_test`, Postgres disponível, `src/test/resources/application.properties` |

---

## Referências do projeto

- `.github/composer-instructions.md` — guardrails gerais
- `AGENTS.md` — bancos e ambiente
