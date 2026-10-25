# FASE 3 - Plano de Execucao Incremental (Sem Over Engineering)

Data: 2026-07-29
Escopo: roadmap pragmatico para evoluir qualidade, testes e observabilidade da API Quarkus em ambiente self-hosted (Linux + Docker + VPS privado).
Base: diagnostico da FASE 1 e proposta tecnica da FASE 2.

---

## 0) Nota de abordagem

Nao vou abrir rodada de 8-15 perguntas agora porque ja existe diagnostico tecnico com evidencias de codigo, testes, Docker e pipelines.

Quando abriria perguntas primeiro:
- se nao houvesse acesso aos Jenkinsfiles/compose/codigo;
- se o ambiente real estivesse indefinido;
- se faltassem evidencias de acoplamento/testabilidade.

---

## 1) Checklist de diagnostico (projeto)

Use esta checklist antes de qualquer mudanca estrutural.

### 1.1 Arquitetura e testabilidade
- [ ] Controllers com pouca regra de negocio?
- [ ] Services sem dependencia direta de Active Record estatico onde mock seria necessario?
- [ ] Repositories/ports consistentes por modulo?
- [ ] Dependencias externas encapsuladas em adapters?
- [ ] Erros padronizados (estrutura unica para 4xx/5xx)?
- [ ] Security/filter com responsabilidade controlada (nao monolito)?

### 1.2 Testes atuais
- [ ] Unit tests realmente isolados (sem banco/rede)?
- [ ] Integracao com escopo claro (DB, serializacao, auth, transacao)?
- [ ] Nomes e tags padronizados por tipo de teste?
- [ ] Casos de erro importantes cobertos (auth, permissao, validacao, timeout)?
- [ ] Bugs historicos transformados em testes de regressao?

### 1.3 Build e qualidade
- [ ] JaCoCo gerando XML para Sonar?
- [ ] Quality Gate bloqueia pipeline?
- [ ] Relatorios de teste publicados no Jenkins?
- [ ] Pipeline possui timeout por etapa e fail-fast adequado?

---

## 2) Principais riscos e limitacoes do estado atual

Riscos observados:
1. Acoplamento arquitetural heterogeneo (mix Active Record + Repository + logica em controller).
2. Parte dos "unit tests" e de integracao na pratica, aumentando custo e tempo.
3. Sonar integrado, mas sem gate bloqueante de deploy identificado.
4. Segredos e credenciais em arquivos de compose/properties (baixo nivel de governanca).
5. Observabilidade ainda basica para detectar degradacao comportamental apos deploy.

Limitacoes operacionais:
1. Stack Docker funcional, mas com historico de multiplos arquivos/copias e chance de divergencia.
2. Falta de contrato formal minimo por endpoint critico (schema/compatibilidade).
3. Possivel custo de latencia por validacoes de auth no filtro a cada request com lookup de banco.

---

## 3) Arquitetura alvo recomendada

Objetivo: robustez com simplicidade, sem reescrever o sistema.

### 3.1 Camadas alvo
1. Resource/controller: HTTP puro (entrada/saida, status, headers).
2. Application service: regras de negocio e orquestracao.
3. Domain/core: validacoes e regras chave reutilizaveis.
4. Repository/port: acesso a dados e contratos de persistencia.
5. Adapter externo: clientes HTTP externos encapsulados.

### 3.2 Regras de desenho
- Controllers nao persistem diretamente.
- Services nao chamam static find/persist quando o teste exigir isolamento.
- Dependencias externas entram por interface (port) e saem por adapter.
- Erros de dominio mapeados por exception mapper unico.

### 3.3 Tipos de teste alinhados as camadas
- Unit: domain + service isolado (Mockito).
- Integracao: resource + service + repository + DB real.
- Contrato: schema/payload por endpoint critico.
- Smoke: checks minimos pos-deploy.

---

## 4) Estrutura sugerida do framework de testes

## 4.1 Estrutura de pacotes

```text
src/test/
  java/br/com/aguideptbr/tests/
    unit/
      auth/
      content/
      usermessage/
    integration/
      api/
      persistence/
    contract/
      api/
    regression/
      incidents/
    smoke/
      deploy/
    e2e/
      flows/
    support/
      builders/
      factories/
      fixtures/
      containers/
      auth/
      assertions/
  resources/
    testdata/
      json/
      sql/
    schemas/
    wiremock/
      mappings/
      __files/
```

## 4.2 Convencoes
- Tag por tipo: unit, integration, contract, regression, smoke, e2e.
- Nome orientado a comportamento: should_X_when_Y.
- 1 intencao por teste.

