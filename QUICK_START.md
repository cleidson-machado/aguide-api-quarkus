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

## 🧪 Comandos de Testes Avançados

### Executar Todos os Testes

```bash
# Todos os testes do projeto
./mvnw test

# Todos os testes com relatório de cobertura (JaCoCo)
./mvnw verify

# Ver relatório de cobertura após executar:
open target/site/jacoco/index.html
```

### Executar Testes de uma Classe Específica

```bash
# Sintaxe: ./mvnw test -Dtest=NomeDaClasse
./mvnw test -Dtest=PhoneNumberServiceIntegrationTest
./mvnw test -Dtest=UserServiceTest
./mvnw test -Dtest=ContentRecordServiceTest
```

### Executar um Teste Específico (Método)

```bash
# Sintaxe: ./mvnw test -Dtest=NomeDaClasse#nomeDoMetodo
./mvnw test -Dtest=PhoneNumberServiceIntegrationTest#testValidBrazilianMobile
./mvnw test -Dtest=UserServiceTest#testCreateUser
```

### Executar Testes de uma Feature Específica

```bash
# Todos os testes do pacote 'phone'
./mvnw test -Dtest=br.com.aguideptbr.features.phone.**.*Test

# Todos os testes do pacote 'user'
./mvnw test -Dtest=br.com.aguideptbr.features.user.**.*Test

# Todos os testes do pacote 'content'
./mvnw test -Dtest=br.com.aguideptbr.features.content.**.*Test
```

### Executar Múltiplos Testes

```bash
# Sintaxe: -Dtest=Classe1,Classe2,Classe3
./mvnw test -Dtest=UserServiceTest,PhoneNumberServiceIntegrationTest
```

### Executar Testes com Padrão de Nome

```bash
# Todos os testes que terminam com "IntegrationTest"
./mvnw test -Dtest=**/*IntegrationTest

# Todos os testes que começam com "User"
./mvnw test -Dtest=User*

# Todos os testes de "Service"
./mvnw test -Dtest=*Service*Test
```

### Pular Testes (com cuidado!)

```bash
# Compilar sem executar testes (use apenas se necessário)
./mvnw clean package -DskipTests

# ⚠️ ATENÇÃO: Não use isso antes de commits/PRs!
```

### Modo Debug de Testes

```bash
# Executar testes com saída detalhada
./mvnw test -X -Dtest=PhoneNumberServiceIntegrationTest

# Ver logs SQL durante testes
./mvnw test -Dquarkus.hibernate-orm.log.sql=true
```

### Testes com Limpeza de Cache

```bash
# Limpar tudo e recompilar antes de testar
./mvnw clean test

# Forçar recompilação e testes
./mvnw clean verify
```

### Exemplos Práticos

```bash
# Cenário 1: Refatorei PhoneNumberService, quero testar só ele
./mvnw test -Dtest=PhoneNumberServiceIntegrationTest

# Cenário 2: Alterei lógica de validação de telefone brasileiro
./mvnw test -Dtest=PhoneNumberServiceIntegrationTest#testValidBrazilianMobile

# Cenário 3: Trabalhei em toda a feature 'user'
./mvnw test -Dtest=br.com.aguideptbr.features.user.**.*Test

# Cenário 4: Quero todos os testes de integração
./mvnw test -Dtest=**/*IntegrationTest

# Cenário 5: PR pronto, verificar cobertura completa
./mvnw clean verify
```

### Estrutura de Testes no Projeto

```
src/test/java/br/com/aguideptbr/features/
├── auth/
│   └── JWTServiceTest.java
├── content/
│   ├── ContentRecordServiceTest.java
│   └── ContentEngagementServiceTest.java
├── phone/
│   └── PhoneNumberServiceIntegrationTest.java
├── user/
│   └── UserServiceTest.java
└── usermessage/
    ├── ConversationServiceTest.java
    └── MessageServiceTest.java
```

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

## � Dicas de Produtividade com Testes

### 1. Desenvolvimento Orientado a Testes (TDD)

```bash
# Ciclo Red-Green-Refactor:
# 1. Escrever teste que falha
# 2. Fazer teste passar
# 3. Refatorar código

# Execute apenas o teste que está trabalhando
./mvnw test -Dtest=MinhaNovaFeatureTest

# Quando passar, execute todos os testes da feature
./mvnw test -Dtest=br.com.aguideptbr.features.minhafuncionalidade.**.*Test
```

