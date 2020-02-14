package io.jenkins.x.client.kube;

import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinition;
import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinitionBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;

public class ClientHelper {

    public static final String JENKINS_CRD_GROUP = "jenkins.io";
    public static final String PIPELINE_ACTIVITIES_CRD_NAME = "pipelineactivities." + JENKINS_CRD_GROUP;

    public static NonNamespaceOperation<PipelineActivity, PipelineActivityList, DoneablePipelineActivities, Resource<PipelineActivity, DoneablePipelineActivities>> pipelineActivityClient(KubernetesClient client, String namespace) {
        CustomResourceDefinition runCRD = new CustomResourceDefinitionBuilder().
            withApiVersion("apiextensions.k8s.io/v1beta1").
            withNewMetadata().withName(PIPELINE_ACTIVITIES_CRD_NAME).endMetadata().
            withNewSpec().withGroup(JENKINS_CRD_GROUP).withVersion("v1").withScope("Namespaced").
            withNewNames().withKind("PipelineActivity").withShortNames("pipelineactivity", "activity", "act").withPlural("pipelineactivities").endNames().endSpec().
            build();
        return client.customResources(runCRD, PipelineActivity.class, PipelineActivityList.class, DoneablePipelineActivities.class).inNamespace(namespace);
    }

    private ClientHelper() {}

}
