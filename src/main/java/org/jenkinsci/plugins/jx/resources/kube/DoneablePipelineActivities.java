package org.jenkinsci.plugins.jx.resources.kube;

import io.fabric8.kubernetes.api.builder.Function;
import io.fabric8.kubernetes.client.CustomResourceDoneable;

/**
 */
public class DoneablePipelineActivities extends CustomResourceDoneable<PipelineActivities> {
    public DoneablePipelineActivities(PipelineActivities resource, Function function) {
        super(resource, function);
    }
}