---

## 5) Roadmap incremental (do simples ao maduro)

## Fase A - Estabilizar arquitetura e testabilidade (2-3 semanas)
Objetivo: preparar terreno para testes maduros.

Passos:
1. Identificar endpoints/services com maior acoplamento (auth, user, content).
2. Extrair persistencia direta de controllers para services.
3. Introduzir repositorios/ports onde hoje ha static find/persist critico.
4. Padronizar response de erro e logging minimo util.

Saida:
- Codigo mais isolavel e mais simples de testar.

## Fase B - Revisar e limpar testes unitarios atuais (1-2 semanas)
Objetivo: recuperar feedback rapido e confiavel.

Passos:
1. Separar o que e unitario vs integracao no naming/tags.
2. Converter testes pseudo-unitarios acoplados a banco em:
   - unit real com mocks, ou
   - integracao declarada explicitamente.
3. Introduzir builders/factories deterministicas.

Saida:
- Suite mais clara, menos flakey, mais rapida no PR.

## Fase C - Base de testes de integracao e API (2-4 semanas)
Objetivo: validar comportamento fim-a-fim da API com foco funcional.

Passos:
1. Consolidar REST Assured para endpoints criticos.
2. Adotar Testcontainers para Postgres em integracao (ou perfil controlado ate migracao).
3. Definir conjunto minimo de testes de contrato (schema de resposta para endpoints chave).
4. Cobrir auth, paginacao, filtros, erros, permissao, idempotencia basica.

Saida:
- Confiança real de API para deploy.

## Fase D - Integracao com CI self-hosted (1-2 semanas)
Objetivo: colocar ordem no fluxo Jenkins.

Passos:
1. Pipeline PR: unit + integracao curta + sonar scan.
2. Pipeline main/release: unit + integracao completa + contrato + regressao.
3. Publicar relatorios JUnit e cobertura em todo pipeline.
4. Fail-fast em etapa critica e timeout por stage.

Saida:
- Pipeline previsivel, com feedback util.

## Fase E - Quality gates SonarQube (1 semana)
Objetivo: bloquear regressao de qualidade no new code.

Passos:
1. Configurar gate de new code (bugs/vulnerabilidades/cobertura minima).
2. Adicionar waitForQualityGate bloqueante antes de build de imagem/deploy.
3. Definir excecoes formais (aprovacao tecnica) para casos raros.

Saida:
- Governanca objetiva, sem burocracia excessiva.

## Fase F - Smoke tests + observabilidade util (2-3 semanas)
Objetivo: detectar degradacao apos deploy com custo baixo.

Passos:
1. Smoke pos-deploy obrigatorio (health + 2-3 endpoints criticos).
2. Expor metricas Micrometer e coletar com Prometheus.
3. Dashboards Grafana minimos: disponibilidade, 5xx, p95 por endpoint critico.
4. Monitorizacao sintetica simples (blackbox ou script periodic).

Saida:
- Visibilidade operacional sem exagero.

## Fase G - So depois avaliar BDD e avancados (opcional)
Objetivo: decidir com base em dor real.

Adotar apenas se houver necessidade comprovada:
- Cucumber/BDD formal.
- Relatorios avancados e evidencias enriquecidas.
- Suites E2E extensas.

Nao recomendado agora:
- BDD full sem participacao ativa de negocio.
- Ferramental pesado de pouca relacao custo-beneficio.

---

## 6) Ferramentas: o que recomendo e o que NAO recomendo agora

Recomendo agora:
1. JUnit 5
2. REST Assured
3. Mockito
4. Testcontainers
5. WireMock
6. JaCoCo
7. SonarQube gate bloqueante
8. Micrometer + Prometheus + Grafana (pacote minimo)

Nao recomendo agora:
1. Cucumber (sem demanda de BDD real).
2. Framework paralelo de API test (Karate/Postman collection como base principal) enquanto REST Assured ja atende.
3. Tracing distribuido completo (Jaeger/Tempo) antes de maturidade basica de metricas e alertas.
4. Testes E2E muito extensos no PR.

---

## 7) Refactors arquiteturais necessarios (se codigo continuar acoplado)

Lista objetiva de refactors prioritarios:
1. Remover persistencia direta de controllers.
2. Padronizar acesso a dados por modulo (repository/port consistente).
3. Extrair regras de negocio repetidas para services/domain helpers.
4. Reduzir uso de Active Record estatico em fluxos que precisam de unit test isolado.
5. Separar validacao de auth em componentes menores (parsing, policy, user status).
6. Padronizar DTOs de request/response e erros.
7. Criar camada adapter para integracoes externas.
8. Introduzir boundary clara para seguranca/autorizacao por caso de uso.

