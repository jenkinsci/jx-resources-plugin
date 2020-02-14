/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.jenkins.x.client.tree;

import io.jenkins.x.client.Pipelines;
import io.jenkins.x.client.kube.CoreActivityStep;
import io.jenkins.x.client.kube.GitStatus;
import io.jenkins.x.client.kube.PreviewActivityStep;
import io.jenkins.x.client.kube.PromoteActivityStep;
import io.jenkins.x.client.kube.PromotePullRequestStep;
import io.jenkins.x.client.kube.PromoteUpdateStep;

import java.util.List;

import static io.jenkins.x.client.util.Strings.getOrBlank;
import static io.jenkins.x.client.util.Strings.notEmpty;
import static io.jenkins.x.client.util.Times.elapsedTime;

/**
 */
public class StageNode extends TreeNode<String, StageNode> {
    private final CoreActivityStep step;

    public StageNode(TreeItem parent, String label, CoreActivityStep step) {
        super(parent.getListeners(), parent, label);
        this.step = step;
    }

    @Override
    public String getIconPath() {
        switch (getStatus()) {
            case "Succeeded":
                return "atomist_build_passed.png";
            case "Failed":
            case "Error":
                return "atomist_build_failed.png";
            case "Running":
                return "spinner.gif";
            case "Aborted":
                return "circle-64.png";
            case "NotExecuted":
                // TODO
                return "";
        }
        return "";
    }

    @Override
    public String getTooltip() {
        return getLabel() + ": " + getStatus() + elapsedTime(" Duration: ", getStartedTimestamp(), getCompletedTimestamp());
    }

    public String getPullRequestURL() {
        if (step instanceof PreviewActivityStep) {
            PreviewActivityStep previewActivityStep = (PreviewActivityStep) step;
            return previewActivityStep.getPullRequestURL();
        } else if (step instanceof PromoteActivityStep) {
            PromoteActivityStep promoteActivityStep = (PromoteActivityStep) step;
            PromotePullRequestStep pullRequest = promoteActivityStep.getPullRequest();
            if (pullRequest != null) {
                return pullRequest.getPullRequestURL();
            }
        } else if (step instanceof PromotePullRequestStep) {
            PromotePullRequestStep pullRequestStep = (PromotePullRequestStep) step;
            return pullRequestStep.getPullRequestURL();
        }
        return null;
    }

    public String getPullRequestName() {
        return Pipelines.getPullRequestName(getPullRequestURL());
    }

    public String getApplicationURL() {
        if (step instanceof PreviewActivityStep) {
            PreviewActivityStep previewActivityStep = (PreviewActivityStep) step;
            return previewActivityStep.getApplicationURL();
        } else if (step instanceof PromoteActivityStep) {
            PromoteActivityStep promoteActivityStep = (PromoteActivityStep) step;
            return promoteActivityStep.getApplicationURL();
        }
        return null;
    }

    public String getUpdatePipelineURL() {
        if (step instanceof PromoteUpdateStep) {
            PromoteUpdateStep promoteUpdateStep = (PromoteUpdateStep) step;
            List<GitStatus> statuses = promoteUpdateStep.getStatuses();
            if (statuses != null) {
                for (GitStatus status : statuses) {
                    String url = status.getUrl();
                    if (notEmpty(url)) {
                        return url;
                    }
                }
            }
        }
        return null;
    }

    public String getDescription() {
        return step.getDescription();
    }

    public String getStatus() {
        return getOrBlank(step.getStatus());
    }

    public String getStartedTimestamp() {
        return step.getStartedTimestamp();
    }

    public String getCompletedTimestamp() {
        return step.getCompletedTimestamp();
    }

    public CoreActivityStep getStep() {
        return step;
    }
}
