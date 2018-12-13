package org.jenkinsci.plugins.jx.resources;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import hudson.EnvVars;
import hudson.model.Run;
import hudson.model.TaskListener;

@RunWith(MockitoJUnitRunner.class)
public class ProwEnvironmentContributorTest {

    @Mock
    Run run;

    @Mock
    TaskListener listener;

    @Before
    public void setUp() {
        when(listener.getLogger()).thenReturn(System.out);
    }

    @Test
    public void whenProwJobIdIsSetThenInjectBuildID() {
        ProwEnvironmentContributor instance = spy(new ProwEnvironmentContributor());
        when(instance.getEnv("PROW_JOB_ID")).thenReturn("jobid");
        when(instance.getEnv("BUILD_ID")).thenReturn("buildid");

        EnvVars envvars = new EnvVars();
        envvars.put("BUILD_ID", "1");

        instance.buildEnvironmentFor(run, envvars, listener);

        assertEquals("buildid", envvars.get("BUILD_ID"));
        assertEquals("buildid", envvars.get("BUILD_NUMBER"));
    }

    @Test
    public void whenProwJobIdIsSetButNotBuildIdThenDontInjectBuildID() {
        ProwEnvironmentContributor instance = spy(new ProwEnvironmentContributor());
        when(instance.getEnv("PROW_JOB_ID")).thenReturn("jobid");

        EnvVars envvars = new EnvVars();
        envvars.put("BUILD_ID", "1");

        instance.buildEnvironmentFor(run, envvars, listener);

        assertEquals("1", envvars.get("BUILD_ID"));
        assertFalse(envvars.containsKey("BUILD_NUMBER"));
    }

    @Test
    public void whenProwJobIdIsNotSetThenDontInjectBuildID() {
        ProwEnvironmentContributor instance = spy(new ProwEnvironmentContributor());
        when(instance.getEnv("BUILD_ID")).thenReturn("buildid");

        EnvVars envvars = new EnvVars();
        envvars.put("BUILD_ID", "1");

        instance.buildEnvironmentFor(run, envvars, listener);

        assertEquals("1", envvars.get("BUILD_ID"));
        assertFalse(envvars.containsKey("BUILD_NUMBER"));
    }


}
