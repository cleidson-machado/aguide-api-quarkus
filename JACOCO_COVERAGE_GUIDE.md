# 📊 Guia Completo: JaCoCo Coverage no Projeto

**Data:** 2026-07-29
**Status:** ✅ CONFIGURADO E FUNCIONANDO

---

## 🎯 O que é JaCoCo?

**JaCoCo (Java Code Coverage)** é uma biblioteca de análise de cobertura de código para Java. Ele:
- Rastreia quais linhas de código foram executadas durante os testes
- Gera relatórios detalhados de cobertura (HTML, XML, CSV)
- Integra-se com SonarQube para análise de qualidade
- Ajuda a identificar código não testado

---

## ✅ Status Atual no Projeto

### Configuração no `pom.xml`

```xml
<properties>
    <jacoco.version>0.8.12</jacoco.version>
</properties>

<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>${jacoco.version}</version>
    <executions>
        <execution>
            <id>prepare-agent</id>
            <goals>
                <goal>prepare-agent</goal>
            </goals>
        </execution>
        <execution>
            <id>report</id>
            <phase>verify</phase>
            <goals>
                <goal>report</goal>
            </goals>
            <configuration>
                <formats>
                    <format>XML</format>
                    <format>HTML</format>
                </formats>
            </configuration>
        </execution>
    </executions>
</plugin>
```

**O que cada parte faz:**

1. **prepare-agent**: Configura o agente JaCoCo para coletar dados durante execução dos testes
2. **report**: Gera relatórios HTML e XML na fase `verify` do Maven
3. **Formatos**: XML (para SonarQube) e HTML (para visualização local)

---

## 🚀 Como Usar o JaCoCo

### 1. Executar Testes com Coleta de Cobertura

```bash
# Opção 1: Apenas testes (coleta dados, MAS NÃO gera relatório)
./mvnw test

# Opção 2: Testes + Relatório (RECOMENDADO)
./mvnw verify

# Opção 3: Gerar relatório a partir de dados já coletados
./mvnw jacoco:report
```

### 2. Localização dos Arquivos Gerados

```
target/
├── jacoco.exec                   # Dados binários de cobertura (2.3MB)
└── site/
    └── jacoco/
        ├── index.html            # Relatório HTML principal
        ├── jacoco.xml            # Relatório XML (para SonarQube)
        ├── jacoco.csv            # Dados CSV
        └── br.com.aguideptbr.features.*/  # Relatórios por pacote
```

### 3. Visualizar Relatório Local

```bash
# macOS
open target/site/jacoco/index.html

# Linux
xdg-open target/site/jacoco/index.html

# Windows
start target/site/jacoco/index.html

# Ou abra diretamente no navegador:
# file:///Users/cleidson/RestAPIsApps/GoBack_Java_Quarkus/mobile-rest-api/target/site/jacoco/index.html
```

---

## 📊 Interpretando o Relatório

### Métricas de Cobertura

O JaCoCo rastreia diferentes tipos de cobertura:

| Métrica | Descrição | Meta |
|---------|-----------|------|
| **Instructions** | Bytecode executado | > 80% |
| **Branches** | Decisões (if/else, switch) | > 70% |
| **Lines** | Linhas de código executadas | > 80% |
| **Methods** | Métodos executados | > 80% |
| **Classes** | Classes testadas | > 80% |

### Código de Cores

- 🟢 **Verde**: Alta cobertura (> 80%)
- 🟡 **Amarelo**: Cobertura média (50-80%)
- 🔴 **Vermelho**: Baixa cobertura (< 50%)

---

## 🔍 Exemplo de Análise

### Resultado Atual do Projeto

```
Analyzed bundle 'mobile-rest-api' with 127 classes
```

**Como acessar cobertura de uma classe específica:**

1. Abra `target/site/jacoco/index.html`
2. Navegue: `br.com.aguideptbr.features.phone`
3. Clique em `PhoneNumberService.java`
4. Veja linhas cobertas (verde) e não cobertas (vermelho)

**Exemplo de relatório:**

```
PhoneNumberService.java
└── create()           ✅ 95% cobertura
└── update()           ✅ 90% cobertura
└── delete()           ⚠️  60% cobertura
└── validatePhone()    ✅ 100% cobertura
```

