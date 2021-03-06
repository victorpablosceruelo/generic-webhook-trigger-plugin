package org.jenkinsci.plugins.gwt.gitlabAdHocTrigger;

import static com.google.common.base.Charsets.UTF_8;
import static java.util.logging.Level.FINE;
import static java.util.logging.Level.SEVERE;
import static org.jenkinsci.plugins.gwt.gitlabAdHocTrigger.GenericResponse.jsonResponse;

import com.google.common.annotations.VisibleForTesting;
import hudson.Extension;
import hudson.model.UnprotectedRootAction;
import hudson.security.csrf.CrumbExclusion;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;
import org.jenkinsci.plugins.gwt.gitlabAdHocTrigger.global.JenkinsGitlabAdHocWebhookTrigger;
import org.jenkinsci.plugins.gwt.gitlabAdHocTrigger.global.JobNameTool;
import org.jenkinsci.plugins.gwt.gitlabAdHocTrigger.jobfinder.JobFinder;
import org.jenkinsci.plugins.gwt.gitlabAdHocTrigger.resolvers.VariablesResolver;
import org.jenkinsci.plugins.gwt.gitlabAdHocTrigger.whitelist.WhitelistException;
import org.jenkinsci.plugins.gwt.gitlabAdHocTrigger.whitelist.WhitelistVerifier;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.StaplerRequest;

@Extension
public class GenericWebHookRequestReceiver extends CrumbExclusion implements UnprotectedRootAction {

  private static final String URL_NAME = "my-gitlab-webhook-trigger";

  private static final String FULL_URL_NAME = "http://user:passsword@jenkins/" + URL_NAME;

  private static final String ERROR_GETTING_JOBS_NAME_MSG_1 =
      "Request invalid for launching jobs: Could NOT get a jobname tail from it. ";

  private static final String ERROR_GETTING_JOBS_NAME_MSG_2 =
      "Request invalid for launching jobs: "
          + "Could not get from JSON a not-null value for \'project_path_with_namespace\'";

  private static final String ERROR_GETTING_JOBS_NAME_MSG_3 =
      "Request invalid for launching jobs: " + "Could not get a valid job name from the strings ";

  private static final String NO_JOBS_MSG =
      "Did not find any jobs with "
          + GitlabAdHocTrigger.class.getSimpleName()
          + " configured! "
          + "If you are using a token, you need to pass it like ...trigger/invoke?token=TOKENHERE. "
          + "If you are not using a token, you need to authenticate like "
          + FULL_URL_NAME
          + "... ";

  private static final String NO_JOBS_AFTER_FILTER_MSG =
      "Did not find any jobs after applying the filter \'only jobs named NAME\', being NAME = ";

  private static final Logger LOGGER =
      Logger.getLogger(GenericWebHookRequestReceiver.class.getName());

  public HttpResponse doInvoke(final StaplerRequest request) {
    String postContent = null;
    Map<String, String[]> parameterMap = null;
    Map<String, List<String>> headers = null;
    try {
      headers = getHeaders(request);
      parameterMap = request.getParameterMap();
      postContent = IOUtils.toString(request.getInputStream(), UTF_8.name());
    } catch (final IOException e) {
      LOGGER.log(SEVERE, "", e);
      return jsonResponse(500, "Unable to read inputstream: " + e.getMessage());
    }

    try {
      WhitelistVerifier.verifyWhitelist(request.getRemoteAddr(), headers, postContent);
    } catch (final WhitelistException e) {
      return jsonResponse(
          403,
          "Sender, "
              + request.getRemoteHost()
              + ", with headers "
              + headers
              + " did not pass whitelist.\n"
              + e.getMessage());
    }

    final String givenToken = getGivenToken(headers, parameterMap);

    return doInvoke(headers, parameterMap, postContent, givenToken);
  }

  @VisibleForTesting
  String getGivenToken(
      final Map<String, List<String>> headers, final Map<String, String[]> parameterMap) {
    if (parameterMap.containsKey("token")) {
      return parameterMap.get("token")[0];
    }
    if (headers.containsKey("token")) {
      return headers.get("token").get(0);
    }
    if (headers.containsKey("authorization")) {
      for (final String candidateValue : headers.get("authorization")) {
        if (candidateValue.startsWith("Bearer ")) {
          return candidateValue.substring(7);
        }
      }
    }
    return null;
  }

  @VisibleForTesting
  Map<String, List<String>> getHeaders(final StaplerRequest request) {
    final Map<String, List<String>> headers = new HashMap<>();
    final Enumeration<String> headersEnumeration = request.getHeaderNames();
    while (headersEnumeration.hasMoreElements()) {
      final String headerName = headersEnumeration.nextElement();
      headers.put(headerName.toLowerCase(), Collections.list(request.getHeaders(headerName)));
    }
    return headers;
  }

