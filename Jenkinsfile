pipeline {
    agent any

    tools {
        maven 'Maven-3.9'
        jdk 'JDK-21'
    }

    environment {
        MYSQL_ROOT_PASSWORD = credentials('mysql-root-password')
        DYNATRACE_API_TOKEN = credentials('dynatrace-api-token')
        DYNATRACE_URI       = credentials('dynatrace-uri')
        DOCKER_REGISTRY     = 'pram821'
        APP_NAME            = 'hello-springboot'
    }

    options {
        timestamps()
        timeout(time: 30, unit: 'MINUTES')
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '10'))
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build') {
            steps {
                sh 'mvn clean install -DskipTests -q -pl !functional-tests'
            }
        }

        stage('Unit Tests') {
            steps {
                sh 'mvn test -pl common,persona -am -Dspring.autoconfigure.exclude=org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration'
            }
            post {
                always {
                    junit '**/target/surefire-reports/*.xml'
                }
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('SonarQube') {
                    sh 'mvn sonar:sonar -pl !functional-tests'
                }
            }
        }

        stage('Quality Gate') {
            steps {
                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        stage('Contract Tests') {
            steps {
                sh 'mvn test -pl persona -am -Dtest="*ContractTest*"'
            }
            post {
                always {
                    junit 'persona/target/surefire-reports/*.xml'
                }
                success {
                    archiveArtifacts artifacts: 'persona/target/stubs/**', allowEmptyArchive: true
                }
            }
        }

        stage('Functional Tests') {
            steps {
                sh 'mvn clean test -pl functional-tests -am'
            }
            post {
                always {
                    junit 'functional-tests/target/surefire-reports/*.xml'
                    archiveArtifacts artifacts: 'functional-tests/target/cucumber-reports/**', allowEmptyArchive: true
                }
            }
        }

        stage('Performance Tests') {
            when {
                branch 'main'
            }
            steps {
                sh '''
                    cd persona
                    java -jar target/persona-*.jar \
                        --logging.config= \
                        --spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration &
                    sleep 20
                    curl -f http://localhost:8080/seller-center/ || exit 1
                '''
                sh 'mvn gatling:test -pl persona'
            }
            post {
                always {
                    archiveArtifacts artifacts: 'persona/target/gatling/**', allowEmptyArchive: true
                    sh 'pkill -f "persona-*.jar" || true'
                }
            }
        }

        stage('Docker Build & Push') {
            when {
                branch 'main'
            }
            steps {
                script {
                    def commitHash = sh(script: 'git rev-parse --short HEAD', returnStdout: true).trim()
                    def imageName = "${DOCKER_REGISTRY}/${APP_NAME}"

                    sh """
                        docker build -t ${imageName}:${commitHash} -t ${imageName}:latest .
                        docker push ${imageName}:${commitHash}
                        docker push ${imageName}:latest
                    """
                }
            }
        }

        stage('Deploy to K8s') {
            when {
                branch 'main'
            }
            steps {
                script {
                    def commitHash = sh(script: 'git rev-parse --short HEAD', returnStdout: true).trim()
                    sh """
                        kubectl set image deployment/hello-springboot \
                            hello-springboot=${DOCKER_REGISTRY}/${APP_NAME}:${commitHash}
                        kubectl rollout status deployment/hello-springboot --timeout=120s
                    """
                }
            }
        }
    }

    post {
        success {
            echo 'Pipeline completed successfully!'
        }
        failure {
            echo 'Pipeline failed!'
        }
        always {
            cleanWs()
        }
    }
}