---

## 🛠️ Comandos Úteis

### Cobertura de Teste Específico

```bash
# Executar teste específico e ver cobertura
./mvnw test -Dtest=PhoneNumberServiceIntegrationTest
./mvnw jacoco:report
open target/site/jacoco/index.html
```

### Cobertura de Feature Específica

```bash
# Executar testes de uma feature
./mvnw test -Dtest=br.com.aguideptbr.features.phone.**.*Test
./mvnw jacoco:report
```

### Pipeline Completa

```bash
# Limpar tudo e gerar relatório completo
./mvnw clean verify
open target/site/jacoco/index.html
```

### Ver Cobertura Mínima no Terminal

```bash
# Ver sumário no terminal (requer configuração adicional)
./mvnw jacoco:check
```

---

## 🔗 Integração com SonarQube

### Configuração Atual nos Jenkinsfiles

Todos os 3 Jenkinsfiles do projeto já integram JaCoCo + SonarQube:

**Jenkinsfile (develop):**
```groovy
stage('SonarQube Analysis') {
    withSonarQubeEnv() {
        sh """
            ${mvn}/bin/mvn verify sonar:sonar \
                -Dsonar.projectKey=aguide-api-quarkus \
                -Dsonar.projectName='Aguide API Quarkus'
        """
    }
}
```

**Jenkinsfile.test (test branch):**
```groovy
stage('SonarQube Analysis') {
    withSonarQubeEnv() {
        sh """
            ${mvn}/bin/mvn verify sonar:sonar \
                -Dsonar.projectKey=aguide-api-quarkus \
                -Dsonar.projectName='Aguide API Quarkus'
        """
    }
}
```

**Jenkinsfile.production (main):**
```groovy
stage('SonarQube Analysis') {
    withSonarQubeEnv() {
        sh """
            ${mvn}/bin/mvn verify sonar:sonar \
                -Dsonar.projectKey=aguide-api-quarkus \
                -Dsonar.projectName='Aguide API Quarkus'
        """
    }
}
```

### Como Funciona

1. **`./mvnw verify`**: Executa testes e gera `target/jacoco.exec`
2. **`./mvnw jacoco:report`**: Gera `target/site/jacoco/jacoco.xml`
3. **SonarQube Plugin**: Lê `jacoco.xml` e envia para servidor SonarQube
4. **Dashboard SonarQube**: Exibe métricas de cobertura

### Fluxo no Jenkins

```
1. Git Pull
   ↓
2. Maven Compile
   ↓
3. Maven Verify (executa testes + JaCoCo)
   ↓
4. JaCoCo gera jacoco.xml
   ↓
5. SonarQube Scanner envia dados
   ↓
6. Dashboard SonarQube atualizado
```

---

## 🎯 Configurações Avançadas (Opcional)

### Excluir Pacotes da Cobertura

Se você quiser excluir DTOs ou Models da análise:

```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <configuration>
        <excludes>
            <exclude>**/dto/**</exclude>
            <exclude>**/*Model.class</exclude>
            <exclude>**/*Request.class</exclude>
            <exclude>**/*Response.class</exclude>
        </excludes>
    </configuration>
</plugin>
```

### Definir Meta de Cobertura Mínima

```xml
<execution>
    <id>check</id>
    <goals>
        <goal>check</goal>
    </goals>
    <configuration>
        <rules>
            <rule>
                <element>PACKAGE</element>
                <limits>
                    <limit>
                        <counter>LINE</counter>
                        <value>COVEREDRATIO</value>
                        <minimum>0.80</minimum>
                    </limit>
                </limits>
            </rule>
        </rules>
    </configuration>
</execution>
```

Com isso, o build **falhará** se a cobertura for < 80%.

---

## 📋 Checklist de Verificação

Antes de fazer PR/merge:

- [ ] Executei `./mvnw verify`?
- [ ] Cobertura geral está > 80%?
- [ ] Novos métodos têm testes?
- [ ] Visualizei relatório em `target/site/jacoco/index.html`?
- [ ] SonarQube Analysis passou no Jenkins?

