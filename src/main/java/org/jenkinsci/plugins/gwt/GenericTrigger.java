package org.jenkinsci.plugins.gwt;

import static com.google.common.collect.Lists.newArrayList;
import static org.jenkinsci.plugins.gwt.ParameterActionUtil.createParameterAction;
import static org.jenkinsci.plugins.gwt.Renderer.isMatching;
import static org.jenkinsci.plugins.gwt.Renderer.renderText;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.Extension;
import hudson.model.CauseAction;
import hudson.model.Item;
import hudson.model.Job;
import hudson.model.ParametersAction;
import hudson.model.ParametersDefinitionProperty;
import hudson.triggers.Trigger;
import hudson.triggers.TriggerDescriptor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.model.ParameterizedJobMixIn;
import org.jenkinsci.Symbol;
import org.jenkinsci.plugins.gwt.resolvers.VariablesResolver;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

public class GenericTrigger extends Trigger<Job<?, ?>> {

  /** A value of -1 will make sure the quiet period of the job will be used. */
  private static final int RESPECT_JOBS_QUIET_PERIOD = -1;

  private List<GenericVariable> genericVariables = newArrayList();
  private final String regexpFilterText;
  private final String regexpFilterExpression;
  private List<GenericRequestVariable> genericRequestVariables = newArrayList();
  private List<GenericHeaderVariable> genericHeaderVariables = newArrayList();
  private boolean printPostContent;
  private boolean printContributedVariables;
  private String causeString;
  private String token;
  private boolean silentResponse;

  private static final Logger LOGGER = Logger.getLogger(GenericTrigger.class.getName());

  @Symbol("GenericTrigger")
  public static class GenericDescriptor extends TriggerDescriptor {

    @Override
    public boolean isApplicable(final Item item) {
      return Job.class.isAssignableFrom(item.getClass());
    }

    @Override
    public String getDisplayName() {
      return "GitLab Ad-Hoc Webhook Trigger";
    }
  }

  @DataBoundConstructor
  public GenericTrigger(
      final List<GenericVariable> genericVariables,
      final String regexpFilterText,
      final String regexpFilterExpression,
      final List<GenericRequestVariable> genericRequestVariables,
      final List<GenericHeaderVariable> genericHeaderVariables) {
    // this.genericVariables = addDefaultGenericVariables(genericVariablesIn);
    this.genericVariables = genericVariables;
    this.regexpFilterExpression = regexpFilterExpression;
    this.regexpFilterText = regexpFilterText;
    this.genericRequestVariables = genericRequestVariables;
    this.genericHeaderVariables = genericHeaderVariables;
  }

  public static List<GenericVariable> getDefaultGenericVariables() {
    List<GenericVariable> genericVariables = new ArrayList<GenericVariable>();
    return addDefaultGenericVariables(genericVariables);
  }

  private static List<GenericVariable> addDefaultGenericVariables(
      List<GenericVariable> genericVariables) {

    final List<GenericVariable> toAdd = getDefaultVariables();
    final List<String> genericVariablesKeys = getKeys(genericVariables);

    for (GenericVariable varToAdd : toAdd) {
      String toAddKey = varToAdd.getVariableName();
      if (!genericVariablesKeys.contains(toAddKey)) {
        genericVariables.add(varToAdd);
      }
    }
    return genericVariables;
  }

  private static List<String> getKeys(List<GenericVariable> genericVariablesList) {
    List<String> listOut = new ArrayList<String>();
    if (null != genericVariablesList) {
      for (GenericVariable var : genericVariablesList) {
        listOut.add(var.getVariableName());
      }
    }
    return listOut;
  }

  private static List<GenericVariable> getDefaultVariables() {
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
    toAdd.add(new GenericVariable("project_iid", "$.object_attributes.iid"));
    return toAdd;
  }

  @DataBoundSetter
  public void setCauseString(final String causeString) {
    this.causeString = causeString;
  }

  public String getCauseString() {
    return causeString;
  }

  @DataBoundSetter
  public void setPrintContributedVariables(final boolean printContributedVariables) {
    this.printContributedVariables = printContributedVariables;
  }

  @DataBoundSetter
  public void setPrintPostContent(final boolean printPostContent) {
    this.printPostContent = printPostContent;
  }

  @DataBoundSetter
  public void setSilentResponse(final boolean silentResponse) {
    this.silentResponse = silentResponse;
  }

  public boolean isSilentResponse() {
    return silentResponse;
  }

  public boolean isPrintContributedVariables() {
    return printContributedVariables;
  }

  public boolean isPrintPostContent() {
    return printPostContent;
  }

  @DataBoundSetter
  public void setToken(final String token) {
    this.token = token;
  }

  public String getToken() {
    return token;
  }

