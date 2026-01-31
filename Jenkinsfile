pipeline {
    agent any

    environment {
        // Configurações do projeto
        PROJECT_DIR = '/opt/apps/aguide-api-quarkus'
        GIT_REPO = 'https://github.com/cleidson-machado/aguide-api-quarkus.git'
        GIT_BRANCH = 'develop-data-objects'  // Ajuste conforme a branch
        MAVEN_OPTS = '-Dmaven.repo.local=/var/jenkins_home/.m2/repository'
    }

    stages {
        stage('Pipeline Info') {
            steps {
                script {
                    // Captura quem iniciou o build de forma nativa
                    def causes = currentBuild.getBuildCauses('hudson.model.Cause$UserIdCause')
                    def userName = causes ? causes[0].userName : 'Trigger Automático'

                    echo '================================================'
                    echo '📊 INFORMAÇÕES DO JENKINS PIPELINE'
                    echo '================================================'
                    echo "🆔 Build ID: ${BUILD_ID}"
                    echo "🔢 Build Number: ${BUILD_NUMBER}"
                    echo "📝 Job Name: ${JOB_NAME}"
                    echo "🔗 Build URL: ${BUILD_URL}"
                    echo "👤 Iniciado por: ${userName}"
                    echo "🌿 Branch Git: ${env.GIT_BRANCH}"
                    echo "📂 Workspace: ${WORKSPACE}"
                    echo "🏠 Jenkins Home: ${JENKINS_HOME}"
                    echo "🖥️  Node Name: ${NODE_NAME}"
                    echo "⏰ Timestamp: ${new Date()}"
                    echo '================================================'
                }
            }
        }

        stage('Checkout') {
            steps {
                echo '📥 Atualizando código do repositório...'
                sh '''
                    cd /opt/apps/aguide-api-quarkus
                    git fetch origin
                    git reset --hard origin/${GIT_BRANCH}
                    git clean -fd
                '''

                // Captura informações do commit
                script {
                    env.GIT_COMMIT_SHORT = sh(
                        script: "cd /opt/apps/aguide-api-quarkus && git rev-parse --short HEAD",
                        returnStdout: true
                    ).trim()
                    env.GIT_COMMIT_MSG = sh(
                        script: "cd /opt/apps/aguide-api-quarkus && git log -1 --pretty=%B",
                        returnStdout: true
                    ).trim()
                    env.GIT_AUTHOR = sh(
                        script: "cd /opt/apps/aguide-api-quarkus && git log -1 --pretty=%an",
                        returnStdout: true
                    ).trim()
                }

                echo "📌 Commit: ${env.GIT_COMMIT_SHORT}"
                echo "💬 Mensagem: ${env.GIT_COMMIT_MSG}"
                echo "👨‍💻 Autor: ${env.GIT_AUTHOR}"
            }
        }

        stage('Build Maven') {
            steps {
                echo '🔨 Compilando projeto com Maven (SEM testes)...'
                sh '''
                    cd /opt/apps/aguide-api-quarkus
                    ./mvnw clean package -DskipTests
                '''
            }
        }

        stage('SonarQube Analysis') {
            steps {
                echo '🔍 Executando análise do SonarQube...'
                script {
                    // Configura o Maven tool
                    def mvn = tool 'Default Maven'

                    // Executa a análise do SonarQube
                    withSonarQubeEnv() {
                        sh """
                            cd /opt/apps/aguide-api-quarkus
                            export QUARKUS_DATASOURCE_JDBC_URL=jdbc:postgresql://quarkus_postgres:5432/quarkus_db
                            ${mvn}/bin/mvn clean verify sonar:sonar \
                                -Dsonar.projectKey=aguide-api-quarkus \
                                -Dsonar.projectName='Aguide API Quarkus'
                        """
                    }
                }
                echo '✅ Análise do SonarQube concluída!'
            }
        }

        stage('Verificar Artefatos') {
            steps {
                echo '📋 Verificando artefatos gerados...'
                sh '''
                    cd /opt/apps/aguide-api-quarkus
                    ls -lh target/
                    ls -lh target/quarkus-app/ || echo "Pasta quarkus-app não encontrada"
                '''
            }
        }

        stage('Build Docker Image') {
            steps {
                echo '🐳 Construindo imagem Docker...'
                sh '''
                    cd /opt/apps/aguide-api-quarkus
                    docker compose -f docker-compose.yml build --no-cache
                '''
            }
        }

        stage('Deploy Container') {
            steps {
                echo '🚀 Fazendo deploy do container...'
                echo '⚠️  IMPORTANTE: Flyway migrations serão executadas automaticamente ao iniciar o container'
                sh '''
                    cd /opt/apps/aguide-api-quarkus
                    docker rm -f aguide-api || true
                    docker compose -f docker-compose.yml down --remove-orphans
                    docker compose -f docker-compose.yml up -d
                '''
                echo '⏳ Aguardando inicialização do container (30s)...'
                sleep 30
            }
        }

        stage('Verificar Migrations') {
            steps {
                echo '🔍 Verificando se migrations Flyway foram executadas...'
                sh '''
                    echo "📋 Últimas linhas do log do container:"
                    docker logs aguide-api --tail 50 | grep -i "flyway\|migration" || echo "⚠️  Flyway logs não encontrados (pode estar OK se já executou)"
                '''
            }
        }

        stage('Cleanup Docker') {
            steps {
                echo '🧹 Limpando recursos Docker não utilizados...'
                sh 'docker system prune -f || true'
            }
        }

        stage('Verificar Status') {
            steps {
                echo '✅ Verificando status do container e saúde da aplicação...'
                sh '''
                    echo "🐳 Status do container:"
                    docker ps --filter "name=aguide-api" --format "table {{.Names}}\\t{{.Status}}\\t{{.Ports}}"

                    echo ""
                    echo "🏥 Verificando health check da aplicação (aguardando 10s)..."
                    sleep 10
                    curl -f http://localhost:8083/q/health 2>/dev/null && echo "✅ Aplicação está saudável!" || echo "⚠️  Health check falhou (verifique logs)"

                    echo ""
                    echo "📊 Últimas 20 linhas do log:"
                    docker logs aguide-api --tail 20
                '''
            }
        }
    }

    post {
        success {
            echo '================================================'
            echo '✅ PIPELINE EXECUTADO COM SUCESSO!'
            echo '================================================'
            echo "🎉 Aplicação aguide-api está rodando!"
            echo "🔢 Build #${BUILD_NUMBER} concluído"
            echo "⏱️  Duração: ${currentBuild.durationString}"
            echo "🔗 Console: ${BUILD_URL}console"
            echo '================================================'
        }
        failure {
            echo '================================================'
            echo '❌ PIPELINE FALHOU!'
            echo '================================================'
            echo "🔢 Build #${BUILD_NUMBER} com erro"
            echo "📋 Verificar logs: ${BUILD_URL}console"
            echo "🔍 Status: ${currentBuild.result}"
            echo '================================================'
        }
        always {
            script {
                def duration = currentBuild.durationString.replace(' and counting', '')
                echo '================================================'
                echo '📊 RESUMO DA EXECUÇÃO'
                echo '================================================'
                echo "🆔 Build ID: ${BUILD_ID}"
                echo "🔢 Build Number: ${BUILD_NUMBER}"
                echo "📝 Job: ${JOB_NAME}"
                echo "🎯 Status: ${currentBuild.currentResult}"
                echo "⏱️  Duração: ${duration}"
                echo "🌿 Branch: ${env.GIT_BRANCH}"
                echo "📌 Commit: ${env.GIT_COMMIT_SHORT ?: 'N/A'}"
                echo "👨‍💻 Autor: ${env.GIT_AUTHOR ?: 'N/A'}"
                echo "⏰ Finalizado: ${new Date()}"
                echo '================================================'
            }
        }
    }
}
