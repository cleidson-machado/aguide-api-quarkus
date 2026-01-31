# 🎯 Saga Jenkins + SonarQube - Jornada Completa

**Data:** 22 de Janeiro de 2026  
**Objetivo:** Integrar análise do SonarQube no pipeline Jenkins para projeto Quarkus  
**Resultado:** ✅ **SUCESSO TOTAL - Build #19**

---

## 📊 Resumo Executivo

### Antes (Branch main)
- ✅ Jenkins rodando builds básicos
- ✅ Deploy automático via Docker
- ❌ **SEM** análise de qualidade de código
- ❌ **SEM** execução de testes automatizados

### Depois (Branch develop-data-objects → main)
- ✅ Jenkins com pipeline completo
- ✅ **Testes automatizados** (3 testes de integração)
- ✅ **SonarQube integrado** com análise de código
- ✅ Deploy automático mantido
- ✅ Cobertura de código rastreada

---

## 🗺️ Jornada Completa - Os 19 Builds

### 🔴 **Builds 1-10: Problemas de Permissão e Configuração**

#### Build #1-5: Erro de Permissão Git
**Problema:**
```
insufficient permission for adding an object to repository database .git/objects
```

**Solução:**
```bash
sudo chown -R ubuntu:ubuntu /opt/apps/aguide-api-quarkus
sudo chmod -R 755 /opt/apps/aguide-api-quarkus
```

**Lição:** Jenkins rodando como usuário `ubuntu` precisa ter ownership correto no diretório de deploy.

---

#### Build #6-8: Erro de Conexão com Banco de Dados
**Problema:**
```
Connection to localhost:5432 refused
```

**Causa:** Testes tentando conectar em `localhost:5432` mas PostgreSQL está em container Docker chamado `quarkus_postgres`.

**Solução:** Adicionar no Jenkinsfile:
```groovy
export QUARKUS_DATASOURCE_JDBC_URL=jdbc:postgresql://quarkus_postgres:5432/quarkus_db
```

**Lição:** Containers Docker usam nomes de serviço para comunicação, não localhost.

---

#### Build #9-10: Testes Template Falhando
**Problema:**
```
Error: GET /hello endpoint does not exist
Tests: GreetingResourceTest e GreetingResourceIT falhando
```

**Solução:** Remover testes template do Quarkus que testavam endpoint inexistente:
```bash
rm src/test/java/br/com/aguideptbr/GreetingResourceIT.java
rm src/test/java/br/com/aguideptbr/GreetingResourceTest.java
```

**Lição:** Remover código template antes de configurar CI/CD.

---

### 🟡 **Builds 11-13: Problemas de Autenticação nos Testes**

#### Build #11: Erro 401 Unauthorized
**Problema:**
```
Expected status code <200> but was <401>
```

**Causa:** API usa sistema de autenticação Bearer Token, mas testes não incluíam o header.

**Solução:** Adicionar token nos testes:
```java
private static final String AUTH_TOKEN = "Bearer my-token-super-recur-12345";

given()
    .header("Authorization", AUTH_TOKEN)
    .when().get("/users")
```

**Lição:** Testes de integração devem replicar autenticação de produção.

---

#### Build #12: Erro 405 Method Not Allowed
**Problema:**
```
Expected status code <200> but was <405>
testGetUsersPaginatedEndpoint - 405
testGetUserByIdNotFound - 405
```

**Causa:** Endpoints `/users/paginated` e `/users/{id}` **não existiam** no UserResource.

**Solução:** Implementar endpoints faltantes:
```java
@GET
@Path("/paginated")
public PaginatedResponse<UserModel> listPaginated(
    @QueryParam("page") @DefaultValue("0") int page,
    @QueryParam("size") @DefaultValue("10") int size) {
    // implementação...
}

@GET
@Path("/{id}")
public Response getUserById(@PathParam("id") UUID id) {
    // implementação...
}
```

**Lição:** Testes só podem validar funcionalidades que realmente existem! 😅

---

#### Build #13: Campos JSON Incorretos
**Problema:**
```
JSON path totalElements doesn't match
Expected: not null, Actual: null
```

**Causa:** Teste esperava `totalElements` mas `PaginatedResponse` usa `totalItems`.

