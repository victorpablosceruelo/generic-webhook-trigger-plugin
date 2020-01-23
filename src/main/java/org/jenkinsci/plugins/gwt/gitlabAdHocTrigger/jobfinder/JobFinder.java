package org.jenkinsci.plugins.gwt.gitlabAdHocTrigger.jobfinder;

import static com.google.common.base.Strings.isNullOrEmpty;

import com.google.common.annotations.VisibleForTesting;
import hudson.model.BuildAuthorizationToken;
import hudson.triggers.Trigger;
import hudson.triggers.TriggerDescriptor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import jenkins.model.ParameterizedJobMixIn.ParameterizedJob;
import org.jenkinsci.plugins.gwt.gitlabAdHocTrigger.FoundJob;
import org.jenkinsci.plugins.gwt.gitlabAdHocTrigger.GitlabAdHocTrigger;

public final class JobFinder {

  private JobFinder() {}

  private static JobFinderImpersonater jobFinderImpersonater = new JobFinderImpersonater();

  @VisibleForTesting
  static void setJobFinderImpersonater(final JobFinderImpersonater jobFinderImpersonater) {
    JobFinder.jobFinderImpersonater = jobFinderImpersonater;
  }

  public static List<FoundJob> findAllJobsWithTrigger(final String givenToken) {

    final List<FoundJob> found = new ArrayList<>();

    final boolean impersonate = !isNullOrEmpty(givenToken);
    final List<ParameterizedJob> candidateProjects =
        jobFinderImpersonater.getAllParameterizedJobs(impersonate);
    for (final ParameterizedJob candidateJob : candidateProjects) {
      final GitlabAdHocTrigger candidateJobTrigger = findTrigger(candidateJob.getTriggers());
      if (candidateJobTrigger != null) {
        if (authenticationTokenMatches(
            givenToken, candidateJob.getAuthToken(), candidateJobTrigger.getToken())) {
          found.add(new FoundJob(candidateJob.getFullName(), candidateJobTrigger));
        }
      }
    }

    return found;
  }

  private static boolean authenticationTokenMatches(
      final String givenToken,
      @SuppressWarnings("deprecation") final BuildAuthorizationToken authToken,
      final String genericToken) {
    final boolean noTokenGiven = isNullOrEmpty(givenToken);
    final boolean noKindOfTokenConfigured =
        isNullOrEmpty(genericToken) && !jobHasAuthToken(authToken);
    final boolean genericTokenNotConfigured = isNullOrEmpty(genericToken);
    final boolean authTokenNotConfigured = !jobHasAuthToken(authToken);
    return genericTokenNotConfigured && authenticationTokenMatches(authToken, givenToken)
        || authTokenNotConfigured && authenticationTokenMatchesGeneric(genericToken, givenToken)
        || noTokenGiven && noKindOfTokenConfigured;
  }

  /** This is the token configured in this plugin. */
  private static boolean authenticationTokenMatchesGeneric(
      final String token, final String givenToken) {
    final boolean jobHasAuthToken = !isNullOrEmpty(token);
    final boolean authTokenWasGiven = !isNullOrEmpty(givenToken);
    if (jobHasAuthToken && authTokenWasGiven) {
      return token.equals(givenToken);
    }
    if (!jobHasAuthToken && !authTokenWasGiven) {
      return true;
    }
    return false;
  }

  /** This is the token configured in the job. A feature found in Jenkins core. */
  @SuppressWarnings("deprecation")
  private static boolean authenticationTokenMatches(
      final hudson.model.BuildAuthorizationToken authToken, final String givenToken) {

    final boolean jobHasAuthToken = jobHasAuthToken(authToken);
    final boolean authTokenWasGiven = !isNullOrEmpty(givenToken);
    if (jobHasAuthToken && authTokenWasGiven) {
      return authToken.getToken().equals(givenToken);
    }
    if (!jobHasAuthToken && !authTokenWasGiven) {
      return true;
    }
    return false;
  }

  @SuppressWarnings("deprecation")
  private static boolean jobHasAuthToken(final hudson.model.BuildAuthorizationToken authToken) {
    return authToken != null && !isNullOrEmpty(authToken.getToken());
  }

  private static GitlabAdHocTrigger findTrigger(final Map<TriggerDescriptor, Trigger<?>> triggers) {
    if (triggers == null) {
      return null;
    }
    for (final Trigger<?> candidate : triggers.values()) {
      if (candidate instanceof GitlabAdHocTrigger) {
        return (GitlabAdHocTrigger) candidate;
      }
    }
    return null;
  }
}
