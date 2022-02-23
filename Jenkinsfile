// Jenkinsfile (Declarative Pipeline)
pipeline {
    agent any

    stages {
        stage('Build') {
            steps {
                echo ' ==== Build ==== '
                bat 'mvn clean install -DskipTests'
            }
        }
    }

}