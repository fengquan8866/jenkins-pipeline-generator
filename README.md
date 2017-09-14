# jenkins-pipeline-generator

* 半自动化部署，Jenkins脚本生成器
* 可自定义配置部署哪（几）个工程
* 可自定义配置部署哪些环境
* springboot工程
* gradle构建

### 待处理：

* 同一个微服务分布式部署
* 配置文件env.yml，不同系统，分不同文件配置，如env-iot.yml、env-mps.yml

### 1.env.yml 配置：

```
    # 脚本生成路径
    rootPath: /data/mps/script

    # 启用的环境
    enableEnv:
      - dev
      - test
      - aliyun

    # 前端项目
    frontProject:
      - MPS-FE

    # 启用的工程
    enableProject:
      - mps-device
      - MPS-FE

    # 单独启动的工程
    singles:
      - eureka-server
      - config-server

    # 配置顺序：project config > current env default config > default config [ > dev project config > dev default config ]
    env:
      # 默认配置（default config）
      default:
        # 服务器IP
        # ip: 
        # 系统名称
        systemName: hc-iot
        # 工程名称
        name: ${projectName}
        # 工程全名称
        fullName: hccloud-${projectName}
        # 工程源码对应的 git url
        gitUrl: git.hccloud.com:3000/hc/${fullName}.git
        # 服务名称（centos上的服务名称）
        serviceName: ${projectName}${(envName=='dev')?string("", "-"+envName)}
        # 部署路径（jar包部署到服务器上的位置）
        serverPath: /data/${systemName}${(envName=='dev')?string("", "-"+envName)}/lib
        # 版本
        version: 2.6.0
        # 配置文件部署位置，config-server所需
        # configPath: 
        # 前端项目需要，压缩时去除部分文件（夹）
        excludes:
          - .git
          - node_modules
          - .idea
          - .vscode
          - "*.gz"
          - "*.zip"
      # 开发环境
      dev:
        # 开发环境默认配置（current env default config）
        # default: ...
        eureka-server:
          name: eureka-server
          fullName: hccloud-eureka-server
          ip: 192.168.79.83
          serviceName: eureka-server
          serverPath: /data/${systemName}/lib
        config-server:
          ip: 192.168.79.83
          configPath: /data/${systemName}/conf
        iot-tool:
          ip: 192.168.79.84
        iot-user:
          ip: 192.168.79.84

      test:
        # 测试环境默认配置（current env default config）
        # default: ...
        eureka-server:
          ip: 192.168.79.88
          serviceName: eureka-server
        config-server:
          ip: 192.168.79.88
          serviceName: config-server

        mps-user:
          serverPath: /data/${systemName}/lib_test
        MPS-FE:
          ip: 192.168.79.81
          serverPath: /data/${systemName}/html
```

* 配置解析（环境配置、project配置，均为以下）：

```
# 服务器IP
ip: 
# 系统名称
systemName: 
# 工程名称
name: ${projectName}
# 工程全名称
fullName: hccloud-${projectName}
# 工程源码对应的 git url
gitUrl: git.hccloud.com:3000/hc/${fullName}.git
# 服务名称（centos上的服务名称）
serviceName: ${projectName}${(envName=='dev')?string("", "-"+envName)}
# 部署路径（jar包部署到服务器上的位置）
serverPath: /data/${systemName}${(envName=='dev')?string("", "-"+envName)}/lib
# 版本
version: 2.6.0
# 配置文件部署位置，config-server所需
configPath: 
# 前端项目需要，压缩时去除部分文件（夹）
excludes:
```

### 2.部署人员准备步骤：

```
1.本地安装jdk
2.本地安装gradle
3.本地下载工程：http://git.kdxcloud.com:3000/huangchao/kdx-jenkins-script-generator.git
4.服务器上需提前创建好相应的文件夹
5.服务器上添加服务，测试非开发环境，需指定环境，如：
    --spring.profiles.active=test
```

### 3.部署人员执行步骤：

```
1.配置 env.yml
2.终端 cd 到文件夹中，执行：gradle bootRun
3.复制生成的Jenkins脚本，到 Jenkins web管理端执行
（注：1.Jenkins上执行成功，不代表服务已经启动成功，需查看各服务启动日志。
     2.如日志有错误，找相应开发人员解决）
```

### 4.开发人员注意：

* 1.每个工程开发、开发测试OK后，需打相应的tag，以便部署人员，get到正确的代码
* 2.不同环境，配置好不同日志文件

```
1.依赖：compile ("org.springframework.boot:spring-boot-starter-log4j2")
2.yml配置：
    logging:
      config: classpath:log4j2-test.xml
```

* 3.配置好不同环境端口

### 5.常见错误

```
1.Permission denied (publickey,gssapi-keyex,gssapi-with-mic,password).
原因：服务器之间未做信任链，通过下面命令生成
    ssh-copy-id 192.168.79.81

2.tar: .: file changed as we read it
当再次执行时，未出现该错误
```



