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
      sh 'tar -zcvf ${item.name}-${item.version}.tar.gz <#if item.excludes??><#list item.excludes as ii,e>--exclude=${e} </#list></#if>./'
    }
  }
  stage('deploy') {
    dir('./${item.fullName}') {
      sh 'ssh ${item.ip} sudo rm -rf ${item.serverPath}'
      sh 'scp ${item.name}-${item.version}.tar.gz ${item.ip}:~/'
      sh 'ssh ${item.ip} sudo tar -zxvf ~/${item.name}-${item.version}.tar.gz -C ${item.serverPath}'
    }
  }
}