  @VisibleForTesting
  HttpResponse doInvoke(
      final Map<String, List<String>> headers,
      final Map<String, String[]> parameterMap,
      final String postContent,
      final String givenToken) {

    String msg = "Received request at " + FULL_URL_NAME + " with post content:\n" + postContent;
    LOGGER.log(Level.INFO, msg);

    final JenkinsGitlabAdHocWebhookTrigger jenkinsGitlabAdHocWebhookTrigger =
        JenkinsGitlabAdHocWebhookTrigger.get();

    String causeString = jenkinsGitlabAdHocWebhookTrigger.getCauseString();
    final Map<String, String> resolvedVariables =
        new VariablesResolver(
                headers,
                parameterMap,
                postContent,
                jenkinsGitlabAdHocWebhookTrigger.getGenericVariables(),
                jenkinsGitlabAdHocWebhookTrigger.getGenericRequestVariables(),
                jenkinsGitlabAdHocWebhookTrigger.getGenericHeaderVariables())
            .getVariables();

    JobNameTool jobNameTool = new JobNameTool();

    String jobNameTail = jobNameTool.getJobNameTail(resolvedVariables);
    if (jobNameTail == null) {
      return jsonResponse(404, ERROR_GETTING_JOBS_NAME_MSG_1);
    }

    final List<FoundJob> foundJobs1 = JobFinder.findAllJobsWithTrigger(givenToken);
    if (foundJobs1.isEmpty()) {
      LOGGER.log(Level.WARNING, NO_JOBS_MSG);
      return jsonResponse(404, NO_JOBS_MSG);
    }

    String jobFullNameWithoutTail = jobNameTool.getJobFullNameWithoutTail(resolvedVariables);
    if (jobFullNameWithoutTail == null) {
      return jsonResponse(404, ERROR_GETTING_JOBS_NAME_MSG_2);
    }
    String jobFullName = jobNameTool.computeJobFullName(jobFullNameWithoutTail, jobNameTail);
    if (jobFullName == null) {
      String fullErrorMsg =
          ERROR_GETTING_JOBS_NAME_MSG_3 + jobFullNameWithoutTail + " " + jobNameTail;
      return jsonResponse(404, fullErrorMsg);
    }

    final List<FoundJob> foundJobs2 = filterJobsByName(foundJobs1, jobFullName);
    if (foundJobs2.isEmpty()) {
      String fullErrorMsg = NO_JOBS_AFTER_FILTER_MSG + jobFullName;
      LOGGER.log(Level.WARNING, fullErrorMsg);
      return jsonResponse(404, fullErrorMsg);
    }

    return invokeJobs(
        foundJobs2, headers, parameterMap, postContent, causeString, resolvedVariables);
  }

  private void logValueIsNotTheExpectedOne(String key, String value, String currentValue) {
    StringBuilder sbMsg = new StringBuilder();
    sbMsg.append("Expected value for key ");
    sbMsg.append(key).append(" is ");
    sbMsg.append(value).append(", not ").append(currentValue);
    LOGGER.log(Level.WARNING, sbMsg.toString());
  }

  private List<FoundJob> filterJobsByName(List<FoundJob> foundJobsIn, String jobFullName) {

    List<FoundJob> foundJobsOut = new ArrayList<FoundJob>();

    for (FoundJob foundJob : foundJobsIn) {
      if (jobFullName.equals(foundJob.getFullName())) {
        foundJobsOut.add(foundJob);
      }
    }
    return foundJobsOut;
  }

  private HttpResponse invokeJobs(
      List<FoundJob> foundJobs,
      Map<String, List<String>> headers,
      Map<String, String[]> parameterMap,
      String postContent,
      String causeString,
      Map<String, String> resolvedVariables) {

    final Map<String, Object> triggerResultsMap = new HashMap<>();
    boolean errors = false;
    for (final FoundJob foundJob : foundJobs) {
      try {
        LOGGER.log(FINE, "Triggering job " + foundJob.getFullName());
        LOGGER.log(FINE, " with:\n\n" + postContent + "\n\n");
        final GitlabAdHocTrigger trigger = foundJob.getGitlabAdHocTrigger();
        final GenericTriggerResults triggerResults =
            trigger.trigger(
                headers,
                parameterMap,
                postContent,
                foundJob.getFullName(),
                resolvedVariables,
                causeString);
        triggerResultsMap.put(foundJob.getFullName(), triggerResults);

      } catch (final Throwable t) {
        LOGGER.log(SEVERE, foundJob.getFullName(), t);
        final String msg = createMessageFromException(t);
        triggerResultsMap.put(foundJob.getFullName(), msg);
        errors = true;
      }
    }
    // if (foundJobs.size() > 0) {
    // return ok();
    // }
    if (errors) {
      return jsonResponse(500, "There were errors when triggering jobs.", triggerResultsMap);
    } else {
      return jsonResponse(200, "Triggered jobs.", triggerResultsMap);
    }
  }

  String createMessageFromException(final Throwable t) {
    String stacktraceInfo = "";
    if (t.getStackTrace().length > 0) {
      stacktraceInfo =
          "Thrown in: "
              + t.getStackTrace()[0].getClassName()
              + ":"
              + t.getStackTrace()[0].getLineNumber();
    }
    final String msg =
        "Exception occurred ("
            + t.getClass()
            + ": "
            + t.getMessage()
            + "), full stack trace in Jenkins server log. "
            + stacktraceInfo;
    return msg;
  }

  @Override
  public String getIconFileName() {
    return null;
  }

  @Override
  public String getDisplayName() {
    return null;
  }

  @Override
  public String getUrlName() {
    return URL_NAME;
  }

  public static String getUrl() {
    return URL_NAME;
  }

  @Override
  public boolean process(
      final HttpServletRequest request, final HttpServletResponse response, final FilterChain chain)
      throws IOException, ServletException {
    final String pathInfo = request.getPathInfo();
    if (pathInfo != null && pathInfo.startsWith("/" + URL_NAME + "/")) {
      chain.doFilter(request, response);
      return true;
    }
    return false;
  }
}
