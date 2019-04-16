package org.jenkinsci.plugins.jx.resources;

import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.apache.commons.lang.StringUtils;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;
import static java.time.temporal.ChronoField.HOUR_OF_DAY;
import static java.time.temporal.ChronoField.MINUTE_OF_HOUR;
import static java.time.temporal.ChronoField.SECOND_OF_MINUTE;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.logging.Logger;

/**
 */
public class KubernetesUtils {

    private final static Logger logger = Logger.getLogger(KubernetesUtils.class.getName());
    private static final DateTimeFormatter dateFormatter = new DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .append(ISO_LOCAL_DATE)
            .appendLiteral('T')
            .appendValue(HOUR_OF_DAY, 2)
            .appendLiteral(':')
            .appendValue(MINUTE_OF_HOUR, 2)
            .optionalStart()
            .appendLiteral(':')
            .appendValue(SECOND_OF_MINUTE, 2)
            .appendOffsetId().toFormatter();
    private static KubernetesClient kubernetesClient;

    /**
     * Initializes an {@link KubernetesClient}
     *
     * @param serverUrl the optional URL of where the OpenShift cluster API server is running
     * @return  the kubernetes client
     */
    protected static KubernetesClient initializeKubernetesClient(String serverUrl) {
        if (serverUrl != null && !serverUrl.isEmpty()) {
            ConfigBuilder configBuilder = new ConfigBuilder();
            configBuilder.withMasterUrl(serverUrl);
            Config config = configBuilder.build();
            return new DefaultKubernetesClient(config);
        }
        return new DefaultKubernetesClient();
    }

    public synchronized static KubernetesClient getKubernetesClient() {
        return getKubernetesClient(null, null);
    }

    public synchronized static KubernetesClient getKubernetesClient(String server, GlobalPluginConfiguration config) {
        if (kubernetesClient == null) {
            if (server == null) {
                if (config == null) {
                    config = GlobalPluginConfiguration.get();
                }
                if (config != null) {
                    server = config.getServer();
                }
            }
            kubernetesClient = initializeKubernetesClient(server);
        }
        return kubernetesClient;
    }


    public synchronized static void shutdownKubernetesClient() {
        if (kubernetesClient != null) {
            kubernetesClient.close();
            kubernetesClient = null;
        }
    }


    /**
     * Gets the current namespace running Jenkins inside or returns a reasonable default
     *
     * @param configuredNamespace the optional configured namespace
     * @param client              the OpenShift client
     * @return the default namespace using either the configuration value, the default namespace on the client or "default"
     */
    public static String getNamespaceOrUseDefault(String configuredNamespace, KubernetesClient client) {
        String namespace = configuredNamespace;
        if (namespace != null && namespace.startsWith("${") && namespace.endsWith("}")) {
            String envVar = namespace.substring(2, namespace.length() - 1);
            namespace = System.getenv(envVar);
            if (StringUtils.isBlank(namespace)) {
                logger.warning("No value defined for namespace environment variable `" + envVar + "`");
            }
        }
        if (StringUtils.isBlank(namespace)) {
            if (client != null) {
                namespace = client.getNamespace();
            }
            if (StringUtils.isBlank(namespace)) {
                namespace = "default";
            }
        }
        return namespace;
    }


    public static long parseResourceVersion(String resourceVersion) {
        try {
            return Long.parseLong(resourceVersion);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public static String formatTimestamp(long timestamp) {
        return ZonedDateTime.ofInstant(
            Instant.ofEpochMilli(timestamp), ZoneId.systemDefault())
            .format(dateFormatter);
    }

}