---

## 🆘 Problemas Comuns

### Problema: "open target/site/jacoco/index.html" não funciona

**Causa:** Relatório não foi gerado (executou apenas `./mvnw test`)

**Solução:**
```bash
# Opção 1: Gerar relatório a partir de dados existentes
./mvnw jacoco:report
open target/site/jacoco/index.html

# Opção 2: Executar pipeline completa
./mvnw verify
open target/site/jacoco/index.html
```

### Problema: Cobertura mostra 0% ou muito baixa

**Causa:** Testes não estão executando ou dados corrompidos

**Solução:**
```bash
# Limpar e regenerar
rm -rf target/
./mvnw clean verify
```

### Problema: SonarQube não mostra cobertura

**Causa:** `jacoco.xml` não foi gerado ou está no caminho errado

**Solução:**
```bash
# Verificar se arquivo existe
ls -lh target/site/jacoco/jacoco.xml

# Se não existir, gerar:
./mvnw verify
```

**Configuração SonarQube (se necessário):**
```properties
# sonar-project.properties (se usar)
sonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml
```

### Problema: Build lento após adicionar JaCoCo

**Causa:** Instrumentação de bytecode adiciona overhead

**Solução:**
```bash
# Pular JaCoCo durante desenvolvimento rápido
./mvnw test -DskipJacoco=true

# Executar com JaCoCo apenas antes de PR
./mvnw verify
```

---

## 💡 Boas Práticas

### 1. Cobertura ≠ Qualidade

```java
// ❌ Teste inútil (100% cobertura, 0% valor)
@Test
void testGetName() {
    assertEquals("John", user.getName());
}

// ✅ Teste útil (testa lógica de negócio)
@Test
void testValidatePhoneNumber_BrazilianMobile_ShouldPass() {
    PhoneNumberModel phone = createPhone("+55", "67", "984073221");
    assertDoesNotThrow(() -> service.create(testUser.id, phone));
}
```

### 2. Focar em Lógica de Negócio

**Priorizar cobertura:**
- ✅ Services (lógica de negócio)
- ✅ Validações complexas
- ✅ Fluxos críticos (autenticação, pagamento)

**Não priorizar:**
- ❌ Getters/Setters simples
- ❌ DTOs sem lógica
- ❌ Constantes

### 3. Usar com TDD

```bash
# Ciclo TDD com feedback de cobertura:

# 1. Escrever teste que falha
# 2. Fazer teste passar
./mvnw test -Dtest=MinhaNovaFeatureTest

# 3. Ver cobertura
./mvnw jacoco:report
open target/site/jacoco/br.com.aguideptbr.features.minhafuncionalidade/MinhaFeatureService.html

# 4. Refatorar e repetir
```

---

## 📚 Recursos Adicionais

- **JaCoCo Oficial:** https://www.jacoco.org/jacoco/
- **Maven Plugin:** https://www.jacoco.org/jacoco/trunk/doc/maven.html
- **SonarQube Java:** https://docs.sonarqube.org/latest/analysis/languages/java/

---

## 🎉 Resumo Executivo

### ✅ O que está funcionando:

1. ✅ JaCoCo 0.8.12 configurado no `pom.xml`
2. ✅ Dados de cobertura coletados durante `./mvnw test`
3. ✅ Relatórios HTML e XML gerados com `./mvnw verify`
4. ✅ Integração com SonarQube nos 3 Jenkinsfiles
5. ✅ 127 classes analisadas no projeto

### 🚀 Como usar no dia a dia:

```bash
# Desenvolvimento local
./mvnw test -Dtest=MinhaClasseTest    # Executar teste
./mvnw jacoco:report                  # Gerar relatório
open target/site/jacoco/index.html    # Visualizar

# Antes de PR
./mvnw clean verify                   # Pipeline completa
open target/site/jacoco/index.html    # Verificar cobertura

# Jenkins (automático)
# SonarQube Analysis stage executa:
# ./mvnw verify sonar:sonar
# e envia dados para SonarQube
```

---

**Última atualização:** 2026-07-29
**Autor:** GitHub Copilot Assistant
**Status:** ✅ Documentação completa e validada
