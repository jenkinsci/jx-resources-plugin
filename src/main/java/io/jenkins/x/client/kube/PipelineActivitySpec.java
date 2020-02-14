/*
 * The MIT License
 *
 * Copyright (c) 2004-2010, Sun Microsystems, Inc., Kohsuke Kawaguchi, Yahoo! Inc., CloudBees, Inc.
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
package io.jenkins.x.client.kube;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.fabric8.kubernetes.api.model.KubernetesResource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 */
@JsonDeserialize(
        using = JsonDeserializer.None.class
)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class PipelineActivitySpec implements KubernetesResource {
    private String pipeline;
    private String build;
    private String version;
    private String status;
    private String startedTimestamp;
    private String completedTimestamp;
    private List<PipelineActivityStep> steps = new ArrayList<>();
    private String buildUrl;
    private String buildLogsUrl;
    private String gitUrl;
    private String gitRepository;
    private String gitOwner;
    private String gitBranch;
    private String author;
    private String pullTitle;
    private String releaseNotesUrl;
    private String lastCommitSHA;
    private String lastCommitMessage;
    private String lastCommitURL;
    private String context;
    private String baseSHA;
    private List<PipelineAttachment> attachments = new ArrayList<>();

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperties(Map<String, Object> additionalProperties) {
        this.additionalProperties = additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    @Override
    public String toString() {
        return "PipelineActivitiesSpec{" + additionalProperties + "}";
    }

    public String getPipeline() {
        return pipeline;
    }

    public void setPipeline(String pipeline) {
        this.pipeline = pipeline;
    }

    public String getBuild() {
        return build;
    }

    public void setBuild(String build) {
        this.build = build;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getBuildUrl() {
        return buildUrl;
    }

    public void setBuildUrl(String buildUrl) {
        this.buildUrl = buildUrl;
    }

    public String getBuildLogsUrl() {
        return buildLogsUrl;
    }

    public void setBuildLogsUrl(String buildLogsUrl) {
        this.buildLogsUrl = buildLogsUrl;
    }

    public String getGitUrl() {
        return gitUrl;
    }

    public void setGitUrl(String gitUrl) {
        this.gitUrl = gitUrl;
    }

    public String getGitRepository() {
        return gitRepository;
    }

    public void setGitRepository(String gitRepository) {
        this.gitRepository = gitRepository;
    }

    public String getGitOwner() {
        return gitOwner;
    }

    public void setGitOwner(String gitOwner) {
        this.gitOwner = gitOwner;
    }

    public String getReleaseNotesUrl() {
        return releaseNotesUrl;
    }

    public void setReleaseNotesUrl(String releaseNotesUrl) {
        this.releaseNotesUrl = releaseNotesUrl;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStartedTimestamp() {
        return startedTimestamp;
    }

    public void setStartedTimestamp(String startedTimestamp) {
        this.startedTimestamp = startedTimestamp;
    }

    public String getCompletedTimestamp() {
        return completedTimestamp;
    }

    public void setCompletedTimestamp(String completedTimestamp) {
        this.completedTimestamp = completedTimestamp;
    }

    public List<PipelineActivityStep> getSteps() {
        return steps;
    }

    public void setSteps(List<PipelineActivityStep> steps) {
        this.steps = steps;
    }

    public String getGitBranch() {
        return gitBranch;
    }

    public void setGitBranch(String gitBranch) {
        this.gitBranch = gitBranch;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getPullTitle() {
        return pullTitle;
    }

    public void setPullTitle(String pullTitle) {
        this.pullTitle = pullTitle;
    }

    public String getLastCommitSHA() {
        return lastCommitSHA;
    }

    public void setLastCommitSHA(String lastCommitSHA) {
        this.lastCommitSHA = lastCommitSHA;
    }

    public String getLastCommitMessage() {
        return lastCommitMessage;
    }

    public void setLastCommitMessage(String lastCommitMessage) {
        this.lastCommitMessage = lastCommitMessage;
    }

    public String getLastCommitURL() {
        return lastCommitURL;
    }

    public void setLastCommitURL(String lastCommitURL) {
        this.lastCommitURL = lastCommitURL;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getBaseSHA() {
        return baseSHA;
    }

    public void setBaseSHA(String baseSHA) {
        this.baseSHA = baseSHA;
    }

    public List<PipelineAttachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<PipelineAttachment> attachments) {
        this.attachments = attachments;
    }
}
