package io.jenkins.x.client;

import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.jenkins.x.client.model.Jenkins;

import java.util.Map;

import static io.jenkins.x.client.Constants.DEFAULT_NS;
import static io.jenkins.x.client.Constants.EXPOSE_URL;
import static io.jenkins.x.client.Constants.J_PASSWD;
import static io.jenkins.x.client.Constants.J_USER;
import static io.jenkins.x.client.Constants.SVC_JENKINS;
import static io.jenkins.x.client.Constants.SVC_MONOCULAR;
import static io.jenkins.x.client.Constants.SVC_NEXUS;

/**
 * @author suren
 */
public class ServiceClient {

    private KubernetesClient client = new DefaultKubernetesClient();

    public Secret getSecret(String name) {
        String ns = client.getNamespace();

        return client.secrets().inNamespace(ns).withName(name).get();
    }

    public String getJenkinsUrl() {
        return getServiceUrl(SVC_JENKINS);
    }

    public Service getJenkinsService() {
        return getService(SVC_JENKINS);
    }

    public Service getService(String serviceName) {
        String ns = client.getNamespace();
        if(ns == null) {
            ns = DEFAULT_NS;
        }

        return client.services().inNamespace(ns).withName(serviceName).get();
    }

    public String getServiceUrl(String serviceName) {
        Service service = getService(serviceName);
        if(service == null) {
            return null;
        }

        Map<String, String> annotations = service.getMetadata().getAnnotations();
        if(annotations == null) {
            return null;
        }

        return annotations.get(EXPOSE_URL);
    }

    public String getNexusUrl() {
        return getServiceUrl(SVC_NEXUS);
    }

    public String getMonocularUrl() {
        return getServiceUrl(SVC_MONOCULAR);
    }

    public Jenkins getJenkins() {
        Jenkins jenkins = new Jenkins();
        jenkins.setUrl(getJenkinsUrl());

        Secret secret = getSecret(SVC_JENKINS);
        if(secret != null) {
            Map<String, String> data = secret.getData();
            
            jenkins.setUsername(data.get(J_USER));
            jenkins.setPassword(data.get(J_PASSWD));
        }

        return jenkins;
    }
}