---

## 8) Checklist pratica para auditar o projeto Quarkus atual

### 8.1 Codigo
- [ ] Cada endpoint tem service correspondente?
- [ ] Existe endpoint com persist() em controller?
- [ ] Existe regra de negocio em entity/model?
- [ ] Existe service acoplado a static find() que impede mock limpo?
- [ ] Exception mapper cobre erros de negocio padronizados?

### 8.2 Testes
- [ ] Unit tests sem DB/rede?
- [ ] Integracao com setup deterministico?
- [ ] Contratos minimos de payload existem para endpoints criticos?
- [ ] Regressao de bugs historicos foi codificada?
- [ ] Smoke pos-deploy cobre caminhos criticos?

### 8.3 Build
- [ ] Tags e suites separadas?
- [ ] Cobertura de new code definida?
- [ ] Tempo de pipeline PR dentro do alvo?

---

## 9) Checklist para auditar Docker, Jenkins e SonarQube

### 9.1 Docker/VPS
- [ ] Containers criticos com healthcheck?
- [ ] Volumes de dados persistentes definidos?
- [ ] Credenciais fora de arquivos versionados quando possivel?
- [ ] Rede docker padronizada sem duplicacao de compose conflitante?
- [ ] Backup e restore testados para Postgres e Sonar?

### 9.2 Jenkins
- [ ] Pipelines separados por objetivo (PR/main/nightly)?
- [ ] Relatorios JUnit publicados?
- [ ] Cobertura publicada?
- [ ] Fail-fast e timeouts configurados?
- [ ] Quality gate bloqueia etapa de deploy?

### 9.3 SonarQube
- [ ] Projeto com new code period definido?
- [ ] Gate com condicoes realistas (sem numero impossivel)?
- [ ] jacoco.xml sendo lido no pipeline?
- [ ] Historico de tendencia acompanhado por release?

---

## 10) Proposta de framework de testes sem exageros

MVP do framework (primeiros 60-90 dias):
1. Convenio de tags e estrutura de pastas.
2. Biblioteca de builders/factories/fixtures deterministicas.
3. REST Assured para API tests.
4. Testcontainers para Postgres em integracao.
5. WireMock para externos.
6. Jenkins com Quality Gate bloqueante.
7. Smoke pos-deploy + observabilidade minima.

Nao incluir no MVP:
- BDD formal.
- Teste de carga pesado no mesmo pipeline funcional.
- Ferramenta de report sofisticada sem dono.

---

## 11) Arvore de pastas sugerida

```text
aguide-api-quarkus/
  src/
    main/
      java/...
      resources/...
    test/
      java/br/com/aguideptbr/tests/
        unit/
        integration/
        contract/
        regression/
        smoke/
        e2e/
        support/
          builders/
          factories/
          fixtures/
          containers/
      resources/
        testdata/json/
        testdata/sql/
        schemas/
        wiremock/mappings/
        wiremock/__files/
  Jenkinsfile
  Jenkinsfile.test
  Jenkinsfile.production
  pom.xml
```

---

## 12) Exemplo de pom.xml (trecho) para fases 3-5

```xml
<dependencies>
  <dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-junit5</artifactId>
    <scope>test</scope>
  </dependency>
  <dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-junit5-mockito</artifactId>
    <scope>test</scope>
  </dependency>
  <dependency>
    <groupId>io.rest-assured</groupId>
    <artifactId>rest-assured</artifactId>
    <scope>test</scope>
  </dependency>
  <dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>junit-jupiter</artifactId>
    <version>1.20.4</version>
    <scope>test</scope>
  </dependency>
  <dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>postgresql</artifactId>
    <version>1.20.4</version>
    <scope>test</scope>
  </dependency>
  <dependency>
    <groupId>com.github.tomakehurst</groupId>
    <artifactId>wiremock-jre8</artifactId>
    <version>2.35.2</version>
    <scope>test</scope>
  </dependency>
</dependencies>

<build>
  <plugins>
    <plugin>
      <artifactId>maven-surefire-plugin</artifactId>
      <version>3.5.3</version>
      <configuration>
        <groups>!integration &amp; !e2e</groups>
      </configuration>
    </plugin>
    <plugin>
      <artifactId>maven-failsafe-plugin</artifactId>
      <version>3.5.3</version>
      <executions>
        <execution>
          <goals>
            <goal>integration-test</goal>
            <goal>verify</goal>
          </goals>
        </execution>
      </executions>
    </plugin>
  </plugins>
</build>
```