  @Extension public static final GenericDescriptor DESCRIPTOR = new GenericDescriptor();

  @SuppressWarnings("static-access")
  @SuppressFBWarnings({"RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT", "NP_NULL_ON_SOME_PATH"})
  public GenericTriggerResults trigger(
      final Map<String, List<String>> headers,
      final Map<String, String[]> parameterMap,
      final String postContent,
      final String jobFullName) {
    final Map<String, String> resolvedVariables =
        new VariablesResolver(
                headers,
                parameterMap,
                postContent,
                genericVariables,
                genericRequestVariables,
                genericHeaderVariables)
            .getVariables();

    StringBuilder sbMsg = new StringBuilder();
    for (Map.Entry<String, String> resolvedVariable : resolvedVariables.entrySet()) {
      sbMsg.append("\n").append("Var value: ").append(resolvedVariable.getKey());
      sbMsg.append(" -> ").append(resolvedVariable.getValue()).append(" ");
    }
    LOGGER.log(Level.INFO, sbMsg.toString());

    final String renderedRegexpFilterText = renderText(regexpFilterText, resolvedVariables);
    final boolean isMatching = isMatching(renderedRegexpFilterText, regexpFilterExpression);

    hudson.model.Queue.Item item = null;
    if (isMatching) {
      if (resolvedVariablesValuesAreValid(resolvedVariables, jobFullName)) {
        item = triggerJobAux(resolvedVariables, postContent);
      }
    }
    return new GenericTriggerResults(
        item, resolvedVariables, renderedRegexpFilterText, regexpFilterExpression);
  }

  private boolean resolvedVariablesValuesAreValid(
      Map<String, String> resolvedVariables, String jobFullName) {
    if (!valueForKeyIs("object_kind", "merge_request", resolvedVariables)) {
      return false;
    }
    if (!valueForKeyIs("project_path_with_namespace", jobFullName, resolvedVariables)) {
      return false;
    }
    return true;
  }

  private boolean valueForKeyIs(String key, String value, Map<String, String> resolvedVariables) {
    String currentValue = resolvedVariables.get(key);
    if (currentValue == null) {
      if (value != null) {
        logValueIsNotTheExpectedOne(key, value, currentValue);
        return false;
      }
      return true;
    }

    if (currentValue.equals(value)) {
      return true;
    }

    logValueIsNotTheExpectedOne(key, value, currentValue);
    return false;
  }

  private void logValueIsNotTheExpectedOne(String key, String value, String currentValue) {
    StringBuilder sbMsg = new StringBuilder();
    sbMsg.append("Expected value for key ");
    sbMsg.append(key).append(" is ");
    sbMsg.append(value).append(", not ").append(currentValue);
    LOGGER.log(Level.WARNING, sbMsg.toString());
  }

  public hudson.model.Queue.Item triggerJobAux(
      Map<String, String> resolvedVariables, String postContent) {
    hudson.model.Queue.Item item = null;

    final String cause = renderText(causeString, resolvedVariables);
    final GenericCause genericCause =
        new GenericCause(
            postContent, resolvedVariables, printContributedVariables, printPostContent, cause);
    final ParametersDefinitionProperty parametersDefinitionProperty =
        job.getProperty(ParametersDefinitionProperty.class);
    final ParametersAction parameters =
        createParameterAction(parametersDefinitionProperty, resolvedVariables);
    item =
        retrieveScheduleJob(job) //
            .scheduleBuild2(
                job, RESPECT_JOBS_QUIET_PERIOD, new CauseAction(genericCause), parameters);
    return item;
  }

  @SuppressWarnings("rawtypes")
  private ParameterizedJobMixIn<?, ?> retrieveScheduleJob(final Job<?, ?> job) {
    return new ParameterizedJobMixIn() {
      @Override
      protected Job<?, ?> asJob() {
        return job;
      }
    };
  }

  public List<GenericVariable> getGenericVariables() {
    return genericVariables;
  }

  public String getRegexpFilterExpression() {
    return regexpFilterExpression;
  }

  public List<GenericRequestVariable> getGenericRequestVariables() {
    return genericRequestVariables;
  }

  public List<GenericHeaderVariable> getGenericHeaderVariables() {
    return genericHeaderVariables;
  }

  public String getRegexpFilterText() {
    return regexpFilterText;
  }

  @Override
  public String toString() {
    return "GenericTrigger [genericVariables="
        + genericVariables
        + ", regexpFilterText="
        + regexpFilterText
        + ", regexpFilterExpression="
        + regexpFilterExpression
        + ", genericRequestVariables="
        + genericRequestVariables
        + ", genericHeaderVariables="
        + genericHeaderVariables
        + "]";
  }
}
