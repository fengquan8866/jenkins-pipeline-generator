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
      sh 'ssh ${item.ip} rm -rf ${item.serverPath}'
      sh 'scp ${item.name}-${item.version}.tar.gz ${item.ip}:${parent_dir(item.serverPath)}'
      sh 'ssh ${item.ip} mkdir ${item.serverPath}'
      sh 'ssh ${item.ip} tar -zxvf ${parent_dir(item.serverPath)}${item.name}-${item.version}.tar.gz -C ${item.serverPath}'
    }
  }
}
