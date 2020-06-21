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

import java.util.ArrayList;
import java.util.List;

/** @author me */
public class GenericVariableUtil {

  public static GenericVariable[] getPreDefinedGenericVariables() {
    List<GenericVariable> toAdd = new ArrayList<GenericVariable>();

    toAdd.add(new GenericVariable("project", "$.project"));
    toAdd.add(new GenericVariable("object_kind", "$.object_kind"));
    toAdd.add(new GenericVariable("source_branch", "$.object_attributes.source_branch"));
    toAdd.add(new GenericVariable("target_branch", "$.object_attributes.target_branch"));
    toAdd.add(new GenericVariable("merge_status", "$.object_attributes.merge_status"));
    toAdd.add(new GenericVariable("project_name", "$.object_attributes.source.name"));
    toAdd.add(new GenericVariable("project_namespace", "$.object_attributes.source.namespace"));
    toAdd.add(
        new GenericVariable(
            "project_path_namespace", "$.object_attributes.source.path_with_namespace"));
    toAdd.add(new GenericVariable("target_url", "$.object_attributes.target.git_http_url"));
    toAdd.add(new GenericVariable("source_url", "$.object_attributes.source.git_http_url"));
    toAdd.add(new GenericVariable("result_gitlab_url", "$.object_attributes.url"));
    toAdd.add(new GenericVariable("state_merge", "$.object_attributes.state"));
    toAdd.add(new GenericVariable("project_id", "$.project.id"));
    toAdd.add(new GenericVariable("author_id", "$.object_attributes.author_id"));
    toAdd.add(new GenericVariable("user_name", "$.user.name"));
    toAdd.add(new GenericVariable("homepage", "$.object_attributes.source.homepage"));
    toAdd.add(new GenericVariable("title", "$.object_attributes.title"));

    toAdd.add(new GenericVariable("merge_request_id", "$.object_attributes.id"));
    toAdd.add(new GenericVariable("merge_request_iid", "$.object_attributes.iid"));

    toAdd.add(new GenericVariable("JDK", "$.labels.[0].description"));

    return toAdd.toArray(new GenericVariable[0]);
  }

  public static String getPreDefinedGenericVariablesStr() {
    StringBuilder sbMsg = new StringBuilder();
    for (GenericVariable v : getPreDefinedGenericVariables()) {
      sbMsg.append("\n");
      sbMsg.append(v.getKey());
      sbMsg.append("&nbsp;:&nbsp;");
      sbMsg.append(v.getValue());
      sbMsg.append("&nbsp;(");
      sbMsg.append(v.getExpressionType().toString());
      sbMsg.append(")<BR/> ");
    }
    return sbMsg.toString();
  }
}
