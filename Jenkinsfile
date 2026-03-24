pipeline {
    agent any

    tools {
        maven 'maven'
        jdk 'jdk-17'

    }

    environment {
        DOCKER_USER    = credentials('docker-hub-credentials')

        K8S_DEPLOYMENT = 'k8s/deployment.yml'

        DOCKER_CREDS   = 'docker-hub-credentials'
        GIT_CREDS      = 'github-credentials'

        GITHUB_USER    = 'SammyOcharo'

        GITHUB_REPO    = 'gitops'
    }

    triggers {
         githubPush()
         pollSCM('H 6 * * *')
    }

    options {
        buildDiscarder(logRotator(numToKeepStr: '20'))
        timestamps()
        timeout(time: 30, unit: 'MINUTES')
        disableConcurrentBuilds()
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
                sh 'git log --oneline -5'
            }
        }

        stage('Set image tag') {
            steps {
                script {
                    // Build IMAGE_NAME here, after credentials are resolved
                    withCredentials([usernamePassword(
                        credentialsId: 'docker-hub-credentials',
                        usernameVariable: 'DOCKER_USERNAME',
                        passwordVariable: 'DOCKER_PASSWORD'
                    )]) {
                        env.IMAGE_NAME = env.DOCKER_USERNAME + '/gitops-app'
                    }

                    def shortSha = sh(
                        script: 'git rev-parse --short HEAD',
                        returnStdout: true
                    ).trim()

                    if (env.BRANCH_NAME == 'main') {
                        def pomVersion = sh(
                            script: "mvn help:evaluate -Dexpression=project.version -q -DforceStdout",
                            returnStdout: true
                        ).trim()
                        env.IMAGE_TAG = "v${pomVersion}"
                    } else {
                        def safeBranch = env.BRANCH_NAME.replaceAll('/', '-')
                        env.IMAGE_TAG = "${safeBranch}-${shortSha}"
                    }

                    echo "IMAGE : ${env.IMAGE_NAME}:${env.IMAGE_TAG}"
                    echo "BRANCH: ${env.BRANCH_NAME}"
                }
            }
        }

        stage('Build & test') {
            steps {
                sh '''
                    mvn clean verify \
                        -Dspring.profiles.active=test \
                        -Dmaven.test.failure.ignore=false \
                        --batch-mode \
                        --no-transfer-progress
                '''
            }
            post {
                always {
                    // Publish JUnit test results in the Jenkins UI
                    // (shows pass/fail counts, individual test details)
                    junit allowEmptyResults: true,
                          testResults: '**/target/surefire-reports/*.xml'
                }
                failure {
                    echo "Tests failed! Check the JUnit report above."
                }
            }
        }

        stage('Build Docker image') {
            when {
                anyOf {
                    branch 'main'
                    branch 'develop'
                }
            }
            steps {
                script {
                    docker.build(env.IMAGE_NAME + ':' + env.IMAGE_TAG, '--pull .')
                    echo 'Docker image built: ' + env.IMAGE_NAME + ':' + env.IMAGE_TAG
                }
            }
        }

        stage('Push image') {
            when {
                anyOf {
                    branch 'main'
                    branch 'develop'
                }
            }
            steps {
                script {
                    docker.withRegistry('https://index.docker.io/v1/', DOCKER_CREDS) {

                        // Push the versioned tag (always)
                        docker.image(env.IMAGE_NAME + ':' + env.IMAGE_TAG).push()

                        echo 'Pushed: ' + env.IMAGE_NAME + ':' + env.IMAGE_TAG

                        // On main: also push 'latest' so docker pull gitops-app always gets current
                        if (env.BRANCH_NAME == 'main') {
                            docker.image(env.IMAGE_NAME + ':' + env.IMAGE_TAG).push('latest')
                            echo 'Pushed: ' + env.IMAGE_NAME + ':latest'
                        }
                    }
                }
            }
        }

        stage('Update K8s manifest') {
            when {
                anyOf {
                    branch 'main'
                    branch 'develop'
                }
            }
            steps {
                script {
                    withCredentials([usernamePassword(
                        credentialsId: GIT_CREDS,
                        usernameVariable: 'GIT_USER',
                        passwordVariable: 'GIT_TOKEN'
                    )]) {
                        sh """
                            # Set git identity for the commit
                            git config user.email "jenkins@ci.local"
                            git config user.name  "Jenkins CI"

                            # Replace the image tag line in deployment.yml
                            # Before: image: your-user/gitops-app:develop-abc1234
                            # After:  image: your-user/gitops-app:v1.0.0
                            sed -i "s|image: ${env.IMAGE_NAME}:.*|image: ${env.IMAGE_NAME}:${env.IMAGE_TAG}|g" ${K8S_DEPLOYMENT}


                            # Verify the substitution worked — this will print in the build log
                            echo "Updated image tag in manifest:"
                            grep 'image:' ${K8S_DEPLOYMENT}

                            # Stage and commit the change
                            git add ${K8S_DEPLOYMENT}
                            git commit -m "ci: update image tag to ${env.IMAGE_TAG} [skip ci]"

                            # Push via HTTPS using the GitHub PAT
                            # GIT_TOKEN is the Personal Access Token from Jenkins credentials
                            git push https://\${GIT_USER}:\${GIT_TOKEN}@github.com/${GITHUB_USER}/${GITHUB_REPO}.git \\
                                HEAD:${env.BRANCH_NAME}
                        """
                    }
                }
            }
        }

         stage('Smoke test') {
            when { branch 'main' }
            steps {
                script {
                    def minikubeIp = sh(
                        script: 'minikube ip',
                        returnStdout: true
                    ).trim()

                    echo "Waiting 60s for FluxCD to sync and pod to start..."
                    sleep(60)

                    sh """
                        curl --retry 5 \
                             --retry-delay 10 \
                             --retry-connrefused \
                             --fail \
                             --silent \
                             http://${minikubeIp}:30080/actuator/health | \
                        grep -q '"status":"UP"'
                        echo "Health check passed — app is UP in Kubernetes!"
                    """
                }
            }
        }

    }

    post {
        success {
            echo '✓ Pipeline succeeded — ' + env.IMAGE_NAME + ':' + env.IMAGE_TAG + ' is live.'
        }

        failure {
            // Remove the local Docker image to free disk space after a failed build
            sh 'docker rmi ' + env.IMAGE_NAME + ':' + env.IMAGE_TAG + ' || true'
            echo "✗ Pipeline failed. Image removed from local Docker."
        }

        always {
            // Remove dangling intermediate layers from docker build cache
            sh 'docker image prune -f || true'

            // Wipe the Jenkins workspace so the next build starts clean
            // (prevents stale files from previous builds interfering)
            cleanWs()
        }
    }
}
