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

import static io.jenkins.x.client.util.Icons.lightDarkIcon;
import static io.jenkins.x.client.util.Strings.notEmpty;

/**
 */
public class PullRequestsNode extends TreeNode<String, BranchNode> {

    public PullRequestsNode(RepoNode repoNode, String label) {
        super(repoNode.getListeners(), repoNode, label);
    }

    public void deletePipeline(String branchName, String build, PipelineActivity activity) {
        if (notEmpty(branchName)) {
            BranchNode child = getChild(branchName);
            if (child != null) {
                child.deletePipeline(branchName, build, activity);
                if (child.isEmpty()) {
                    removeChild(branchName);
                }
            }
        }
    }

    public void upsertPipeline(String branchName, String build, PipelineActivity activity) {
        if (notEmpty(branchName)) {
            BranchNode child = getChild(branchName);
            if (child == null) {
                child = new BranchNode(this, branchName);
                putChild(branchName, child);
            }
            child.upsertPipeline(branchName, build, activity);
        }
    }

    @Override
    public String getIconPath() {
        return lightDarkIcon("git-pull-request.png");
    }

    @Override
    public String getTooltip() {
        return "Pull Requests on " + getParent().getTooltip();

    }
}
