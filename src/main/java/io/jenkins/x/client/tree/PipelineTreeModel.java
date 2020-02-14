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

import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.Watcher;
import io.jenkins.x.client.PipelineClient;
import io.jenkins.x.client.kube.PipelineActivity;
import io.jenkins.x.client.kube.PipelineActivitySpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.jenkins.x.client.util.Strings.empty;
import static io.jenkins.x.client.util.Strings.notEmpty;

/**
 */
public class PipelineTreeModel extends TreeNode<String, OwnerNode> implements Watcher<PipelineActivity> {
    private static final transient Logger LOG = LoggerFactory.getLogger(PipelineTreeModel.class);
    private PipelineClient pipelineClient;

    public PipelineTreeModel(PipelineClient pipelineClient) {
        super(new TreeModelListeners(), null, "Pipelines");
        this.pipelineClient = pipelineClient;
        this.pipelineClient.addListener(this);
    }

    public static PipelineTreeModel newInstance() {
        return newInstance(PipelineClient.newInstance());
    }

    public static PipelineTreeModel newStaticInstance() {
        return newStaticInstance(PipelineClient.newInstance());
    }

    public static PipelineTreeModel newStaticInstance(PipelineClient pipelineClient) {
        PipelineTreeModel model = new PipelineTreeModel(pipelineClient);
        pipelineClient.start();
        return model;
    }

    public static PipelineTreeModel newInstance(PipelineClient pipelineClient) {
        PipelineTreeModel model = new PipelineTreeModel(pipelineClient);
        pipelineClient.startAsync();
        return model;
    }

    public PipelineClient getPipelineClient() {
        return pipelineClient;
    }

    public void addListener(TreeModelListener listener) {
        getListeners().addListener(listener);
    }

    public void removeListener(TreeModelListener listener) {
        getListeners().removeListener(listener);
    }

    @Override
    public String getIconPath() {
        return "jx-logo.png";
    }

    @Override
    public String getTooltip() {
        return "Jenkins X Pipelines";
    }

    @Override
    public void eventReceived(Action action, PipelineActivity activity) {
        ObjectMeta metadata = activity.getMetadata();
        PipelineActivitySpec spec = activity.getSpec();
        if (metadata == null || spec == null) {
            return;
        }
        String name = metadata.getName();
        String owner = spec.getGitOwner();
        String repo = spec.getGitRepository();
        String pipeline = spec.getPipeline();
        String branchName = "";
        String build = spec.getBuild();
        if (notEmpty(pipeline)) {
            String[] values = pipeline.split("/");
            if (values != null && values.length > 2) {
                if (empty(owner)) {
                    owner = values[0];
                }
                if (empty(repo)) {
                    repo = values[1];
                }
                if (empty(branchName)) {
                    branchName = values[2];
                }
            }
        }
        if (empty(branchName)) {
            branchName = "master";
        }
        if (empty(owner) || empty(repo) || empty(branchName)) {
            LOG.info("Missing data for PipelineActivity " + name + " owner: " + owner + " repo: " + repo + " branchName: " + branchName);
            return;
        }

        if (action.equals(Action.DELETED)) {
            OwnerNode child = getChild(owner);
            if (child != null) {
                child.deletePipeline(owner, repo, branchName, build, activity);
                removeChild(owner);
            }
        } else if (action.equals(Action.ADDED) || action.equals(Action.MODIFIED)) {
            OwnerNode child = getChild(owner);
            if (child == null) {
                child = new OwnerNode(this, owner);
                putChild(owner, child);
            }
            child.upsertPipeline(repo, branchName, build, activity);
        }
    }

    @Override
    public void onClose(KubernetesClientException cause) {
    }
}
