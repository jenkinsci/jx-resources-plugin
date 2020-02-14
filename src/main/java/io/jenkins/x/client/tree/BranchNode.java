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
import io.jenkins.x.client.kube.PipelineActivity;

import java.util.Collections;
import java.util.List;

import static io.jenkins.x.client.Pipelines.isPullRequestBranch;
import static io.jenkins.x.client.util.Icons.lightDarkIcon;

/**
 */
public class BranchNode extends TreeNode<Integer, BuildNode> {
    public BranchNode(TreeItem parent, String branchName) {
        super(parent.getListeners(), parent, branchName);
    }

    @Override
    public List<TreeItem> getChildrenItems() {
        List<TreeItem> answer = super.getChildrenItems();
        Collections.reverse(answer);
        return answer;
    }

    public void deletePipeline(String branchName, String build, PipelineActivity activity) {
        Integer buildNumber = Pipelines.getBuildNumber(build);
        if (buildNumber != null) {
            removeChild(buildNumber);
        }
    }

    public void upsertPipeline(String branchName, String build, PipelineActivity activity) {
        Integer buildNumber = Pipelines.getBuildNumber(build);
        if (buildNumber != null) {
            BuildNode child = getChild(buildNumber);
            if (child == null) {
                child = new BuildNode(this, build);
                putChild(buildNumber, child);
            }
            child.setPipeline(activity);
        }
    }

    public String getBranch() {
        return getLabel();
    }

    @Override
    public String getIconPath() {
        if (isPullRequestBranch(getBranch())) {
            return lightDarkIcon("git-pull-request.png");
        }
        return lightDarkIcon("git-branch.png");

    }

    @Override
    public String getTooltip() {
        return getParent().getTooltip() + " branch: " + getBranch();
    }
}
