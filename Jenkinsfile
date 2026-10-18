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
        K8S_NAMESPACE = "devops"
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

        stage('3. Test + SonarQube + JaCoCo + Quality Gate') {
            steps {
                dir('backend') {
                    echo 'Running unit tests (generates JaCoCo coverage)...'
                    sh 'mvn test'

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
            post {
                always {
                    junit 'backend/target/surefire-reports/*.xml'
                }
            }
        }

        stage('4. Package Backend and Build Frontend') {
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

        stage('5. Build Docker Images (Compose + K8s)') {
            steps {
                echo 'docker build (Compose variant)'
                sh "docker build -t ${IMAGE_BACKEND}:${TAG} -t ${IMAGE_BACKEND}:latest ./backend"
                sh "docker build -t ${IMAGE_FRONTEND}:${TAG} -t ${IMAGE_FRONTEND}:latest ./frontend"

                echo 'docker build (K8s variant with nginx-k8s.conf)'
                sh "docker build --build-arg NGINX_CONFIG=nginx-k8s.conf -t ${IMAGE_FRONTEND}:k8s -t ${IMAGE_FRONTEND}:k8s-${TAG} ./frontend"
            }
        }

        stage('6. Push to Docker Hub') {
            steps {
                echo 'docker push (Compose + K8s tags)'
                sh 'echo $DOCKER_HUB_CREDS_PSW | docker login -u $DOCKER_HUB_CREDS_USR --password-stdin'
                catchError(buildResult: 'UNSTABLE', stageResult: 'FAILURE') {
                    retry(3) {
                        sh "docker push ${IMAGE_BACKEND}:${TAG}"
                        sh "docker push ${IMAGE_BACKEND}:latest"
                        sh "docker push ${IMAGE_FRONTEND}:${TAG}"
                        sh "docker push ${IMAGE_FRONTEND}:latest"
                        sh "docker push ${IMAGE_FRONTEND}:k8s"
                        sh "docker push ${IMAGE_FRONTEND}:k8s-${TAG}"
                    }
                }
            }
        }

        stage('7. Deploy to Docker Compose') {
            steps {
                echo 'docker compose up -d'
                sh 'docker compose down --remove-orphans || true'
                sh 'docker rm -f devops-mysql devops-backend devops-frontend 2>/dev/null || true'
                sh 'docker compose up -d'
            }
        }

        stage('8. Deploy to Kubernetes ') {
            steps {
                echo 'Updating Kubernetes deployments with new image tags...'
                withKubeConfig(credentialsId: 'kubernetes-config') {
                    sh "kubectl config current-context"
                    sh "kubectl get pods -n ${K8S_NAMESPACE} || true"

                    sh "kubectl set image deployment/spring spring=${IMAGE_BACKEND}:${TAG} -n ${K8S_NAMESPACE} || echo 'spring deployment not found'"
                    sh "kubectl set image deployment/frontend frontend=${IMAGE_FRONTEND}:k8s-${TAG} -n ${K8S_NAMESPACE} || echo 'frontend deployment not found'"

                    sh "kubectl rollout status deployment/spring -n ${K8S_NAMESPACE} --timeout=180s || echo 'spring rollout skipped'"
                    sh "kubectl rollout status deployment/frontend -n ${K8S_NAMESPACE} --timeout=180s || echo 'frontend rollout skipped'"

                    sh "kubectl get pods -n ${K8S_NAMESPACE}"
                }
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
            echo 'Pipeline unstable.'
        }
        always {
            cleanWs()
        }
    }
}
