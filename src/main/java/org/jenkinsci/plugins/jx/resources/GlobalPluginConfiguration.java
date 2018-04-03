package org.jenkinsci.plugins.jx.resources;

import hudson.Extension;
import hudson.model.Computer;
import hudson.triggers.SafeTimerTask;
import io.fabric8.kubernetes.client.KubernetesClient;
import jenkins.model.GlobalConfiguration;
import jenkins.model.Jenkins;
import jenkins.util.Timer;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.jenkinsci.plugins.jx.resources.KubernetesUtils.getNamespaceOrUseDefault;

@Extension
public class GlobalPluginConfiguration extends GlobalConfiguration {

    private static final transient Logger logger = Logger.getLogger(GlobalPluginConfiguration.class.getName());

    private boolean enabled = true;

    private String server;

    private String namespace;

    @DataBoundConstructor
    public GlobalPluginConfiguration(boolean enable, String server, String namespace) {
        this.enabled = enable;
        this.server = server;
        this.namespace = namespace;
        configChange();
    }

    public GlobalPluginConfiguration() {
        load();
        configChange();
        save();
    }

    public static GlobalPluginConfiguration get() {
        return GlobalConfiguration.all().get(GlobalPluginConfiguration.class);
    }

    @Override
    public String getDisplayName() {
        return "Jenkins X Resources";
    }

    @Override
    public boolean configure(StaplerRequest req, JSONObject json) throws hudson.model.Descriptor.FormException {
        req.bindJSON(this, json);
        configChange();
        save();
        return true;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    private void configChange() {
        try {
            if (!enabled) {
                KubernetesUtils.shutdownKubernetesClient();
                return;
            }
            KubernetesClient kubeClient = KubernetesUtils.getKubernetesClient(server, this);
            this.namespace = getNamespaceOrUseDefault(namespace, kubeClient);

            Runnable task = new SafeTimerTask() {
                @Override
                protected void doRun() throws Exception {
                    logger.info("Waiting for Jenkins to be started");
                    while (true) {
                        Computer[] computers = Jenkins.getActiveInstance().getComputers();
                        boolean ready = false;
                        for (Computer c : computers) {
                            // Jenkins.isAcceptingTasks() results in hudson.model.Node.isAcceptingTasks() getting called, and that always returns true;
                            // the Computer.isAcceptingTasks actually introspects various Jenkins data structures to determine readiness
                            if (c.isAcceptingTasks()) {
                                ready = true;
                                break;
                            }
                        }
                        if (ready) {
                            break;
                        }
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            // ignore
                        }
                    }
                }
            };
            // lets give jenkins a while to get started ;)
            Timer.get().schedule(task, 1, TimeUnit.SECONDS);
        } catch (Exception e) {
            if (e.getCause() != null) {
                logger.log(Level.SEVERE, "Failed to configure OpenShift Jenkins Sync Plugin: " + e.getCause());
            } else {
                logger.log(Level.SEVERE, "Failed to configure OpenShift Jenkins Sync Plugin: " + e);
            }
        }
    }

}
