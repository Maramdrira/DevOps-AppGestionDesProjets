pipeline {
    agent any

    tools {
        jdk 'JDK-17'
        maven 'Maven-3.9'
    }

    environment {
        DOCKER_HUB_CREDS = credentials('dockerhub-creds')
        IMAGE_BACKEND = "driramaram/devops-backend"
        IMAGE_FRONTEND = "driramaram/devops-frontend"
        TAG = "v${BUILD_NUMBER}"
    }

    stages {
        stage('1. Checkout') {
            steps {
                echo 'Getting code from Git...'
                checkout scm
            }
        }

        stage('2. Compile Backend') {
            steps {
                echo 'mvn clean compile'
                dir('backend') {
                    sh 'mvn clean compile'
                }
            }
        }

        stage('3. SonarQube + JaCoCo Analysis+ Quality Gate') {
            steps {
                dir('backend') {
                    echo 'Preparing test data (needed for JaCoCo coverage)...'
                    sh 'mvn test -DskipTests=false || true'

                    echo 'Running SonarQube analysis with JaCoCo coverage...'
                    withSonarQubeEnv('SonarQube') {
                        sh 'mvn org.sonarsource.scanner.maven:sonar-maven-plugin:3.11.0.3922:sonar -Dsonar.projectKey=devops-backend -Dsonar.projectName=DevOps-Backend -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml'
                    }

                    echo 'Waiting for Quality Gate...'
                    timeout(time: 5, unit: 'MINUTES') {
                        waitForQualityGate abortPipeline: false
                    }
                }
            }
        }

        stage('4. Test Backend') {
            steps {
                echo 'mvn test'
                dir('backend') {
                    sh 'mvn test'
                }
            }
            post {
                always {
                    junit 'backend/target/surefire-reports/*.xml'
                }
            }
        }

        stage('5. Package Backend and Build Frontend') {
            steps {
                echo 'mvn package -DskipTests'
                dir('backend') {
                    sh 'mvn package -DskipTests'
                }
                echo 'Building Angular frontend...'
                dir('frontend') {
                    sh 'npm install --legacy-peer-deps'
                    sh 'npm run build -- --configuration production || npm run build'
                }
            }
        }

        stage('6. Build Docker Images') {
            steps {
                echo 'docker build'
                sh "docker build -t ${IMAGE_BACKEND}:${TAG} -t ${IMAGE_BACKEND}:latest ./backend"
                sh "docker build -t ${IMAGE_FRONTEND}:${TAG} -t ${IMAGE_FRONTEND}:latest ./frontend"
            }
        }

        stage('7. Push to Docker Hub') {
            steps {
                echo 'docker push'
                sh 'echo $DOCKER_HUB_CREDS_PSW | docker login -u $DOCKER_HUB_CREDS_USR --password-stdin'
                catchError(buildResult: 'UNSTABLE', stageResult: 'FAILURE') {
                    retry(3) {
                        sh "docker push ${IMAGE_BACKEND}:${TAG}"
                        sh "docker push ${IMAGE_BACKEND}:latest"
                        sh "docker push ${IMAGE_FRONTEND}:${TAG}"
                        sh "docker push ${IMAGE_FRONTEND}:latest"
                    }
                }
            }
        }

        stage('8. Deploy with Docker Compose') {
            steps {
                echo 'docker compose up -d'
                sh 'docker compose down --remove-orphans || true'
                sh 'docker rm -f devops-mysql devops-backend devops-frontend 2>/dev/null || true'
                sh 'docker compose up -d'
            }
        }
    }

    post {
        success {
            echo 'Pipeline succeeded.'
        }
        failure {
            echo 'Pipeline failed.'
        }
        unstable {
            echo 'Pipeline unstable (push may have failed but deploy continued).'
        }
        always {
            cleanWs()
        }
    }
}
