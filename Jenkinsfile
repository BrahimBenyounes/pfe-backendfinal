pipeline {
    agent any

    tools {
        maven 'M3'
        jdk 'jdk17'
    }

    environment {
        DOCKER_IMAGE_VERSION = "v1.0.${BUILD_NUMBER}"
        DOCKER_COMPOSE_FILE = 'docker-compose.yml'
        DOCKER_HUB_USERNAME = 'brahim20255'
        DOCKER_HUB_PASSWORD = 'Lifeisgoodbrahim@@'  // ⚠️ من الأفضل استخدام credentials بدلًا من تخزين الباسورد هنا
        MAVEN_HOME = tool name: 'M3', type: 'maven'
        JAVA_HOME = tool name: 'jdk17', type: 'jdk'
        PATH = "${MAVEN_HOME}/bin:${JAVA_HOME}/bin:${env.PATH}"
        SONAR_HOST_URL = 'http://localhost:9000'
        SONAR_TOKEN = 'squ_e8b5f30571241cb357ad5734ed99e18e1807aa81'
        BUILD_TAG = "1.0.${env.BUILD_NUMBER}"
    }

    stages {

        stage('Checkout Code') {
            steps {
                checkout scm
            }
        }

        stage('Build All Maven Projects') {
            steps {
                script {
                    def services = [
                        "discovery-service", "gateway-service", "product-service",
                        "formation-service", "order-service", "notification-service",
                        "login-service", "contact-service"
                    ]
                    services.each { service ->
                        dir(service) {
                            bat 'mvn clean install -DskipTests'
                        }
                    }
                }
            }
        }

        stage('SonarQube Analysis') {
            steps {
                script {
                    def services = [
                        "discovery-service", "gateway-service", "product-service",
                        "formation-service", "order-service", "notification-service",
                        "login-service", "contact-service"
                    ]
                    services.each { service ->
                        def timestamp = new Date().format("yyyyMMdd-HHmmss", TimeZone.getTimeZone('UTC'))
                        def projectKey = "${service}-${timestamp}"
                        dir(service) {
                            bat 'mvn clean org.jacoco:jacoco-maven-plugin:prepare-agent install -Dmaven.test.failure.ignore=true'
                            bat "mvn sonar:sonar -Dsonar.token=${env.SONAR_TOKEN} -Dsonar.projectKey=${projectKey} -Dsonar.host.url=${SONAR_HOST_URL}"
                        }
                    }
                }
            }
        }

        stage('Upload Artifacts to Nexus') {
            steps {
                script {
                    def version = "1.0-SNAPSHOT"
                    def projects = [
                        "discovery-service", "gateway-service", "product-service",
                        "formation-service", "order-service", "notification-service",
                        "login-service", "contact-service"
                    ]
                    projects.each { project ->
                        dir(project) {
                            nexusArtifactUploader(
                                nexusVersion: 'nexus3',
                                protocol: 'http',
                                nexusUrl: 'localhost:8081',
                                groupId: 'org.pfe',
                                version: version,
                                repository: 'maven-snapshots',
                                credentialsId: 'nexus-credentials',
                                artifacts: [[
                                    artifactId: project,
                                    classifier: '',
                                    file: "target/${project}-${version}.jar",
                                    type: 'jar'
                                ]]
                            )
                        }
                    }
                }
            }
        }

        stage('Build Docker Images') {
            steps {
                script {
                    def services = [
                        "discovery-service", "gateway-service", "product-service",
                        "formation-service", "order-service", "notification-service",
                        "login-service", "contact-service"
                    ]
                    services.each { serviceName ->
                        dir(serviceName) {
                            bat "docker build -t ${serviceName}:${DOCKER_IMAGE_VERSION} ."
                        }
                    }
                }
            }
        }

        stage('Deploy Microservices') {
            steps {
                script {
                    bat "docker compose -f ${DOCKER_COMPOSE_FILE} down"
                    bat "docker compose -f ${DOCKER_COMPOSE_FILE} up -d"
                }
            }
        }

        stage('Push Docker Images to Docker Hub') {
            steps {
                script {
                    withCredentials([usernamePassword(credentialsId: 'dockerhub-credentials', passwordVariable: 'DOCKER_HUB_PASSWORD', usernameVariable: 'DOCKER_HUB_USERNAME')]) {
                        bat "echo ${DOCKER_HUB_PASSWORD} | docker login -u ${DOCKER_HUB_USERNAME} --password-stdin"

                        def services = [
                            "discovery-service", "gateway-service", "product-service",
                            "formation-service", "order-service", "notification-service",
                            "login-service", "contact-service"
                        ]

                        services.each { service ->
                            def localTag = "${service}:${DOCKER_IMAGE_VERSION}"
                            def remoteTag = "${DOCKER_HUB_USERNAME}/${service}:${DOCKER_IMAGE_VERSION}"
                            def remoteLatest = "${DOCKER_HUB_USERNAME}/${service}:latest"

                            bat "docker tag ${localTag} ${remoteTag}"
                            bat "docker tag ${localTag} ${remoteLatest}"

                            catchError(buildResult: 'FAILURE', stageResult: 'FAILURE') {
                                retry(3) {
                                    bat "docker push ${remoteTag}"
                                    bat "docker push ${remoteLatest}"
                                }
                            }
                        }
                    }
                }
            }
        }

        stage('Deploy to Kubernetes') {
            steps {
                script {
                    try {
                        withKubeConfig([credentialsId: 'mykubeconfig', serverUrl: 'https://127.0.0.1:64781']) {
                            bat 'kubectl apply -f Kubernetes --validate=false'
                        }
                    } catch (Exception e) {
                        error "Kubernetes deployment failed: ${e.getMessage()}"
                    }
                }
            }
        }

    } 


}
