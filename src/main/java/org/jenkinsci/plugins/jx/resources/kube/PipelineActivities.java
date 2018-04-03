package org.jenkinsci.plugins.jx.resources.kube;

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.fabric8.kubernetes.client.CustomResource;

/**
 */
@JsonDeserialize(
        using = JsonDeserializer.None.class
)
public class PipelineActivities extends CustomResource {
    private PipelineActivitiesSpec spec;

    public PipelineActivities() {
        setKind("PipelineActivities");
    }

    @Override
    public String toString() {
        return "PipelineActivities{" +
                "apiVersion='" + getApiVersion() + '\'' +
                ", metadata=" + getMetadata() +
                ", spec=" + spec +
                '}';
    }

    public PipelineActivitiesSpec getSpec() {
        return spec;
    }

    public void setSpec(PipelineActivitiesSpec spec) {
        this.spec = spec;
    }
}
