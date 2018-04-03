package org.jenkinsci.plugins.jx.resources.kube;

import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinition;
import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinitionBuilder;
import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinitionList;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;

import java.util.List;

/**
 */
public class ClientHelper {

    public static String JENKINS_CRD_GROUP = "jenkins.io";
    public static String PIPELINE_ACTIVITIES_CRD_NAME = "pipelineactivities." + JENKINS_CRD_GROUP;

    public static NonNamespaceOperation<PipelineActivities, PipelineActivitiesList, DoneablePipelineActivities, Resource<PipelineActivities, DoneablePipelineActivities>> pipelineActivityClient(KubernetesClient client, String namespace) {
        CustomResourceDefinitionList crds = client.customResourceDefinitions().list();
        List<CustomResourceDefinition> crdsItems = crds.getItems();
        CustomResourceDefinition runCRD = null;
        for (CustomResourceDefinition crd : crdsItems) {
            ObjectMeta metadata = crd.getMetadata();
            if (metadata != null) {
                String name = metadata.getName();
                System.out.println("    " + name + " => " + metadata.getSelfLink());
                if (PIPELINE_ACTIVITIES_CRD_NAME.equals(name)) {
                    runCRD = crd;
                }
            }
        }
        if (runCRD == null) {
            runCRD = new CustomResourceDefinitionBuilder().
                    withApiVersion("apiextensions.k8s.io/v1beta1").
                    withNewMetadata().withName(PIPELINE_ACTIVITIES_CRD_NAME).endMetadata().
                    withNewSpec().withGroup(JENKINS_CRD_GROUP).withVersion("v1").withScope("Namespaced").
                    withNewNames().withKind("PipelineActivities").withShortNames("run").withPlural("runs").endNames().endSpec().
                    build();

            client.customResourceDefinitions().create(runCRD);
        }
        return client.customResources(runCRD, PipelineActivities.class, PipelineActivitiesList.class, DoneablePipelineActivities.class).inNamespace(namespace);
    }

}
