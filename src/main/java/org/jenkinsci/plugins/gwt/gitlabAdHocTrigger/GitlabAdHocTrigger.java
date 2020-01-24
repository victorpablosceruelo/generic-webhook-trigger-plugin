package org.jenkinsci.plugins.gwt.gitlabAdHocTrigger;

import static org.jenkinsci.plugins.gwt.gitlabAdHocTrigger.ParameterActionUtil.createParameterAction;
import static org.jenkinsci.plugins.gwt.gitlabAdHocTrigger.Renderer.isMatching;
import static org.jenkinsci.plugins.gwt.gitlabAdHocTrigger.Renderer.renderText;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.Extension;
import hudson.model.CauseAction;
import hudson.model.Item;
import hudson.model.Job;
import hudson.model.ParametersAction;
import hudson.model.ParametersDefinitionProperty;
import hudson.triggers.Trigger;
import hudson.triggers.TriggerDescriptor;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.model.ParameterizedJobMixIn;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

public class GitlabAdHocTrigger extends Trigger<Job<?, ?>> {

  /** A value of -1 will make sure the quiet period of the job will be used. */
  private static final int RESPECT_JOBS_QUIET_PERIOD = -1;

  private final String regexpFilterText;
  private final String regexpFilterExpression;
  private String token;

  private static final Logger LOGGER = Logger.getLogger(GitlabAdHocTrigger.class.getName());

  private void logJobTriggeredWithParams(
      String jobFullName, final Map<String, String> resolvedVariables) {
    StringBuilder sbMsg = new StringBuilder();
    sbMsg.append("Triggering job ").append(jobFullName).append(" with variables: \n");

    for (Map.Entry<String, String> resolvedVariable : resolvedVariables.entrySet()) {
      sbMsg.append(resolvedVariable.getKey());
      sbMsg.append(":").append(resolvedVariable.getValue()).append("; ");
    }
    LOGGER.log(Level.INFO, sbMsg.toString());
  }

  @Symbol("GitlabAdHocTrigger")
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
  public GitlabAdHocTrigger(final String regexpFilterText, final String regexpFilterExpression) {

    this.regexpFilterExpression = regexpFilterExpression;
    this.regexpFilterText = regexpFilterText;
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
      final String jobFullName,
      Map<String, String> resolvedVariables,
      String causeString) {

    final String renderedRegexpFilterText = renderText(regexpFilterText, resolvedVariables);
    final boolean isMatching = isMatching(renderedRegexpFilterText, regexpFilterExpression);

    hudson.model.Queue.Item item = null;
    if (isMatching) {
      if (resolvedVariablesValuesAreValid(resolvedVariables, jobFullName)) {
        logJobTriggeredWithParams(jobFullName, resolvedVariables);
        item = triggerJobAux(resolvedVariables, postContent, causeString);
      }
    }
    return new GenericTriggerResults(
        item, resolvedVariables, renderedRegexpFilterText, regexpFilterExpression);
  }

  private boolean resolvedVariablesValuesAreValid(
      Map<String, String> resolvedVariables, String jobFullName) {

    // 1: Solo permitimos merge_requests:
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
      Map<String, String> resolvedVariables, String postContent, String causeString) {
    hudson.model.Queue.Item item = null;

    final String cause = renderText(causeString, resolvedVariables);
    final GenericCause genericCause = new GenericCause(postContent, resolvedVariables, cause);
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

  public String getRegexpFilterExpression() {
    return regexpFilterExpression;
  }

  public String getRegexpFilterText() {
    return regexpFilterText;
  }

  @Override
  public String toString() {
    return "GitlabAdHocTrigger ["
        + "regexpFilterText="
        + regexpFilterText
        + ", regexpFilterExpression="
        + regexpFilterExpression
        + "]";
  }
}