**Solução:** Corrigir nomes dos campos no teste:
```java
.body("totalItems", notNullValue())  // era totalElements
.body("totalPages", notNullValue())
.body("currentPage", notNullValue()) // adicionado
```

**Lição:** Manter consistência entre contratos de API e testes.

---

### 🟢 **Build #14: TODOS OS TESTES PASSARAM! Mas...**

**Conquista:** 
```
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0
```

**Novo Problema:** SonarQube sem autenticação
```
Not authorized. Please check the user token in the property 'sonar.token'
```

**Solução:**
1. Gerar token no SonarQube: My Account → Security → Generate Token
2. Configurar no Jenkins: Manage Jenkins → Configure System → SonarQube servers
3. Adicionar credencial tipo "Secret text" com o token

**Lição:** SonarQube precisa de autenticação mesmo para análises locais.

---

### 🔵 **Builds 15-18: Problemas de Deploy Docker**

#### Build #15: Container em Uso
**Problema:**
```
Error: Container name "/aguide-api" is already in use
```

**Causa:** Container anterior não sendo removido antes de subir novo.

**Solução:** Adicionar remoção forçada:
```groovy
sh '''
    docker rm -f aguide-api || true
    docker compose -f docker-compose.yml down --remove-orphans
    docker compose -f docker-compose.yml up -d
'''
```

**Lição:** Sempre limpar recursos antes de recriar.

---

#### Builds #16-18: Jenkinsfile Unificado
**Desafio:** Mesclar melhor dos 2 Jenkinsfiles:
- **Original (main):** Deploy em `/opt/apps/aguide-api-quarkus` funcionando
- **Novo (develop):** SonarQube funcionando mas no workspace do Jenkins

**Problema #1 - Build #16:** Sintaxe Groovy corrompida
```
expecting anything but '\n'; got it anyway @ line 174
```

**Problema #2 - Build #17:** Ainda com sintaxe quebrada (código duplicado)

**Problema #3 - Build #18:** Git checkout falhando
```
error: Your local changes to the following files would be overwritten by checkout
```

**Solução Final:** 
1. Corrigir seção `post` do Jenkinsfile
2. Mudar checkout para `git reset --hard origin/${GIT_BRANCH}`

---

### 🎉 **Build #19: SUCESSO TOTAL!**

```
✅ Pipeline executado com sucesso
✅ Tests run: 3, Failures: 0, Errors: 0, Skipped: 0
✅ SonarQube Analysis: 563 linhas de código analisadas
✅ Docker Image: Construída com sucesso
✅ Deploy: Container aguide-api rodando
✅ Duração: 1 min 7 sec
```

**Análise SonarQube:**
- 🟢 0 Security issues
- 🟡 1 Reliability issue (medium)
- 🟡 27 Maintainability issues
- ⚠️ 0.0% Coverage (normal, melhorar depois)

---

## 📁 Arquivos Criados/Modificados

### Novos Arquivos
```
src/test/java/br/com/aguideptbr/features/user/UserResourceTest.java
```

### Arquivos Modificados
```
Jenkinsfile                                                   (melhorado)
src/main/java/br/com/aguideptbr/features/user/UserResource.java (+ 2 endpoints)
```

### Arquivos Removidos
```
src/test/java/br/com/aguideptbr/GreetingResourceIT.java      (template)
src/test/java/br/com/aguideptbr/GreetingResourceTest.java    (template)
```

---

## 🎓 Lições Aprendidas

### 1. **Permissões no Jenkins**
- Jenkins container precisa ownership correto (`ubuntu:ubuntu`)
- Permissões 755 no diretório de deploy

### 2. **Docker Networking**
- Usar nomes de serviço Docker, não `localhost`
- Todos os serviços devem estar na mesma network (`proxy-network`)

### 3. **Testes de Integração**
- Implementar endpoints antes de criar testes
- Incluir autenticação nos testes
- Validar contratos JSON corretamente

### 4. **SonarQube**
- Requer token de autenticação
- Pode rodar testes durante análise (não usar `-DskipTests`)
- Fornece métricas valiosas de qualidade

### 5. **Jenkinsfile**
- `git reset --hard` > `git checkout` para automação
- Sempre limpar containers antes de recriar
- Capturar informações do commit para rastreabilidade

