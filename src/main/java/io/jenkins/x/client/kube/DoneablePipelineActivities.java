package io.jenkins.x.client.kube;

import io.fabric8.kubernetes.api.builder.Function;
import io.fabric8.kubernetes.client.CustomResourceDoneable;

/**
 */
public class DoneablePipelineActivities extends CustomResourceDoneable<PipelineActivity> {
    public DoneablePipelineActivities(PipelineActivity resource, Function function) {
        super(resource, function);
    }
}
