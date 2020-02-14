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
package io.jenkins.x.client;

import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.Watch;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.jenkins.x.client.kube.ClientHelper;
import io.jenkins.x.client.kube.DoneablePipelineActivities;
import io.jenkins.x.client.kube.PipelineActivity;
import io.jenkins.x.client.kube.PipelineActivityList;
import io.jenkins.x.client.kube.PipelineKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import static io.jenkins.x.client.kube.KubeHelpers.isNewer;

/**
 */
public class PipelineClient implements Closeable {
    private static final transient Logger LOG = LoggerFactory.getLogger(PipelineClient.class);

    private final NonNamespaceOperation<PipelineActivity, PipelineActivityList, DoneablePipelineActivities, Resource<PipelineActivity, DoneablePipelineActivities>> pipelines;
    private final String namespace;
    private List<Watcher<PipelineActivity>> listeners = new ArrayList<>();
    private Watch watcher;
    private Map<PipelineKey, PipelineActivity> map = new TreeMap<>();

    public PipelineClient(NonNamespaceOperation<PipelineActivity, PipelineActivityList, DoneablePipelineActivities, Resource<PipelineActivity, DoneablePipelineActivities>> pipelines, String namespace) {
        this.pipelines = pipelines;
        this.namespace = namespace;
    }

    public static PipelineClient newInstance() {
        KubernetesClient client = new DefaultKubernetesClient();
        String ns = client.getNamespace();
        Objects.nonNull(ns);
        return newInstance(client, ns);
    }

    public static PipelineClient newInstance(@NotNull KubernetesClient client, @NotNull String ns) {
        return new PipelineClient(ClientHelper.pipelineActivityClient(client, ns), ns);
    }

    public String getNamespace() {
        return namespace;
    }

    @Override
    public void close() throws IOException {
        doClose();
    }


    /**
     * Returns the current pipelines objects
     */
    public List<PipelineActivity> getPipelines() {
        synchronized (map) {
            return new ArrayList<>(map.values());
        }
    }


    public void addListener(@NotNull Watcher<PipelineActivity> listener) {
        this.listeners.add(listener);
    }

    public void removeListener(@NotNull Watcher<PipelineActivity> listener) {
        this.listeners.remove(listener);
    }

    /**
     * Starts listening in a background thread to avoid blocking the calling thread
     */
    public void startAsync() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    start();
                } catch (Exception e) {
                    LOG.error("Failed to connect to kubernetes: " + e, e);
                }
            }
        }, "Jenkins X PipelineClient Thread");
        thread.start();
    }

    /**
     * Starts listing and watching the pipelines in the namespace and firing events.
     */
    public void start() {
        doClose();

        Watcher<PipelineActivity> listener = new Watcher<PipelineActivity>() {
            @Override
            public void eventReceived(Action action, PipelineActivity pipelineActivity) {
                onEventReceived(action, pipelineActivity);
            }

            @Override
            public void onClose(KubernetesClientException e) {
                // TODO should we start watching again?
                LOG.warn("Pipeline watcher is closed: " + e, e);

                for (Watcher<PipelineActivity> listener : listeners) {
                    listener.onClose(e);
                }
            }
        };
        this.watcher = this.pipelines.watch(listener);
        PipelineActivityList list = this.pipelines.list();
        if (list != null) {
            List<PipelineActivity> items = list.getItems();
            if (items != null) {
                for (PipelineActivity item : items) {
                    onEventReceived(Watcher.Action.ADDED, item);
                }
            }
        }
    }

    protected void onEventReceived(Watcher.Action action, PipelineActivity pipelineActivity) {
        // TODO first lets filter out duplicates after joining the list and watch event stream
        PipelineKey key = PipelineKey.createKey(pipelineActivity);
        if (key == null) {
            return;
        }

        synchronized (map) {
            if (action.equals(Watcher.Action.DELETED)) {
                map.remove(key);
            } else if (action.equals(Watcher.Action.ADDED) || action.equals(Watcher.Action.MODIFIED)) {
                PipelineActivity old = map.get(key);
                if (old == null || isNewer(pipelineActivity, old)) {
                    map.put(key, pipelineActivity);
                } else {
                    // don't fire any more events as this is an old value
                    return;
                }
            }
        }

        for (Watcher<PipelineActivity> listener : listeners) {
            listener.eventReceived(action, pipelineActivity);
        }
    }


    protected void doClose() {
        if (this.watcher != null) {
            this.watcher.close();
            this.watcher = null;
        }
    }

}
