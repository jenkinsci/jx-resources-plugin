package org.jenkinsci.plugins.jx.resources;

import javax.annotation.Nonnull;

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
        if (System.getenv("PROW_JOB_ID") != null) {
            envs.put("BUILD_ID", System.getenv("BUILD_ID"));
            envs.put("BUILD_NUMBER", System.getenv("BUILD_NUMBER"));
        }
    }
}
