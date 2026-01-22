pipeline {
    agent any
    
    environment {
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
                    echo "🌿 Branch Git: ${env.GIT_BRANCH ?: 'N/A'}"
                    echo "📂 Workspace: ${WORKSPACE}"
                    echo "🏠 Jenkins Home: ${JENKINS_HOME}"
                    echo "🖥️  Node Name: ${NODE_NAME}"
                    echo "⏰ Timestamp: ${new Date()}"
                    echo '================================================'
                }
            }
        }
        
        stage('Build Maven') {
            steps {
                echo '🔨 Compilando projeto com Maven (SEM testes)...'
                sh './mvnw clean package -DskipTests'
            }
        }
        
        stage('SonarQube Analysis') {
            steps {
                echo '🔍 Executando análise do SonarQube...'
                script {
                    // Configura o Maven tool (certifique-se que 'Default Maven' está configurado no Jenkins)
                    def mvn = tool 'Default Maven'
                    
                    // Executa a análise do SonarQube
                    withSonarQubeEnv() {
                        sh """
                            ${mvn}/bin/mvn clean verify sonar:sonar \
                                -DskipTests \
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
                    ls -lh target/
                    ls -lh target/quarkus-app/ || echo "Pasta quarkus-app não encontrada"
                '''
            }
        }
        
        stage('Build Docker Image') {
            steps {
                echo '🐳 Construindo imagem Docker...'
                sh 'docker compose -f docker-compose.yml build --no-cache'
            }
        }
        
        stage('Deploy Container') {
            steps {
                echo '🚀 Fazendo deploy do container...'
                sh '''
                    docker compose -f docker-compose.yml down
                    docker compose -f docker-compose.yml up -d
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
                echo '✅ Verificando status do container...'
                sh 'docker ps --filter "name=aguide-api" --format "table {{.Names}}\\t{{.Status}}\\t{{.Ports}}"'
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
                echo "🌿 Branch: ${env.GIT_BRANCH ?: 'N/A'}"
                echo "📌 Commit: ${env.GIT_COMMIT?.take(7) ?: 'N/A'}"
                echo "👨‍💻 Autor: N/A"
                echo "⏰ Finalizado: ${new Date()}"
                echo '================================================'
            }
        }
    }
}