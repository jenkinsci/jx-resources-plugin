package org.jenkinsci.plugins.jx.resources.kube;

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.fabric8.kubernetes.client.CustomResource;

/**
 */
@JsonDeserialize(
        using = JsonDeserializer.None.class
)
public class PipelineActivity extends CustomResource {
    private PipelineActivitySpec spec;

    public PipelineActivity() {
        setKind("PipelineActivity");
    }

    @Override
    public String toString() {
        return "PipelineActivity{" +
                "apiVersion='" + getApiVersion() + '\'' +
                ", metadata=" + getMetadata() +
                ", spec=" + spec +
                '}';
    }

    public PipelineActivitySpec getSpec() {
        return spec;
    }

    public void setSpec(PipelineActivitySpec spec) {
        this.spec = spec;
    }
}