---

## 🏗️ Arquitetura Final

### Ambiente VPS
```
┌─────────────────────────────────────────────────┐
│                  proxy-network                   │
│                                                  │
│  ┌──────────┐  ┌──────────┐  ┌──────────────┐  │
│  │ Jenkins  │  │ SonarQube│  │   Postgres   │  │
│  │ :8080    │  │ :9000    │  │ :5432        │  │
│  │          │  │          │  │ quarkus_db   │  │
│  └────┬─────┘  └──────────┘  └──────────────┘  │
│       │                                          │
│       │ deploy                                   │
│       ▼                                          │
│  ┌──────────────────────────────────────────┐   │
│  │  /opt/apps/aguide-api-quarkus           │   │
│  │  ┌────────────────────────────────┐     │   │
│  │  │  Container: aguide-api         │     │   │
│  │  │  Port: 127.0.0.1:8083->8080    │     │   │
│  │  │  Quarkus 3.23.3 + Java 17      │     │   │
│  │  └────────────────────────────────┘     │   │
│  └──────────────────────────────────────────┘   │
└─────────────────────────────────────────────────┘
```

### Pipeline Jenkins
```
┌─────────────────────────────────────────────────┐
│ 1. Pipeline Info     - Mostra detalhes do build │
│ 2. Checkout          - Atualiza código do Git   │
│ 3. Build Maven       - Compila (sem testes)     │
│ 4. SonarQube         - Testes + análise código  │
│ 5. Verificar Artefatos                          │
│ 6. Build Docker Image                           │
│ 7. Deploy Container  - Remove antigo + sobe novo│
│ 8. Cleanup Docker    - Limpa recursos não usados│
│ 9. Verificar Status  - Confirma container UP    │
└─────────────────────────────────────────────────┘
```

---

## 🔧 Configurações Importantes

### Jenkins - SonarQube Integration
- **Nome:** SonarQube
- **URL:** `http://sonarqube:9000`
- **Token:** Secret text credential
- **Project Key:** `aguide-api-quarkus`

### Quarkus Test Configuration
- **Database:** PostgreSQL (não H2 in-memory)
- **JDBC URL:** `jdbc:postgresql://quarkus_postgres:5432/quarkus_db`
- **Profile:** test
- **Flyway:** Migrations automáticas

### Docker Compose
- **Network:** proxy-network
- **Container:** aguide-api
- **Port:** 127.0.0.1:8083:8080
- **Restart:** always

---

## ⏭️ Próximos Passos

### Imediato
1. ✅ **Build #19 passou** - Pipeline funcionando!
2. ⚠️ **Webhook GitHub não disparando builds automáticos** (investigar amanhã)
3. 📝 Fazer **Pull Request** de `develop-data-objects` para `main`

### Melhorias Futuras
1. **Aumentar cobertura de testes** (atualmente 0.0%)
   - Adicionar testes para ContentResource
   - Adicionar testes para ContentService
   - Testar cenários de erro

2. **Corrigir issues do SonarQube**
   - 1 Reliability issue (medium)
   - 27 Maintainability issues

3. **Configurar Quality Gate**
   - Definir threshold mínimo de cobertura
   - Bloquear merge com issues críticos

4. **Adicionar testes de carga**
   - JMeter ou Gatling
   - Testar performance da API

---

## 🚨 PENDENTE: Jenkins Webhook Automático

### Problema Atual
Jenkins **NÃO** está disparando builds automaticamente quando há `git push` no GitHub.

### Configuração Atual no Jenkins Job
```groovy
// No Jenkinsfile, falta trigger de webhook
// Provavelmente precisa adicionar:
triggers {
    githubPush()
}
```

### Checklist para Investigar Amanhã

#### 1️⃣ **Verificar Webhook no GitHub**
- [ ] Ir em: `https://github.com/cleidson-machado/aguide-api-quarkus/settings/hooks`
- [ ] Verificar se existe webhook apontando para Jenkins
- [ ] URL esperada: `https://nauto.aguide-ptbr.com.br/github-webhook/`
- [ ] Events: `Just the push event` ou `Let me select: Pull requests, Pushes`
- [ ] Status: ✅ Verde (último delivery com sucesso)

