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

/** @author me */
public class JobNameTailTool {

  public static String getJobNameTail(Map<String, String> resolvedVariables) {

    // 1: Solo permitimos merge_requests:
    if (!valueForKeyIs("object_kind", "merge_request", resolvedVariables)) {
      return null;
    }

    // Time to build job name tail ...
    // 1) Merge request opened de developer a integracion.
    if ((valueForKeyIs("object_kind", "merge_request", resolvedVariables))
        && (valueForKeyIs("state_merge", "opened", resolvedVariables))
        && (valueForKeyIs("source_branch", "developer", resolvedVariables))
        && (valueForKeyIs("target_branch", "integracion", resolvedVariables))) {
      return "_m2int";
    }

    // 2) Merge request open de developer-hotfix a hotfix.
    if ((valueForKeyIs("object_kind", "merge_request", resolvedVariables))
        && (valueForKeyIs("state_merge", "opened", resolvedVariables))
        && (valueForKeyIs("source_branch", "developer-hotfix", resolvedVariables))
        && (valueForKeyIs("target_branch", "hotfix", resolvedVariables))) {
      return "_m2hotf";
    }

    // 3) Merge request merged de developer a integracion.
    if ((valueForKeyIs("object_kind", "merge_request", resolvedVariables))
        && (valueForKeyIs("state_merge", "merged", resolvedVariables))
        && (valueForKeyIs("source_branch", "developer", resolvedVariables))
        && (valueForKeyIs("target_branch", "integracion", resolvedVariables))) {
      return "_dep2Art";
    }

    return null;
  }

  private static boolean valueForKeyIs(
      String key, String value, Map<String, String> resolvedVariables) {
    String currentValue = resolvedVariables.get(key);
    if (currentValue == null) {
      return (value == null);
    }

    return (currentValue.equals(value));
  }

  public static String getJobFullNameWithoutTail(Map<String, String> resolvedVariables) {
    return resolvedVariables.get("project_path_with_namespace");
  }

  public static String computeJobFullName(String jobFullNameWithoutTail, String jobNameTail) {

    int index = jobFullNameWithoutTail.lastIndexOf("/");
    if (index < 0) {
      return null;
    }

    String jobBaseName = jobFullNameWithoutTail.substring(index + 1);
    if (jobBaseName == null) {
      return null;
    }

    String jobFullName = jobFullNameWithoutTail + "/" + jobBaseName + jobNameTail;
    return jobFullName;
  }
}