### 2. Atalhos Úteis

```bash
# Alias úteis para adicionar no ~/.zshrc ou ~/.bashrc
alias mvntest='./mvnw test'
alias mvntestclass='./mvnw test -Dtest='
alias mvnverify='./mvnw clean verify'

# Depois de adicionar, use:
# mvntest                    # Todos os testes
# mvntestclass UserServiceTest  # Teste específico
```

### 3. Feedback Rápido Durante Desenvolvimento

```bash
# Terminal 1: Aplicação rodando
source .env && ./mvnw quarkus:dev

# Terminal 2: Testes automatizados (watch mode simulado)
# Crie um script 'watch-tests.sh':
#!/bin/bash
while true; do
    clear
    ./mvnw test -Dtest=PhoneNumberServiceIntegrationTest
    sleep 5
done

# Execute: ./watch-tests.sh
```

### 4. Análise de Cobertura

```bash
# RECOMENDADO: Gerar relatório completo (testes + HTML)
./mvnw verify

# ALTERNATIVA: Se já executou './mvnw test', pode gerar apenas o relatório:
./mvnw jacoco:report

# Abrir relatório no navegador
open target/site/jacoco/index.html

# Ver cobertura de uma classe específica
open target/site/jacoco/br.com.aguideptbr.features.phone/PhoneNumberService.html
```

**⚠️ Importante:** O comando `./mvnw test` coleta dados de cobertura (`target/jacoco.exec`) mas **NÃO** gera o relatório HTML. Para visualizar, você precisa:
- Executar `./mvnw verify` (recomendado), ou
- Executar `./mvnw jacoco:report` após `./mvnw test`

### 5. Integração com VS Code

**Extensões recomendadas:**
- **Test Runner for Java** - Executa testes diretamente no editor
- **Coverage Gutters** - Mostra cobertura inline no código

**Comandos úteis no VS Code:**
- `Cmd + Shift + P` → "Java: Run Tests"
- `Cmd + Shift + P` → "Java: Debug Tests"
- Clique direito em método de teste → "Run Test" ou "Debug Test"

### 6. Debugging de Testes

```bash
# Executar teste em modo debug (aguarda conexão do debugger)
./mvnw test -Dtest=PhoneNumberServiceIntegrationTest -Dmaven.surefire.debug

# Conectar debugger na porta 5005
# No VS Code: Run > Start Debugging > Remote Java Application
```

### 7. Testes Paralelos (Avançado)

```bash
# Executar testes em paralelo (mais rápido)
./mvnw test -T 4  # 4 threads

# ⚠️ ATENÇÃO: Use apenas para testes independentes!
# Não recomendado se testes compartilham estado no banco
```

### 8. Filtrar Testes por Tag/Categoria

```bash
# Se você usar @Tag no JUnit 5:
# @Tag("integration")
# @Tag("unit")

# Executar apenas testes de integração
./mvnw test -Dgroups=integration

# Executar apenas testes unitários
./mvnw test -Dgroups=unit
```

### 9. Limpar Banco de Testes Manualmente

```bash
# Se testes estiverem falhando por dados inconsistentes
docker exec quarkus_postgres psql -U quarkus -d quarkus_test -c "
  DROP SCHEMA public CASCADE;
  CREATE SCHEMA public;
"

# Depois execute os testes novamente
./mvnw test
```

### 10. Pipeline de Verificação Completa

```bash
# Antes de fazer commit/PR, execute:
./mvnw clean verify

# Se quiser ser mais rigoroso:
./mvnw clean verify && \
  echo "✅ Build OK" || \
  echo "❌ Build FAILED - Verifique os erros!"
```

---

## �📋 Checklist Diário

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

### Problema: "No tests were found"

**Causa:** Nome da classe ou método incorreto no `-Dtest`

**Solução:**
```bash
# ❌ ERRADO:
./mvnw test -Dtest=PhoneNumberServiceIntegrationTest.java  # Não inclua .java

# ✅ CORRETO:
./mvnw test -Dtest=PhoneNumberServiceIntegrationTest

# Verificar se a classe existe:
find src/test/java -name "*PhoneNumber*Test.java"
```

