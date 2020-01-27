package org.jenkinsci.plugins.gwt.gitlabAdHocTrigger.global;

import static com.google.common.collect.Lists.newArrayList;

import com.google.common.annotations.VisibleForTesting;
import hudson.Extension;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import jenkins.model.GlobalConfiguration;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.StaplerRequest;

@Extension
public class JenkinsGitlabAdHocWebhookTrigger extends GlobalConfiguration implements Serializable {

  private static final long serialVersionUID = -1;

  public static JenkinsGitlabAdHocWebhookTrigger get() {
    return GlobalConfiguration.all().get(JenkinsGitlabAdHocWebhookTrigger.class);
  }

  private List<GenericVariable> genericVariables = newArrayList();
  private List<GenericRequestVariable> genericRequestVariables = newArrayList();
  private List<GenericHeaderVariable> genericHeaderVariables = newArrayList();
  private boolean printPostContent;
  private boolean printContributedVariables;
  private String causeString;
  private boolean silentResponse;

  private boolean whitelistEnabled;
  private List<WhitelistItem> whitelistItems = new ArrayList<>();

  @VisibleForTesting
  public JenkinsGitlabAdHocWebhookTrigger(
      final List<GenericVariable> genericVariables,
      final List<GenericRequestVariable> genericRequestVariables,
      final List<GenericHeaderVariable> genericHeaderVariables,
      final boolean whitelistEnabled,
      final List<WhitelistItem> whitelistItems) {

    this.genericVariables = genericVariables;
    this.genericRequestVariables = genericRequestVariables;
    this.genericHeaderVariables = genericHeaderVariables;

    this.whitelistEnabled = whitelistEnabled;
    this.whitelistItems = whitelistItems;
  }

  public JenkinsGitlabAdHocWebhookTrigger() {
    load();
  }

  @Override
  public boolean configure(final StaplerRequest req, final JSONObject json) throws FormException {
    cleanUpInstanceValues();
    req.bindJSON(this, json);
    save();
    return true;
  }

  private void cleanUpInstanceValues() {
    setCauseString(new String());
    setWhitelistItems(new ArrayList<WhitelistItem>());
    setGenericVariables(new ArrayList<GenericVariable>());
    setGenericRequestVariables(new ArrayList<GenericRequestVariable>());
    setGenericHeaderVariables(new ArrayList<GenericHeaderVariable>());
  }

  @DataBoundSetter
  public void setWhitelistEnabled(final boolean whitelistEnabled) {
    this.whitelistEnabled = whitelistEnabled;
  }

  public boolean isWhitelistEnabled() {
    return whitelistEnabled;
  }

  @DataBoundSetter
  public void setWhitelistItems(final List<WhitelistItem> whitelistItems) {
    this.whitelistItems = whitelistItems;
  }

  public List<WhitelistItem> getWhitelistItems() {
    return whitelistItems;
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

  public boolean isPrintContributedVariables() {
    return printContributedVariables;
  }

  @DataBoundSetter
  public void setPrintPostContent(final boolean printPostContent) {
    this.printPostContent = printPostContent;
  }

  public boolean isPrintPostContent() {
    return printPostContent;
  }

  @DataBoundSetter
  public void setSilentResponse(final boolean silentResponse) {
    this.silentResponse = silentResponse;
  }

  public boolean isSilentResponse() {
    return silentResponse;
  }

  @DataBoundSetter
  public void setGenericVariables(List<GenericVariable> genericVariables) {
    this.genericVariables = genericVariables;
  }

  public List<GenericVariable> getGenericVariables() {
    return genericVariables;
  }

  @DataBoundSetter
  public void setGenericRequestVariables(List<GenericRequestVariable> genericRequestVariables) {
    this.genericRequestVariables = genericRequestVariables;
  }

  public List<GenericRequestVariable> getGenericRequestVariables() {
    return genericRequestVariables;
  }

  @DataBoundSetter
  public void setGenericHeaderVariables(List<GenericHeaderVariable> genericHeaderVariables) {
    this.genericHeaderVariables = genericHeaderVariables;
  }

  public List<GenericHeaderVariable> getGenericHeaderVariables() {
    return genericHeaderVariables;
  }
}
