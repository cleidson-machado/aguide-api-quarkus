# FASE 1 - Diagnostico Tecnico Atual (Quarkus API + Infra + CI)

Data da analise: 2026-07-29
Escopo: apenas diagnostico. Nenhuma alteracao de codigo foi realizada.

## 1) Resumo Executivo

O projeto ja esta em um nivel relevante de pre-producao: API Quarkus funcional, banco PostgreSQL isolado em container, migrations com Flyway, Jenkins e SonarQube ativos, e suite de testes executando com sucesso.

Pontos fortes principais:
- Pipeline e stack self-hosted de fato operacionais (Jenkins, SonarQube, Postgres, Nginx Proxy Manager, Portainer).
- Testes em funcionamento e sem falhas no estado atual (100 testes, 0 falhas, 0 erros).
- Boa cobertura de regras de negocio em alguns dominios (ex.: mensagens, telefone, ownership, ranking).
- Flyway e separacao de perfis dev/prod/test ja estruturados.

Riscos principais:
- Arquitetura de dominio heterogenea (mix Active Record Panache + Repositories + logica no controller), com acoplamento elevado em partes criticas.
- Testes de "unit" em alguns pontos sao, na pratica, testes de integracao com banco.
- Pipeline nao aplica Quality Gate de Sonar como bloqueio real de deploy.
- Segredos sensiveis e credenciais aparecem em compose/properties (adequado para laboratorio, fraco para maturidade profissional).
- Observabilidade tecnica ainda basica (sem stack de metricas/tracing consolidada para operacao).

Conclusao objetiva:
- Base boa para evolucao incremental.
- Sem necessidade de reescrever arquitetura inteira.
- Evolucao recomendada: endurecer contratos, isolar dominio, fortalecer piramide de testes e fechar gates de qualidade no CI.

---

## 2) Evidencias Coletadas

### Codigo e build
- Projeto Quarkus 3.23.3, Java 17 no Maven, com build plugin Quarkus e JaCoCo configurado.
- Dependencias-chave: RESTEasy classic, Panache ORM, PostgreSQL JDBC, Flyway, JWT, OpenAPI.

### Testes
- 14 arquivos em src/test.
- Execucao local realizada com sucesso: 100 testes, 0 falhas, 0 erros, 0 skipped.
- Ha combinacao de:
	- QuarkusTest com banco real (integracao).
	- Mockito puro em parte dos services de usermessage.

### Infra Docker em runtime (VPS)
Containers ativos observados:
- aguide-api
- quarkus_postgres
- quarkus_pgadmin
- jenkins
- sonarqube
- nginx-proxy-manager
- portainer
- landing-page-server

### CI/CD
- 3 Jenkinsfiles no repo da API: geral, teste e producao.
- Compose de Jenkins e SonarQube em pasta dedicada de automacao.

---

## 3) Diagnostico Obrigatorio por Tema

## 3.1 Estrutura atual do projeto Quarkus

Estado:
- Estrutura organizada por "features" (auth, content, ownership, phone, user, usermessage, userposition), o que favorece crescimento modular.
- Existe separacao parcial controller/service/repository em alguns modulos (ex.: phone, usermessage, userposition).
- Em outros modulos, controllers e services acessam entidades Panache diretamente (Active Record), sem camada de repositorio dedicada.

Leitura tecnica:
- Arquitetura esta em transicao entre estilo rapido de entrega e estilo mais profissional de camadas.
- Isso e comum em pre-producao e pode ser melhorado sem ruptura.

## 3.2 Separacao de responsabilidades

Bom:
- Em phone e usermessage, services concentram regras de negocio e usam repositories.
- Em userposition ha tentativa clara de SOLID (services especializados de validacao, metrics e milestones).

A melhorar:
- Controllers ainda contem logica e acesso direto a persistencia em alguns endpoints (ex.: create/update/delete com entidade Panache diretamente).
- Services como AuthService usam static find/persist da entidade, acoplando regra de negocio ao ORM.

Impacto:
- Testabilidade reduzida.
- Evolucao de regras e troca de persistencia mais custosas.

## 3.3 Nivel de acoplamento (resource/service/repository/external)

Acoplamentos observados:
- Alto acoplamento a Panache Active Record em partes centrais (Auth, User, Content).
- Em classes JAX-RS provider (filtros/mappers), dependencia de injecao por campo (limite tecnico do framework, aceitavel).
- Filter de autenticacao consulta banco diretamente para cada request protegida.

Risco operacional:
- A validacao por request pode elevar latencia/carga de banco em picos.
- Comportamentos de seguranca e autorizacao ficam mais dificeis de simular em testes unitarios puros.

## 3.4 Preparacao para testes mais profissionais

Maturidade atual: media.

Pontos positivos:
- Suite automatizada executavel.
- Ambiente de teste com banco dedicado (quarkus_test).
- JaCoCo e relatorio XML para Sonar disponiveis.

Limitacoes:
- "Unit tests" de alguns modulos sao mais de persistencia do que de regra de negocio isolada.
- Forte dependencia de estado de banco em varios testes QuarkusTest.
- Ausencia de estrategia explicita por piramide (unitarios rapidos, integracao, contrato, smoke).

