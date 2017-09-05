String path = "/data/kdx-iot/kdx-script"
node('Slave_95') {
  stage('Prepare') {
    updateProject("kdx-jenkins-script-generator", "git.kdxcloud.com:3000/huangchao/kdx-jenkins-script-generator.git")
  }
  stage('generate script') {
    sh "rm -rf ${path}"
    dir('./kdx-jenkins-script-generator') {
      sh '/usr/bin/sh gradlew clean :bootRun'
    }
  }
  stage('check update commit project') {
    List list = ['kdxcloud-eureka-server', 'git.kdxcloud.com:3000/KDXRD/kdxcloud-eureka-server.git',
                'kdxcloud-config-server', 'git.kdxcloud.com:3000/KDXRD/kdxcloud-config-server.git',
                'kdxcloud-iot-tool', 'git.kdxcloud.com:3000/KDXRD/kdxcloud-iot-tool.git',
                'kdxcloud-iot-user', 'git.kdxcloud.com:3000/KDXRD/kdxcloud-iot-user.git',
                'kdxcloud-iot-basic', 'git.kdxcloud.com:3000/KDXRD/kdxcloud-iot-basic.git',
                'kdxcloud-iot-update', 'git.kdxcloud.com:3000/KDXRD/kdxcloud-iot-update.git',
                'kdxcloud-iot-log', 'git.kdxcloud.com:3000/KDXRD/kdxcloud-iot-log.git',
                'kdxcloud-iot-sequence', 'git.kdxcloud.com:3000/KDXRD/kdxcloud-iot-sequence.git']
    for (int i=0; i<list.size(); i+=2) {
      String key = list[i], val = list[i+1];
      if (fileExists("${path}/${key}")) {
        updateProject(key, val)
        dir("./${key}") {
          sh "cp -rf ${path}/${key}/Jenkins ./"
          withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: 'gitlab',
            usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD']]) {
            sh "git config --global user.name 'huangchao'"
            sh "git config --global user.email 'huangchao@kdxfilm.com'"
            sh 'git add .'
            sh 'git commit -m "update Jenkinsfile"'
            sh 'git push'
          }
        }
      }
    }
  }
}

// 更新工程
def updateProject(projectName, gitUrl) {
  withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: 'gitlab',
    usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD']]) {
    if (fileExists(projectName)) {
      dir("./${projectName}") {
        sh "git reset --hard master"
        sh 'git pull'
      }
    } else {
      sh "git clone http://'${USERNAME}':'${PASSWORD}'@${gitUrl}"
    }
  }
}