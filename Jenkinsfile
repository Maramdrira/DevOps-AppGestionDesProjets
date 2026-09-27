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
        stage('Checkout') {
            steps {
                echo 'Checking out source code...'
                checkout scm
            }
        }

        stage('Build Backend') {
            steps {
                echo 'Compiling backend...'
                dir('backend') {
                    sh 'mvn clean compile'
                }
            }
        }

        stage('Test Backend') {
            steps {
                echo 'Running unit tests...'
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

        stage('SonarQube Analysis') {
            steps {
                echo 'Analyzing with SonarQube...'
                dir('backend') {
                    withSonarQubeEnv('SonarQube') {
sh 'mvn org.sonarsource.scanner.maven:sonar-maven-plugin:3.11.0.3922:sonar -Dsonar.projectKey=devops-backend -Dsonar.projectName=DevOps-Backend'                    }
                }
            }
        }

        stage('Quality Gate') {
            steps {
                echo 'Waiting for Quality Gate...'
                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: false
                }
            }
        }

        stage('Package Backend') {
            steps {
                echo 'Packaging JAR...'
                dir('backend') {
                    sh 'mvn package -DskipTests'
                }
            }
        }

        stage('Build Frontend') {
            steps {
                echo 'Building Angular frontend...'
                dir('frontend') {
                    sh 'npm install --legacy-peer-deps'
                    sh 'npm run build -- --configuration production || npm run build'
                }
            }
        }

        stage('Build Docker Images') {
            steps {
                echo 'Building Docker images...'
                sh "docker build -t ${IMAGE_BACKEND}:${TAG} -t ${IMAGE_BACKEND}:latest ./backend"
                sh "docker build -t ${IMAGE_FRONTEND}:${TAG} -t ${IMAGE_FRONTEND}:latest ./frontend"
            }
        }

        stage('Push to Docker Hub') {
            steps {
                echo 'Pushing images...'
                sh 'echo $DOCKER_HUB_CREDS_PSW | docker login -u $DOCKER_HUB_CREDS_USR --password-stdin'
                sh "docker push ${IMAGE_BACKEND}:${TAG}"
                sh "docker push ${IMAGE_BACKEND}:latest"
                sh "docker push ${IMAGE_FRONTEND}:${TAG}"
                sh "docker push ${IMAGE_FRONTEND}:latest"
            }
        }

        stage('Deploy with Docker Compose') {
            steps {
                echo 'Deploying stack...'
                sh 'docker compose down || true'
                sh 'docker compose up -d'
            }
        }
    }

    post {
        success {
            echo 'Pipeline succeeded!'
        }
        failure {
            echo 'Pipeline failed.'
        }
        always {
            cleanWs()
        }
    }
}
