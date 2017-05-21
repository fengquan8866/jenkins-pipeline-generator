node('Slave_95') {
  stage('Prepare') {
    updateProject("kdx-jenkins-script-generator", "git.kdxcloud.com:3000/huangchao/kdx-jenkins-script-generator.git")
  }
  stage('generate script') {
    dir('./kdx-jenkins-script-generator') {
      sh '/usr/bin/sh gradlew clean :bootRun'
    }
  }
  stage('check update commit project') {
    def map = ['kdxcloud-eureka-server': 'git.kdxcloud.com:3000/KDXRD/kdxcloud-eureka-server.git',
               'kdxcloud-config-server': 'git.kdxcloud.com:3000/KDXRD/kdxcloud-config-server.git',
               'kdxcloud-iot-tool': 'git.kdxcloud.com:3000/KDXRD/kdxcloud-iot-tool.git',
               'kdxcloud-iot-user': 'git.kdxcloud.com:3000/KDXRD/kdxcloud-iot-user.git',
               'kdxcloud-iot-basic': 'git.kdxcloud.com:3000/KDXRD/kdxcloud-iot-basic.git',
               'kdxcloud-iot-update': 'git.kdxcloud.com:3000/KDXRD/kdxcloud-iot-update.git',
               'kdxcloud-iot-log': 'git.kdxcloud.com:3000/KDXRD/kdxcloud-iot-log.git',
               'kdxcloud-iot-sequence': 'git.kdxcloud.com:3000/KDXRD/kdxcloud-iot-sequence.git']
    for (e in map) {
      updateProject(e.key, e.value)
      dir('./${e.key}') {
        if (!fileExists('Jenkins')) {
          sh 'mkdir Jenkins'
        }
        sh 'cp ~/kdx-script/${e.key}/Jenkins/* ./Jenkins/'
        sh 'git add .'
        sh 'git commit -m 'update Jenkinsfile'
        sh 'git push'
      }
    }
  }
  
  def updateProject(projectName, gitUrl) {
    withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: 'gitlab',
      usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD']]) {
      if (fileExists(projectName)) {
        dir('./${projectName}') {
          sh "git reset --hard master"
          sh 'git pull'
        }
      } else {
        sh "git clone http://'${USERNAME}':'${PASSWORD}'@${gitUrl}"
      }
    }
  }
}