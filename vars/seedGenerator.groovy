import jenkins.model.*
import javaposse.jobdsl.dsl.DslScriptLoader
import javaposse.jobdsl.plugin.JenkinsJobManagement

def generateSeedJob(jobName, gitSshUrl, gitRepoUrl, gitBranch, gitCredId, targetSpecifier) {
  // Template Variables:
  //    GIT_SSH_URL       : The SSH URL for the seed job repo.
  //    GIT_REPO_URL      : The HTTP/HTTPS URL for the seed job repo.
  //    GIT_BRANCH        : The branch to checkout.
  //    GIT_CRED_ID       : The credential ID to use to pull the job repo.
  //    TARGET_SPECIFIER  : The target specifier to find the job DSL scripts.
  def template = '''<?xml version="1.1" encoding="UTF-8" standalone="no"?>
  <project>
    <description/>
    <keepDependencies>false</keepDependencies>
    <properties>
      <jenkins.model.BuildDiscarderProperty>
        <strategy class="hudson.tasks.LogRotator">
          <daysToKeep>-1</daysToKeep>
          <numToKeep>25</numToKeep>
          <artifactDaysToKeep>-1</artifactDaysToKeep>
          <artifactNumToKeep>-1</artifactNumToKeep>
        </strategy>
      </jenkins.model.BuildDiscarderProperty>
      <com.sonyericsson.rebuild.RebuildSettings plugin="rebuild@1.31">
        <autoRebuild>false</autoRebuild>
        <rebuildDisabled>false</rebuildDisabled>
      </com.sonyericsson.rebuild.RebuildSettings>
      <com.synopsys.arc.jenkinsci.plugins.jobrestrictions.jobs.JobRestrictionProperty plugin="job-restrictions@0.8"/>
      <hudson.plugins.throttleconcurrents.ThrottleJobProperty plugin="throttle-concurrents@2.1">
        <categories class="java.util.concurrent.CopyOnWriteArrayList"/>
        <throttleEnabled>false</throttleEnabled>
        <throttleOption>project</throttleOption>
        <limitOneJobWithMatchingParams>false</limitOneJobWithMatchingParams>
        <paramsToUseForLimit/>
      </hudson.plugins.throttleconcurrents.ThrottleJobProperty>
    </properties>
    <scm class="hudson.plugins.git.GitSCM" plugin="git@4.5.2">
      <configVersion>2</configVersion>
      <userRemoteConfigs>
        <hudson.plugins.git.UserRemoteConfig>
          <url>GIT_SSH_URL</url>
          <credentialsId>GIT_CRED_ID</credentialsId>
        </hudson.plugins.git.UserRemoteConfig>
      </userRemoteConfigs>
      <branches>
        <hudson.plugins.git.BranchSpec>
          <name>*/GIT_BRANCH</name>
        </hudson.plugins.git.BranchSpec>
      </branches>
      <doGenerateSubmoduleConfigurations>false</doGenerateSubmoduleConfigurations>
      <browser class="hudson.plugins.git.browser.GithubWeb">
        <url>GIT_HTTP_URL</url>
      </browser>
      <submoduleCfg class="list"/>
      <extensions/>
    </scm>
    <canRoam>true</canRoam>
    <disabled>false</disabled>
    <blockBuildWhenDownstreamBuilding>false</blockBuildWhenDownstreamBuilding>
    <blockBuildWhenUpstreamBuilding>false</blockBuildWhenUpstreamBuilding>
    <triggers>
      <hudson.triggers.SCMTrigger>
        <spec>H/15 * * * *</spec>
        <ignorePostCommitHooks>false</ignorePostCommitHooks>
      </hudson.triggers.SCMTrigger>
    </triggers>
    <concurrentBuild>false</concurrentBuild>
    <builders>
      <javaposse.jobdsl.plugin.ExecuteDslScripts plugin="job-dsl@1.77">
        <targets>TARGET_SPECIFIER</targets>
        <usingScriptText>false</usingScriptText>
        <sandbox>false</sandbox>
        <ignoreExisting>false</ignoreExisting>
        <ignoreMissingFiles>false</ignoreMissingFiles>
        <failOnMissingPlugin>false</failOnMissingPlugin>
        <failOnSeedCollision>false</failOnSeedCollision>
        <unstableOnDeprecation>false</unstableOnDeprecation>
        <removedJobAction>DELETE</removedJobAction>
        <removedViewAction>DELETE</removedViewAction>
        <removedConfigFilesAction>DELETE</removedConfigFilesAction>
        <lookupStrategy>JENKINS_ROOT</lookupStrategy>
      </javaposse.jobdsl.plugin.ExecuteDslScripts>
    </builders>
    <publishers/>
    <buildWrappers>
      <hudson.plugins.ws__cleanup.PreBuildCleanup plugin="ws-cleanup@0.38">
        <deleteDirs>false</deleteDirs>
        <cleanupParameter/>
        <externalDelete/>
        <disableDeferredWipeout>false</disableDeferredWipeout>
      </hudson.plugins.ws__cleanup.PreBuildCleanup>
      <com.michelin.cio.hudson.plugins.maskpasswords.MaskPasswordsBuildWrapper/>
      <hudson.plugins.timestamper.TimestamperBuildWrapper plugin="timestamper@1.11.8"/>
    </buildWrappers>
  </project>'''

  def configXML = template.replaceAll("GIT_ORG", gitOrg).replaceAll("GIT_REPO", gitRepo).replaceAll("GIT_BRANCH", gitBranch).replaceAll("TARGET_SPECIFIER", targetSpecifier)

  def xmlStream = new ByteArrayInputStream(configXML.getBytes())

  Jenkins.instance.createProjectFromXML(jobName, xmlStream)
}

def generateSeedJob_v2(dslScriptPath) {
  def jobDslScriptFile = new File(dslScriptPath)
  def workspaceFile = new File('.')

  def jobManagement = new JenkinsJobManagement(System.out, [:], workspaceFile)

  new DslScriptLoader(jobManagement).runScript(jobDslScriptFile.text)
}