Observacao: e apenas exemplo de estrutura. Ajustar ao pom real em fase de implementacao.

---

## 13) Exemplo real de teste de endpoint com REST Assured

```java
package br.com.aguideptbr.tests.integration.api;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

@QuarkusTest
@Tag("integration")
class AuthHealthEndpointIT {

    @Test
    void should_return_health_ok() {
        given()
          .accept(ContentType.JSON)
        .when()
          .get("/api/v1/auth/health")
        .then()
          .statusCode(200)
          .contentType(ContentType.JSON)
          .body("status", equalTo("OK"))
          .body("message", notNullValue())
          .body("timestamp", notNullValue());
    }
}
```

---

## 14) Exemplo de teste de integracao com Quarkus

```java
package br.com.aguideptbr.tests.integration.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import br.com.aguideptbr.features.content.ContentRecordModel;
import br.com.aguideptbr.features.content.ContentType;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.transaction.Transactional;

@QuarkusTest
@Tag("integration")
class ContentPersistenceIT {

    @Test
    @Transactional
    void should_persist_and_read_content() {
        ContentRecordModel m = new ContentRecordModel();
        m.title = "Integration Test Content";
        m.description = "payload validation";
        m.videoUrl = "https://example.test/video/123";
        m.channelId = "UC123TEST";
        m.channelName = "Test Channel";
        m.type = ContentType.VIDEO;

        m.persist();

        assertNotNull(m.id);
        ContentRecordModel found = ContentRecordModel.findById(m.id);
        assertNotNull(found);
        assertEquals("Integration Test Content", found.title);
    }
}
```

---

## 15) Exemplo de pipeline Jenkins self-hosted (simples e pragmatico)

```groovy
pipeline {
  agent any

  stages {
    stage('Checkout') {
      steps { checkout scm }
    }

    stage('Unit + Fast Integration') {
      steps {
        sh './mvnw -B test -Dgroups="unit,integration"'
      }
      post {
        always {
          junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml'
        }
      }
    }

    stage('Sonar Scan') {
      steps {
        withSonarQubeEnv() {
          sh './mvnw -B verify sonar:sonar -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml'
        }
      }
    }

    stage('Quality Gate') {
      steps {
        timeout(time: 10, unit: 'MINUTES') {
          waitForQualityGate abortPipeline: true
        }
      }
    }

    stage('Build Image') {
      when { branch 'main' }
      steps {
        sh 'docker compose -f docker-compose.yml build --no-cache'
      }
    }

    stage('Deploy + Smoke') {
      when { branch 'main' }
      steps {
        sh 'docker compose -f docker-compose.yml up -d'
        sh 'curl -fsS http://localhost:8083/q/health | grep -q "UP"'
      }
    }
  }
}
```

---

## 16) Proposta simples de observabilidade (Grafana + Prometheus + health/synthetic)

Arquitetura minima:
1. API Quarkus exporta metricas HTTP (Micrometer/Prometheus endpoint).
2. Prometheus faz scrape da API e do host/containers.
3. Grafana mostra 1 dashboard principal de API.
4. Check sintetico periodico valida health + 1 endpoint critico com payload minimo.

Metricas minimas no dashboard:
- disponibilidade por endpoint critico;
- taxa de erro 5xx por minuto;
- latencia p95 por endpoint;
- volume de requests;
- uptime de banco e API.

Alertas minimos:
- health endpoint down por 2-5 min;
- 5xx acima de limiar por janela curta;
- latencia p95 acima de limiar definido por endpoint critico.

Exemplo de monitorizacao sintetica (conceito):
- cron job a cada 1 min:
  - GET /q/health (espera UP)
  - GET endpoint autenticado com token tecnico (espera 200 + campo obrigatorio)
  - grava resultado em Prometheus pushgateway ou log processavel

---

## 17) Plano de execucao resumido (prioridade)

1. Refactor de testabilidade (A)
2. Limpeza de unit tests (B)
3. Base de integracao/API/contrato (C)
4. CI robusto self-hosted (D)
5. Quality gates reais (E)
6. Smoke + observabilidade minima (F)
7. Avaliar BDD/avancados somente com necessidade comprovada (G)

---

## 18) Confirmacao de escopo da FASE 3

- Este documento e apenas plano de execucao incremental.
- Nenhum arquivo de codigo da API foi alterado.
- Nenhuma mudanca de infraestrutura foi aplicada.
- Documento salvo em framework_testes_automatizados_1/todo_fase_3.md.
