node('Slave_95') {
  stage('Prepare') {
    withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: 'gitlab',
      usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD']]) {
      // ${name}
      if (fileExists('${fullName}')) {
        dir('./${fullName}') {
          sh "git reset --hard master"
          sh 'git pull'
        }
      } else {
        sh "git clone http://'${r'${USERNAME}'}':'${r'${PASSWORD}'}'@${gitUrl}"
      }
    }
  }
  stage('mvn test and build') {
    dir('./${fullName}') {
      sh 'rm -rf Jenkins'
      sh '/usr/bin/sh gradlew clean build'
    }
  }
  stage('deploy') {
    dir('./${fullName}/build/libs/') {
      sh 'scp ${name}*.jar ${ip}:/data/kdx-iot/lib/${name}.jar'
      sh 'ssh ${ip} sudo systemctl restart ${name}'
    }
  }
}