### Problema: "Test failures: Connection timeout"

**Causa:** Banco de dados não está acessível ou demora na conexão

**Solução:**
```bash
# 1. Verificar se PostgreSQL está UP
docker ps | grep quarkus_postgres

# 2. Testar conexão manualmente
docker exec quarkus_postgres psql -U quarkus -d quarkus_test -c "SELECT 1;"

# 3. Aumentar timeout (se necessário)
# Edite src/test/resources/application.properties:
# quarkus.datasource.jdbc.acquisition-timeout=PT30S

# 4. Reiniciar container PostgreSQL se necessário
docker restart quarkus_postgres
```

### Problema: "ClassNotFoundException" ou "NoClassDefFoundError"

**Causa:** Compilação desatualizada ou cache corrompido

**Solução:**
```bash
# Limpar completamente e recompilar
./mvnw clean compile test-compile

# Se persistir, limpar cache Maven local
rm -rf target/
./mvnw clean install -DskipTests
./mvnw test
```

### Problema: "Flyway checksum mismatch"

**Causa:** Migration foi modificada após ser aplicada

**Solução:**
```bash
# Resetar banco de testes (seguro)
docker exec quarkus_postgres psql -U quarkus -d quarkus_test -c "
  DROP SCHEMA public CASCADE;
  CREATE SCHEMA public;
"

# Executar testes (Flyway recriará schema)
./mvnw test
```

### Problema: "Unsatisfied dependency" após refatoração

**Causa:** Constructor injection não está sendo detectado pelo CDI

**Exemplo do erro:**
```
jakarta.enterprise.inject.UnsatisfiedResolutionException:
Unsatisfied dependency for type br.com.aguideptbr.features.phone.PhoneNumberRepository
```

**Solução:**
```bash
# 1. Verificar se a classe tem @ApplicationScoped
# 2. Verificar se o construtor está correto (não precisa @Inject)
# 3. Limpar e recompilar
./mvnw clean compile test-compile

# 4. Se persistir, verificar imports
# Imports corretos para CDI do Quarkus:
# - jakarta.enterprise.context.ApplicationScoped
# - jakarta.inject.Inject (apenas para @Provider classes)
```

### Problema: Testes passam individualmente mas falham em lote

**Causa:** Testes compartilham estado ou dependem de ordem de execução

**Solução:**
```bash
# Executar testes em modo isolado
./mvnw test -Dtest=ProblematicTest -DforkCount=1 -DreuseForks=false

# Verificar se há estado compartilhado:
# - Campos estáticos mutáveis
# - Dados no banco não limpos no @BeforeEach
# - Dependência de ordem de execução

# Boa prática: Cada teste deve ser independente
# Use @BeforeEach para setup e @AfterEach para cleanup
```

### Problema: Teste de integração não encontra entidades

**Causa:** Transação não commitada ou contexto de persistência incorreto

**Solução:**
```java
// ✅ CORRETO: Marcar métodos de setup com @Transactional
@BeforeEach
@Transactional
void setUp() {
    // Limpar dados
    phoneRepository.deleteAll();
    UserModel.deleteAll();

    // Criar dados de teste
    testUser = new UserModel();
    testUser.email = "test@test.com";
    testUser.persist();  // Persiste no banco
}

// Testes também precisam de @Transactional se modificam dados
@Test
@Transactional
void testCreatePhone() {
    // ...
}
```

### Problema: "OutOfMemoryError" durante testes

**Causa:** Heap insuficiente para execução dos testes

**Solução:**
```bash
# Aumentar heap para testes
export MAVEN_OPTS="-Xmx1024m -XX:MaxPermSize=256m"
./mvnw test

# Ou editar pom.xml:
# <plugin>
#   <artifactId>maven-surefire-plugin</artifactId>
#   <configuration>
#     <argLine>-Xmx1024m</argLine>
#   </configuration>
# </plugin>
```

### Problema: Logs de teste muito verbosos

**Causa:** Nível de log DEBUG ou TRACE ativo

**Solução:**
```bash
# Executar testes com log mínimo
./mvnw test -Dquarkus.log.level=WARN

# Ou editar src/test/resources/application.properties:
# quarkus.log.level=INFO
# quarkus.hibernate-orm.log.sql=false
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