#### 2️⃣ **Configurar Webhook no GitHub (se não existir)**
```
Payload URL: https://nauto.aguide-ptbr.com.br/github-webhook/
Content type: application/json
Secret: (deixar vazio ou configurar)
SSL: Enable SSL verification
Events: Just the push event
Active: ✅
```

#### 3️⃣ **Verificar Configuração do Jenkins Job**
- [ ] Dashboard → Job → Configure
- [ ] Seção **Build Triggers**
- [ ] ✅ Marcar: `GitHub hook trigger for GITScm polling`
- [ ] Salvar

#### 4️⃣ **Adicionar Trigger no Jenkinsfile**
Adicionar após `environment` no Jenkinsfile:
```groovy
triggers {
    githubPush()
}
```

#### 5️⃣ **Verificar Plugin GitHub no Jenkins**
- [ ] Manage Jenkins → Manage Plugins
- [ ] Installed plugins
- [ ] Buscar: "GitHub Plugin"
- [ ] Verificar se está instalado e atualizado

#### 6️⃣ **Verificar Firewall/Proxy**
- [ ] GitHub consegue alcançar `https://nauto.aguide-ptbr.com.br/github-webhook/`?
- [ ] Nginx/Cloudflare bloqueando?
- [ ] Teste manual: `curl -X POST https://nauto.aguide-ptbr.com.br/github-webhook/`

#### 7️⃣ **Verificar Logs do Jenkins**
```bash
# No VPS, entrar no container do Jenkins
docker logs jenkins -f

# Procurar por:
# - "GitHub hook trigger"
# - "Received POST"
# - Erros de webhook
```

#### 8️⃣ **Teste Manual do Webhook**
No GitHub webhook settings, clicar em **"Recent Deliveries"** e:
- Ver status code (deve ser 200)
- Ver response do Jenkins
- Redelivery para testar novamente

### Documentação Oficial
- Jenkins GitHub Plugin: https://plugins.jenkins.io/github/
- GitHub Webhooks: https://docs.github.com/en/webhooks

---

## 📊 Estatísticas da Saga

- **Total de Builds:** 19
- **Builds com Erro:** 18
- **Build de Sucesso:** 1 (Build #19) 🎉
- **Tempo Total:** ~3 horas
- **Commits no GitHub:** 15+
- **Arquivos Criados:** 1 (UserResourceTest.java)
- **Arquivos Modificados:** 2 (Jenkinsfile, UserResource.java)
- **Arquivos Removidos:** 2 (GreetingResource*.java)
- **Linhas de Código Analisadas:** 563
- **Cobertura de Testes:** 0.0% → melhorar!

---

## 🏆 Resultado Final

### ✅ Objetivos Alcançados
1. ✅ Jenkins executando testes automaticamente
2. ✅ SonarQube integrado e analisando código
3. ✅ Deploy automático mantido
4. ✅ Pipeline robusto com tratamento de erros
5. ✅ Rastreabilidade de commits

### 🎯 ROI (Return on Investment)
- **Antes:** Apenas build e deploy (sem garantia de qualidade)
- **Depois:** Build + Testes + Análise de Qualidade + Deploy
- **Valor:** Detecção precoce de bugs, código mais limpo, confiança em releases

---

## 💬 Citações Memoráveis da Saga

> "essa novela toda é para usar o sonar" - Cleidson, definindo o objetivo principal

> "Minhas consultas / testes da API já hospedada no VPS é feita com esse token" - Descobrindo o Bearer token

> "Ops! Esse seu ultimo jenkisnfile deu erro" - Build #16 😅

> "Em fim! O sucesso!?" - Build #19 🎉

---

## 🙏 Agradecimentos

- **PostgreSQL** por ser confiável no Docker
- **Quarkus** por startup rápido nos testes (10-12s)
- **SonarQube** por análise de código grátis
- **Jenkins** por... eventualmente funcionar 😅
- **Git** por manter histórico de todos os erros 😂

---

**Documentado com ❤️ por GitHub Copilot**  
**Data:** 22/01/2026 - 21:30 UTC  
**Build:** #19 - SUCCESS ✅  
**Status:** PRODUCTION READY 🚀
