/*
 * The MIT License
 *
 * Copyright 2020 me.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.jenkinsci.plugins.gwt.gitlabAdHocTrigger.global;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/** @author me */
public class JobNameTool {

  private static final Logger LOGGER = Logger.getLogger(JobNameTool.class.getName());

  public JobNameTool() {}

  public String getJobNameTail(Map<String, String> resolvedVariables) {

    if (valueForKeyIs("object_kind", "deploy_done", resolvedVariables)) {
      String environmentValue = resolvedVariables.get("environment");
      if (environmentValue == null) {
        printWarningNoEnvironmentSet(resolvedVariables);
        return "deploy_done";
      }

      return "deploy_done_" + environmentValue;
    }

    // A partir de aqui, solo permitimos merge_requests:
    if (!valueForKeyIs("object_kind", "merge_request", resolvedVariables)) {
      printErrorGettingJobNameTail(resolvedVariables);
      return null;
    }

    // Time to build job name tail ...
    // 1) Merge request opened de developer a integracion.
    if ((valueForKeyIs("object_kind", "merge_request", resolvedVariables))
        && (valueForKeyIs("state_merge", "opened", resolvedVariables))
        && (valueForKeyIs("target_branch", "developer", resolvedVariables))) {
      return "integracion_continua";
    }

    // 2) Merge request opened de developer-hotfix a hotfix.
    if ((valueForKeyIs("object_kind", "merge_request", resolvedVariables))
        && (valueForKeyIs("state_merge", "opened", resolvedVariables))
        && (valueForKeyIs("target_branch", "developer-hotfix", resolvedVariables))) {
      return "integracion_continua_hotfix";
    }

    // 3) Merge request opened pero no a developer o developer-hotfix.
    if ((valueForKeyIs("object_kind", "merge_request", resolvedVariables))
        && (valueForKeyIs("state_merge", "opened", resolvedVariables))) {
      String errorMsg =
          "Although object_kind=merge_request and state_merge=opened, "
              + "target_branch is NOT developer or developer-hotfix. ";
      LOGGER.log(Level.INFO, errorMsg);
      return null;
    }

    // 3) Merge request closed a developer o developer-hotfix.
    if ((valueForKeyIs("object_kind", "merge_request", resolvedVariables))
        && (valueForKeyIs("state_merge", "closed", resolvedVariables))
        && ((valueForKeyIs("target_branch", "developer", resolvedVariables))
            || (valueForKeyIs("target_branch", "developer-hotfix", resolvedVariables)))) {
      return "merge_request_closed";
    }

    // 3) Merge request closed pero NO a developer o developer-hotfix.
    if ((valueForKeyIs("object_kind", "merge_request", resolvedVariables))
        && (valueForKeyIs("state_merge", "closed", resolvedVariables))) {
      String errorMsg =
          "Although object_kind=merge_request and state_merge=closed, "
              + "target_branch is NOT developer or developer-hotfix. ";
      LOGGER.log(Level.INFO, errorMsg);
      return null;
    }

    // 3) Merge request merged de developer a integracion.
    if ((valueForKeyIs("object_kind", "merge_request", resolvedVariables))
        && (valueForKeyIs("state_merge", "merged", resolvedVariables))
        && (valueForKeyIs("target_branch", "developer", resolvedVariables))) {
      return "mr_a_developer_accepted";
    }

    // 3) Merge request merged de developer a developer-hotfix.
    if ((valueForKeyIs("object_kind", "merge_request", resolvedVariables))
        && (valueForKeyIs("state_merge", "merged", resolvedVariables))
        && (valueForKeyIs("target_branch", "developer-hotfix", resolvedVariables))) {
      return "mr_a_dev_hotfix_accepted";
    }

    if ((valueForKeyIs("object_kind", "merge_request", resolvedVariables))
        && (valueForKeyIs("state_merge", "merged", resolvedVariables))) {
      String errorMsg =
          "Although object_kind=merge_request and state_merge=merged, "
              + "target_branch is NOT developer or developer-hotfix. ";
      LOGGER.log(Level.INFO, errorMsg);
      return null;
    }

    printErrorGettingJobNameTail(resolvedVariables);
    return null;
  }

  private void printWarningNoEnvironmentSet(Map<String, String> resolvedVariables) {
    StringBuilder sbErrorMsg = new StringBuilder();
    sbErrorMsg.append("When using  object_kind=deploy_done  it is possible ");
    sbErrorMsg.append("using environment=name to get a job named ");
    sbErrorMsg.append("deploy_done_name, as deploy_done_preproduction");

    LOGGER.log(Level.WARNING, sbErrorMsg.toString());
  }

  private void printErrorGettingJobNameTail(Map<String, String> resolvedVariables) {
    StringBuilder sbErrorMsg = new StringBuilder();
    sbErrorMsg.append("No job name tail computed. object_kind=");
    sbErrorMsg.append(resolvedVariables.get("object_kind"));
    sbErrorMsg.append(", state_merge=");
    sbErrorMsg.append(resolvedVariables.get("state_merge"));
    sbErrorMsg.append(" and target_branch=");
    sbErrorMsg.append(resolvedVariables.get("target_branch"));

    LOGGER.log(Level.WARNING, sbErrorMsg.toString());
  }

  private boolean valueForKeyIs(String key, String value, Map<String, String> resolvedVariables) {
    String currentValue = resolvedVariables.get(key);
    if (currentValue == null) {
      return (value == null);
    }

    return (currentValue.equals(value));
  }

  public String getJobFullNameWithoutTail(Map<String, String> resolvedVariables) {
    String project_path_with_namespace = resolvedVariables.get("project_path_with_namespace");
    if ((project_path_with_namespace == null) || (project_path_with_namespace.isEmpty())) {
      LOGGER.log(Level.WARNING, "Value for project_path_with_namespace is empty or null.");
      return null;
    }
    return project_path_with_namespace;
  }

  public String computeJobFullName(String jobFullNameWithoutTail, String jobNameTail) {
    if ((jobFullNameWithoutTail == null) || (jobFullNameWithoutTail.isEmpty())) {
      return null;
    }
    if ((jobNameTail == null) || (jobNameTail.isEmpty())) {
      return null;
    }
    return jobFullNameWithoutTail + "/" + jobNameTail;
  }
}
