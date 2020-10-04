  def username = '' 
  def repositoryName = "${JOB_NAME}"
  def environment = ""
  def gitCommitHash = ""
  def registry = "${username}/${repositoryName}"

  def branch = "${scm.branches[0].name}".replaceAll(/^\*\//, '')
  if (branch =~ '^v[0-9].[0-9]' || branch =~ '^v[0-9][0-9].[0-9]' ) {
        // if Application release or branch starts with v* example v0.1 will be deployed to prod
        environment = 'prod' 
        repositoryName = repositoryName + '-prod'

  } else if (branch.contains('dev-feature')) {
        // if branch name contains dev-feature then the deploy will be deployed to dev environment 
        environment = 'dev' 
        repositoryName = repositoryName + '-dev-feature'

  } else if (branch.contains('qa-feature')) {
        // if branch name contains q-feature then the deploy will be deployed to qa environment
        repositoryName = repositoryName + '-qa-feature'
        environment = 'qa' 

  } else if (branch.contains('PR')) {
        // PR means Pull requests all PR will be deployed to test namespace 
        repositoryName = repositoryName + '-pr-feature'
        environment = 'test' 

  } else if (branch == 'master') {
        // If branch is master it will be deployed to stage environment 
        environment = 'stage' 
        repositoryName = repositoryName + '-stage'
  }

def k8slabel = "jenkins-pipeline-${UUID.randomUUID().toString()}"
def slavePodTemplate = """
      metadata:
        labels:
          k8s-label: ${k8slabel}
        annotations:
          jenkinsjoblabel: ${env.JOB_NAME}-${env.BUILD_NUMBER}
      spec:
        affinity:
          podAntiAffinity:
            requiredDuringSchedulingIgnoredDuringExecution:
            - labelSelector:
                matchExpressions:
                - key: component
                  operator: In
                  values:
                  - jenkins-jenkins-master
              topologyKey: "kubernetes.io/hostname"
        containers:
        - name: docker
          image: docker:latest
          imagePullPolicy: IfNotPresent
          command:
          - cat
          tty: true
          volumeMounts:
            - mountPath: /var/run/docker.sock
              name: docker-sock
        serviceAccountName: default
        securityContext:
          runAsUser: 0
          fsGroup: 0
        volumes:
          - name: docker-sock
            hostPath:
              path: /var/run/docker.sock
    """

  properties([
          parameters([
                  booleanParam(defaultValue: false,description: 'Click this if you would like to deploy to latest',name: 'PUSH_LATEST'
          )])
  ])

    podTemplate(name: k8slabel, label: k8slabel, yaml: slavePodTemplate, showRawYaml: false) {
      node(k8slabel) {
        stage('Pull SCM') {
            git 'https://github.com/tuyalou/buildtools.git'
            gitCommitHash = sh(returnStdout: true, script: 'git rev-parse --short HEAD').trim()
        }
        dir('Docker/') {
          stage("Docker Build") {
              container("docker") {
                  dockerImage = docker.build(repositoryName, "--build-arg environment=${environment.toLowerCase()} .")
              }
          }
          stage("Docker Login") {
              withCredentials([string(credentialsId: 'dockerhub-token', variable: 'username')]) {
                  container("docker") {
                      withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: "docker-hub-creds", usernameVariable: 'docker_username', passwordVariable: 'docker_password']]) {
                        docker login --username ${env.docker_username} --password ${env.docker_password}
                  }
              }
          }
          stage("Docker Push") {
              container("docker") {
                  docker.withRegistry(registry, 'docker-hub-creds') {
                          dockerImage.push("${gitCommitHash}")
                          if (params.PUSH_LATEST) {
                                  dockerImage.push("latest")
                          }
              }
          }
        }
      }
    }
   }
