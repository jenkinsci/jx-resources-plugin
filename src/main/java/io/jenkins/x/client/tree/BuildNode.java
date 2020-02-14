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

import io.jenkins.x.client.kube.PipelineActivity;
import io.jenkins.x.client.kube.PipelineActivitySpec;
import io.jenkins.x.client.kube.PipelineActivityStep;
import io.jenkins.x.client.kube.PreviewActivityStep;
import io.jenkins.x.client.kube.PromoteActivityStep;
import io.jenkins.x.client.kube.PromotePullRequestStep;
import io.jenkins.x.client.kube.PromoteUpdateStep;
import io.jenkins.x.client.kube.StageActivityStep;
import io.jenkins.x.client.util.Strings;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static io.jenkins.x.client.Pipelines.getPullRequestName;
import static io.jenkins.x.client.util.Strings.getOrBlank;
import static io.jenkins.x.client.util.Strings.notEmpty;
import static io.jenkins.x.client.util.Times.elapsedTime;

/**
 */
public class BuildNode extends TreeNode<Integer, StageNode> {
    private PipelineActivity pipeline;

    public BuildNode(BranchNode branchNode, String build) {
        super(branchNode.getListeners(), branchNode, build);
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
        }
        return "";
    }

    @Override
    public String getTooltip() {
        PipelineActivitySpec step = getSpec();
        return "#" + getBuild() + ": " + getStatus() + elapsedTime(" Duration: ", step.getStartedTimestamp(), step.getCompletedTimestamp());
    }

    public PipelineActivity getPipeline() {
        if (pipeline == null) {
            pipeline = new PipelineActivity();
        }
        return pipeline;
    }

    public void setPipeline(PipelineActivity pipeline) {
        this.pipeline = pipeline;
        List<PipelineActivityStep> steps = getSteps();
        Map<Integer, StageNode> children = new TreeMap<>();

        for (PipelineActivityStep step : steps) {
            addSteps(children, step);
        }
        setChildren(children);

        getListeners().itemUpdated(this);
    }

    protected void addSteps(Map<Integer, StageNode> children, PipelineActivityStep step) {
        StageActivityStep stage = step.getStage();
        PromoteActivityStep promote = step.getPromote();
        PreviewActivityStep preview = step.getPreview();
        if (stage != null) {
            children.put(children.size(), new StageNode(this, stage.getName(), stage));
        }
        if (preview != null) {
            children.put(children.size(), new StageNode(this, "Preview", preview));
        }
        if (promote != null) {
            String name = "Promote";
            String envName = Strings.capitalise(promote.getEnvironment());
            if (notEmpty(envName)) {
                name = "Promote to " + envName;
            }
            children.put(children.size(), new StageNode(this, name, promote));


            PromotePullRequestStep pullRequest = promote.getPullRequest();
            if (pullRequest != null) {
                String prName = name + " Pull Request" + getPullRequestName(pullRequest.getPullRequestURL());
                children.put(children.size(), new StageNode(this, prName, pullRequest));
            }
            PromoteUpdateStep update = promote.getUpdate();
            if (update != null) {
                String updateName = name + " Update";
                children.put(children.size(), new StageNode(this, updateName, update));
            }
        }
    }

    public PipelineActivitySpec getSpec() {
        PipelineActivitySpec spec = getPipeline().getSpec();
        if (spec == null) {
            spec = new PipelineActivitySpec();
            pipeline.setSpec(spec);
        }
        return spec;
    }

    public String getBuild() {
        return getSpec().getBuild();
    }

    public String getVersion() {
        return getSpec().getVersion();
    }

    public String getBuildUrl() {
        return getSpec().getBuildUrl();
    }

    public String getBuildLogsUrl() {
        return getSpec().getBuildLogsUrl();
    }

    public String getGitUrl() {
        return getSpec().getGitUrl();
    }

    public String getGitRepository() {
        return getSpec().getGitRepository();
    }

    public String getGitOwner() {
        return getSpec().getGitOwner();
    }

    public String getReleaseNotesUrl() {
        return getSpec().getReleaseNotesUrl();
    }

    public String getStatus() {
        return getOrBlank(getSpec().getStatus());
    }

    public String getStartedTimestamp() {
        return getSpec().getStartedTimestamp();
    }

    public String getCompletedTimestamp() {
        return getSpec().getCompletedTimestamp();
    }

    public List<PipelineActivityStep> getSteps() {
        List<PipelineActivityStep> steps = getSpec().getSteps();
        if (steps == null) {
            return Collections.EMPTY_LIST;
        }
        return steps;
    }


}
