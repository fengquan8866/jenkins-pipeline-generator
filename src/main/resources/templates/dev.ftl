node('Slave_95') {
  stage('Prepare') {
    withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: 'gitlab',
      usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD']]) {
      // ${item.name}
      if (fileExists('${item.fullName}')) {
        dir('./${item.fullName}') {
          sh "git checkout master"
          sh "git reset --hard master"
          sh 'git pull'
          sh "git checkout ${item.version}"
        }
      } else {
        sh "git clone http://'${r'${USERNAME}'}':'${r'${PASSWORD}'}'@${item.gitUrl}"
        dir('./${item.fullName}') {
          sh "git checkout ${item.version}"
        }
      }
    }
  }
  stage('mvn test and build') {
    dir('./${item.fullName}') {
      if (fileExists('Jenkins')) {
        sh 'rm -rf Jenkins'
      }
      sh '/usr/bin/sh gradlew clean build'
    }
  }
  stage('deploy') {
    dir('./${item.fullName}/build/libs/') {
      <#if item.configPath??>
        sh 'scp ./src/main/resources/*-*.yml ${item.ip}:${item.configPath}/'
      </#if>
      sh 'scp *${item.name}*.jar ${item.ip}:${item.serverPath}/${item.name}.jar'
      sh 'ssh ${item.ip} sudo systemctl restart ${item.serviceName}'
    }
  }
}