## 3.5 Uso de interfaces, DI e abstracoes para mock/stub/integracao

Estado:
- DI por construtor e bem utilizada em varios services.
- Interfaces de porta/adapter ainda pouco exploradas.
- Repositorios existem em alguns modulos, mas nao de forma uniforme.

Consequencia:
- Onde ha repositorio + injecao por construtor, mock e stub sao simples.
- Onde ha Active Record estatico na entidade, o teste tende a ficar acoplado ao banco ou a tecnicas de mock menos elegantes.

## 3.6 Pontos problematicos de design

Itens relevantes:
- Estilo hibrido (camadas + Active Record direto) gera inconsistencia arquitetural.
- Controllers com responsabilidade de persistencia em alguns casos.
- Filtro de autenticacao com muita responsabilidade (parsing, validacoes, lookup de usuario, respostas de erro).
- Logging muito verboso para fluxo de autenticacao (bom para debug, ruim para operacao em escala se nao houver controle fino de nivel).

## 3.7 Qualidade dos testes unitarios atuais

Diagnostico:
- Os testes existentes "fazem sentido" e estao trazendo valor (regressoes reais cobertas).
- Parte deles esta mal nomeada quanto ao tipo: alguns "unit" sao integracao de DB.
- Existem testes bem focados com Mockito nos services de usermessage, o que e bom sinal.

Risco de fragilidade:
- Testes dependentes de banco e fixtures podem ficar lentos/frageis com crescimento de schema e dados.

## 3.8 Adequacao da infra Docker para testes mais maduros

Estado atual:
- Infra Docker self-hosted funcional e suficiente para evoluir testes.
- Rede compartilhada proxy-network em uso entre varios stacks.
- Banco e ferramentas de qualidade estao em containers estaveis ha meses.

Atenções:
- Composes duplicados e historicos aumentam risco de divergencia operacional.
- Credenciais em claro em multiplos arquivos.
- Falta padronizacao mais forte de ambientes (dev/test/stage/prod) em manifests unificados.

## 3.9 CI atual (Jenkins) para pipelines de teste/qualidade/relatorios/gates

Pontos fortes:
- Pipeline de teste com etapas dedicadas para validacao de config de teste, limpeza de banco, execucao Maven + Sonar.
- Pipeline de producao separado com validacao de branch.

Lacunas criticas para nivel profissional:
- Nao foi identificado gate bloqueante de Sonar (waitForQualityGate) antes de deploy.
- Etapas de deploy continuam mesmo com health-check falhando (ha echo de alerta, mas sem fail hard).
- Uso de git reset --hard e git clean -fd no proprio diretorio da aplicacao no host: pragmatico, mas arriscado para operacao compartilhada.
- Artefatos e relatorios de teste nao estao sendo publicados de forma consistente em todos pipelines.

## 3.10 Enquadramento atual do SonarQube

Estado:
- SonarQube esta operacional e integrado ao Maven.
- JaCoCo XML esta sendo passado no pipeline de testes.

Gap principal:
- Qualidade e medida, mas aparentemente nao governada por gate obrigatorio antes de promover deploy.

## 3.11 Papel de Grafana/Prometheus e afins neste contexto

Faz sentido sim, sem over engineering, para:
- Monitorar disponibilidade e latencia de endpoints criticos.
- Visualizar erro por endpoint/status code e tendencia de falhas.
- Correlacionar impacto de deploy com degradacao de comportamento.

Estado atual observado:
- Nao ha evidencia de stack formal de metricas da aplicacao (Micrometer/Prometheus) no projeto analisado.

Recomendacao de enquadramento (ainda fase diagnostica):
- Introduzir observabilidade basica orientada a API health, latencia e taxa de erro, antes de tracing distribuido completo.

---

## 4) Diagnostico de Maturidade (Pragmatico)

Escala 1-5:
- Arquitetura de codigo: 3.0
- Testes automatizados: 3.2
- CI/CD e gates de qualidade: 2.8
- Seguranca operacional (segredos/config): 2.5
- Observabilidade: 2.2

Maturidade geral estimada: 2.9/5

Interpretacao:
- Ja passou de "prototipo".
- Ainda nao esta no patamar "profissional previsivel" em governanca de qualidade e operacao.

---

## 5) Prioridades de Evolucao (sem over engineering)

1. Fechar gates de qualidade no CI antes de deploy (quality gate + fail hard em health-check).
2. Padronizar camada de acesso a dados por modulo (reduzir mistura Active Record/Repository em pontos criticos).
3. Organizar estrategia de testes por niveis (unit rapido, integracao, contrato, smoke).
4. Endurecer gestao de segredos e variaveis de ambiente.
5. Introduzir observabilidade minima orientada a operacao (metricas e alertas basicos).

Nota:
As prioridades acima sao apenas diretrizes do diagnostico. A proposta de arquitetura-alvo incremental sera feita nas proximas fases, conforme solicitado.

---

## 6) Confirmacao de Escopo

- Nenhum arquivo de codigo da API foi alterado.
- Nenhuma mudanca de infraestrutura foi aplicada.
- Este documento registra somente a FASE 1 (assessment tecnico do estado atual).

