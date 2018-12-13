package org.jenkinsci.plugins.jx.resources;

import javax.annotation.Nonnull;

import com.google.common.annotations.VisibleForTesting;

import hudson.EnvVars;
import hudson.Extension;
import hudson.model.EnvironmentContributor;
import hudson.model.Run;
import hudson.model.TaskListener;

/**
 * Injects environment variables provided by Prow which are normally set by Jenkins core.
 */
@Extension
public class ProwEnvironmentContributor extends EnvironmentContributor {
    @Override
    public void buildEnvironmentFor(@Nonnull Run r, @Nonnull EnvVars envs, @Nonnull TaskListener listener) {
        String prowJobId = getEnv("PROW_JOB_ID");
        if (prowJobId != null) {
            String buildId = getEnv("BUILD_ID");
            if (buildId != null) {
                // Overrides Jenkins predefined environment variables with the one provided by Prow
                envs.put("BUILD_ID", buildId);
                envs.put("BUILD_NUMBER", buildId);
            } else {
                listener.getLogger().printf("[WARN] In a Prow job (PROW_JOB_ID = %s), expected BUILD_ID to be non-null", prowJobId);
            }
        }
    }

    @VisibleForTesting
    String getEnv(String name) {
        return System.getenv(name);
    }
}
