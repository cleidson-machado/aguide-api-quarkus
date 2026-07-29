# 🚀 Melhorias para o Jenkinsfile.test

## Análise do Pipeline Atual

**Arquivo:** `Jenkinsfile.test` (Declarative Pipeline)

### Pontos Fortes
- ✅ Pipeline bem documentado com outputs ricos
- ✅ Separação clara de stages (Build → Validar Config → Limpar DB → Testes)
- ✅ Verificação de branch (só executa em `develop*`)
- ✅ Validação de configurações de teste antes de executar
- ✅ Stage de verificação de testes pulados (JWT)
- ✅ Tratamento de `post` (success, failure, always)
- ✅ Uso de `withSonarQubeEnv()` para análise
- ✅ Configuração de ambiente para Docker (hostname `quarkus_postgres`)

### Pontos a Melhorar

#### 🟡 1. Uso de caminho fixo /opt/apps/aguide-api-quarkus
**Problema:** O pipeline usa `sh 'cd /opt/apps/aguide-api-quarkus'` repetidamente.

**Solução:** Usar `${WORKSPACE}` ou `dir()` do Jenkins.

```groovy
// ANTES:
sh 'cd /opt/apps/aguide-api-quarkus && git fetch origin'

// DEPOIS:
dir(env.WORKSPACE) {
    sh 'git fetch origin'
}
```

**Importante:** Se o workspace Jenkins for diferente de `/opt/apps/aguide-api-quarkus`, o código atual quebra. Verificar configuração do Jenkins.

#### 🟡 2. Hard reset no checkout
**Problema:** `git reset --hard origin/${GIT_BRANCH}` e `git clean -fd` são destrutivos.

```groovy
sh '''
    cd /opt/apps/aguide-api-quarkus
    git fetch origin
    git reset --hard origin/${GIT_BRANCH}
    git clean -fd
'''
```

**Risco:** Se o workspace não estiver limpo (ex: build anterior interrompido), perde-se alterações não commitadas no workspace.

**Solução:** Manter como está para branches de CI, mas considerar `checkout scm` do Jenkins:

```groovy
stage('Checkout') {
    steps {
        checkout scm  // Usa a configuração do Jenkins (credenciais, branch, etc.)
    }
}
```

#### 🟡 3. SonarQube e testes rodam no mesmo stage
**Problema:** `mvn verify sonar:sonar` roda testes E análise no mesmo comando.

```groovy
${mvn}/bin/mvn verify sonar:sonar -Dsonar.projectKey=...
```

**Risco:** Se os testes falharem, o SonarQube não é executado. Isso é aceitável para feedback rápido, mas:
- Perde-se a análise de código mesmo quando testes falham por ambiente
- O pipeline não gera relatório JaCoCo separadamente

**Solução (opcional):** Separar em 2 stages:

```groovy
stage('Executar Testes') {
    steps {
        sh '${mvn}/bin/mvn verify'
    }
}

stage('Análise SonarQube') {
    steps {
        withSonarQubeEnv() {
            sh '${mvn}/bin/mvn sonar:sonar -Dsonar.projectKey=aguide-api-quarkus'
        }
    }
}
```

**Contraponto:** Isso adiciona complexidade. O modelo atual (1 stage) é aceitável para CI rápido.

#### 🔵 4. Stage de limpeza de banco poderia ser mais robusto
**Problema:** O stage atual faz cleanup manual via SQL.

```groovy
docker exec quarkus_postgres psql -U quarkus -d quarkus_test <<EOF
    DROP SCHEMA IF EXISTS public CASCADE;
    CREATE SCHEMA public;
EOF
```

**Melhoria:** Usar `./mvnw flyway:clean` ou script dedicado de reset de banco.

```groovy
stage('Reset Database for Tests') {
    steps {
        sh '''
            cd ${WORKSPACE}
            # Usa quarkus:dev com profile test para resetar
            # Ou chama script dedicado
            docker exec quarkus_postgres psql -U quarkus -d quarkus_test -c "
                DROP SCHEMA IF EXISTS public CASCADE;
                CREATE SCHEMA public;
            "
        '''
    }
}
```

#### 🔵 5. Tratamento de variáveis de ambiente duplicado
**Problema:** `PROJECT_DIR` e `GIT_REPO` são definidos mas não usados.

