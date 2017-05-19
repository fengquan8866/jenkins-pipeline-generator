node('Slave_95') {
  stage('Prepare') {
    withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: 'gitlab',
      usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD']]) {
      // ${item.name}
      if (fileExists('${item.fullName}')) {
        dir('./${item.fullName}') {
          sh "git reset --hard master"
          sh 'git pull'
        }
      } else {
        sh "git clone http://'${r'${USERNAME}'}':'${r'${PASSWORD}'}'@${item.gitUrl}"
      }
    }
  }
  stage('mvn test and build') {
    dir('./${item.fullName}') {
      sh 'rm -rf Jenkins'
      sh '/usr/bin/sh gradlew clean build'
    }
  }
  stage('deploy') {
    dir('./${item.fullName}/build/libs/') {
      sh 'scp ${item.name}*.jar ${item.ip}:/data/kdx-iot/lib/${item.name}.jar'
      sh 'ssh ${item.ip} sudo systemctl restart ${item.name}'
    }
  }
}
