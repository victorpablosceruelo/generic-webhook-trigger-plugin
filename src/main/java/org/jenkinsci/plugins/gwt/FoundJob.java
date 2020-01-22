package org.jenkinsci.plugins.gwt;

public class FoundJob {

  private final String fullName;
  private final GitlabAdHocTrigger gitlabAdHocTrigger;

  public FoundJob(String fullName, GitlabAdHocTrigger gitlabAdHocTrigger) {
    this.fullName = fullName;
    this.gitlabAdHocTrigger = gitlabAdHocTrigger;
  }

  public GitlabAdHocTrigger getGitlabAdHocTrigger() {
    return gitlabAdHocTrigger;
  }

  public String getFullName() {
    return fullName;
  }
}