```groovy
environment {
    PROJECT_DIR = '/opt/apps/aguide-api-quarkus'
    GIT_REPO = 'https://github.com/cleidson-machado/aguide-api-quarkus.git'
}
```

**Solução:** Remover variáveis não utilizadas ou usá-las:

```groovy
environment {
    GIT_BRANCH = 'develop'
    MAVEN_OPTS = '-Dmaven.repo.local=/var/jenkins_home/.m2/repository'
}
```

#### 🔵 6. Adicionar timeout global
**Problema:** Pipeline pode ficar executando indefinidamente se travar.

**Solução:** Adicionar `options` no pipeline:

```groovy
pipeline {
    agent any
    options {
        timeout(time: 30, unit: 'MINUTES')  // Máximo 30 min
        buildDiscarder(logRotator(numToKeepStr: '20'))  // Manter últimos 20 builds
    }
    // ...
}
```

#### 🔵 7. Adicionar notificação (Slack/Email) para falhas
**Problema:** Atualmente só loga no console.

**Sugestão (opcional):**

```groovy
post {
    failure {
        script {
            // Exemplo com Slack
            // slackSend(channel: '#dev-alerts', 
            //           color: 'danger', 
            //           message: "Pipeline FAILED: ${env.JOB_NAME} [#${env.BUILD_NUMBER}]")
            
            // Ou email
            // emailext(
            //     to: 'dev-team@example.com',
            //     subject: "FAILED: ${env.JOB_NAME} - Build #${env.BUILD_NUMBER}",
            //     body: "Pipeline failed. Check: ${env.BUILD_URL}console"
            // )
        }
    }
}
```

---

## 📋 Plano de Ação Resumido

| # | Melhoria | Prioridade | Esforço |
|---|----------|-----------|---------|
| 1 | Usar `dir(env.WORKSPACE)` em vez de caminho fixo | 🟡 Média | 30min |
| 2 | Usar `checkout scm` em vez de git reset manual | 🟡 Média | 15min |
| 3 | Separar SonarQube dos testes (opcional) | 🔵 Baixa | 30min |
| 4 | Melhorar stage de limpeza de banco | 🔵 Baixa | 15min |
| 5 | Remover variáveis não utilizadas | 🔵 Baixa | 5min |
| 6 | Adicionar timeout global | 🟡 Média | 5min |
| 7 | Adicionar notificação de falha | 🔵 Baixa | 30min |

---

## 🔧 Mudanças Sugeridas no Código

```groovy
pipeline {
    agent any
    
    triggers {
        githubPush()
    }
    
    options {
        timeout(time: 30, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr: '20'))
    }

    environment {
        GIT_BRANCH = 'develop'
        MAVEN_OPTS = '-Dmaven.repo.local=/var/jenkins_home/.m2/repository'
    }

    stages {
        stage('Pipeline Info') {
            steps {
                script {
                    def causes = currentBuild.getBuildCauses('hudson.model.Cause$UserIdCause')
                    def userName = causes ? causes[0].userName : 'Trigger Automático'
                    echo "📊 INFORMAÇÕES DO JENKINS PIPELINE"
                    echo "🆔 Build ID: ${BUILD_ID}"
                    echo "🌿 Branch Git: ${env.GIT_BRANCH}"
                    echo "📂 Workspace: ${WORKSPACE}"
                    echo "⏰ Timeout: 30 minutos"
                }
            }
        }

        stage('Checkout') {
            steps {
                checkout scm
                script {
                    env.GIT_COMMIT_SHORT = sh(
                        script: "git rev-parse --short HEAD",
                        returnStdout: true
                    ).trim()
                }
            }
        }

        // Demais stages permanecem similares...
        // Substituir 'cd /opt/apps/aguide-api-quarkus' por dir(env.WORKSPACE)
    }
}
```

---

## 📊 Impacto Geral

**As mudanças propostas são incrementais e não alteram o comportamento funcional do pipeline.** 
- **Prioridade:** Nenhuma é crítica. O pipeline atual funciona.
- **Benefício:** Mais robustez, melhores práticas, prevenção de timeouts.
- **Risco de mudança:** Muito baixo.
