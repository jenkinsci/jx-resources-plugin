package org.jenkinsci.plugins.jx.resources;

import com.cloudbees.workflow.rest.external.RunExt;
import com.cloudbees.workflow.rest.external.StageNodeExt;
import com.cloudbees.workflow.rest.external.StatusExt;
import hudson.Extension;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;
import hudson.plugins.git.GitSCM;
import hudson.plugins.git.UserRemoteConfig;
import hudson.scm.SCM;
import hudson.triggers.SafeTimerTask;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.jenkins.x.client.kube.ClientHelper;
import io.jenkins.x.client.kube.DoneablePipelineActivities;
import io.jenkins.x.client.kube.KubernetesNames;
import io.jenkins.x.client.kube.PipelineActivity;
import io.jenkins.x.client.kube.PipelineActivityList;
import io.jenkins.x.client.kube.PipelineActivitySpec;
import io.jenkins.x.client.kube.PipelineActivityStep;
import io.jenkins.x.client.kube.StageActivityStep;
import io.jenkins.x.client.kube.Statuses;
import io.jenkins.x.client.util.Strings;
import io.jenkins.x.client.util.URLHelpers;
import jenkins.util.Timer;
import org.apache.commons.httpclient.HttpStatus;
import org.jenkinsci.plugins.workflow.cps.CpsScmFlowDefinition;
import org.jenkinsci.plugins.workflow.flow.FlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

import static io.jenkins.x.client.util.MarkupUtils.toYaml;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.WARNING;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.jenkinsci.plugins.jx.resources.KubernetesUtils.formatTimestamp;
import static org.jenkinsci.plugins.jx.resources.KubernetesUtils.getKubernetesClient;

/**
 * Listens to Jenkins Job build {@link Run} and updates the PipelineActivity resource
 */
@Extension
public class BuildSyncRunListener extends RunListener<Run> {
    protected static final String[] exposeUrlAnnotations = {"jenkins-x.io/exposeUrl", "fabric8.io/exposeUrl"};
    private static final Logger logger = Logger.getLogger(BuildSyncRunListener.class.getName());
    private final long pollPeriodMs = 1000;

    private final Set<Run> runsToPoll = new CopyOnWriteArraySet<>();

    private final AtomicBoolean timerStarted = new AtomicBoolean(false);
    private String jenkinsURL;

    /**
     * Joins all the given strings, ignoring nulls so that they form a URL with / between the paths without a // if the
     * previous path ends with / and the next path starts with / unless a path item is blank
     *
     * @param strings the sequence of strings to join
     * @return the strings concatenated together with / while avoiding a double // between non blank strings.
     */
    public static String joinPaths(String... strings) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < strings.length; i++) {
            sb.append(strings[i]);
            if (i < strings.length - 1) {
                sb.append("/");
            }
        }
        String joined = sb.toString();

        // And normalize it...
        return joined
                .replaceAll("/+", "/")
                .replaceAll("/\\?", "?")
                .replaceAll("/#", "#")
                .replaceAll(":/", "://");
    }

    public static String createBuildLogsUrl(String jenkinsURL, String pipeline, String buildNumberText) {
        String[] paths = pipeline.split("/");
        String path = "/job/" + String.join("/job/", paths);
        return URLHelpers.pathJoin(jenkinsURL, path, buildNumberText, "/console");
    }

    public static String createBuildUrl(String jenkinsURL, String pipeline, String buildNumberText) {

        String path = pipeline;
        int idx = path.indexOf('/');
        if (idx > 0) {
            path = path.substring(0, idx) + "%2F" + path.substring(idx + 1);
        }
        idx = path.indexOf('/');
        if (idx > 0) {
            path = path.substring(0, idx) + "/detail/" + path.substring(idx + 1);
        }
        return URLHelpers.pathJoin(jenkinsURL, "/blue/organizations/jenkins/", path, buildNumberText, "/pipeline");
    }

    @Override
    public synchronized void onStarted(Run run, TaskListener listener) {
        if (shouldPollRun(run)) {
            if (runsToPoll.add(run)) {
                logger.info("starting polling build " + run.getUrl());
            }
            checkTimerStarted();
        } else {
            logger.fine("not polling polling build " + run.getUrl() + " as its not a WorkflowJob");
        }
        super.onStarted(run, listener);
    }

    protected void checkTimerStarted() {
        if (timerStarted.compareAndSet(false, true)) {
            Runnable task = new SafeTimerTask() {
                @Override
                protected void doRun() throws Exception {
                    pollLoop();
                }
            };
            Timer.get().scheduleAtFixedRate(task, pollPeriodMs, pollPeriodMs, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public synchronized void onCompleted(Run run, @Nonnull TaskListener listener) {
        if (shouldPollRun(run)) {
            runsToPoll.remove(run);
            pollRun(run);
        }
        super.onCompleted(run, listener);
    }

    @Override
    public synchronized void onDeleted(Run run) {
        if (shouldPollRun(run)) {
            runsToPoll.remove(run);
            pollRun(run);
        }
        super.onDeleted(run);
    }

    @Override
    public synchronized void onFinalized(Run run) {
        if (shouldPollRun(run)) {
            runsToPoll.remove(run);
            pollRun(run);
        }
        super.onFinalized(run);
    }

    protected synchronized void pollLoop() {
        for (Run run : runsToPoll) {
            pollRun(run);
        }
    }

    protected synchronized void pollRun(Run run) {
        if (!(run instanceof WorkflowRun)) {
            throw new IllegalStateException("Cannot poll a non-workflow run");
        }

        RunExt wfRunExt = RunExt.create((WorkflowRun) run);

        try {
            upsertBuild(run, wfRunExt);
        } catch (KubernetesClientException e) {
            if (e.getCode() == HttpStatus.SC_UNPROCESSABLE_ENTITY) {
                runsToPoll.remove(run);
                logger.log(WARNING, "Cannot update status: {0}", e.getMessage());
                return;
            }
            throw e;
        }
    }

    private void upsertBuild(Run run, RunExt wfRunExt) {
        if (run == null) {
            return;
        }

        long started = getStartTime(run);
        String startTime = null;
        String completionTime = null;
        if (started > 0) {
            startTime = formatTimestamp(started);

            long duration = getDuration(run);
            if (duration > 0) {
                completionTime = formatTimestamp(started + duration);
            }
        }

        KubernetesClient kubeClent = getKubernetesClient();
        String namespace = GlobalPluginConfiguration.get().getNamespace();
        NonNamespaceOperation<PipelineActivity, PipelineActivityList, DoneablePipelineActivities, Resource<PipelineActivity, DoneablePipelineActivities>> client = ClientHelper.pipelineActivityClient(kubeClent, namespace);

        String parentFullName = "";

        // when using this plugin inside the jenkinsfile runner these values may not be valid so lets look for the magic env vars first
        String repoOwner = EnvironmentVariableExpander.getenv("REPO_OWNER");
        String repoName = EnvironmentVariableExpander.getenv("REPO_NAME");
        String branchName = EnvironmentVariableExpander.getenv("BRANCH_NAME");
        if (Strings.notEmpty(repoOwner) && Strings.notEmpty(repoName) && Strings.notEmpty(branchName)) {
            parentFullName = repoOwner + "/" + repoName + "/" + branchName;
        }

        String buildNumberText = EnvironmentVariableExpander.getenv("JX_BUILD_NUMBER");
        if (Strings.empty(buildNumberText)) {
            buildNumberText = EnvironmentVariableExpander.getenv("BUILD_NUMBER");
        }
        if (Strings.empty(buildNumberText)) {
            buildNumberText = EnvironmentVariableExpander.getenv("BUILD_ID");
        } 
        
        if (Strings.empty(parentFullName)) {
            parentFullName = run.getParent().getFullName();
        }
        if (Strings.empty(buildNumberText)) {
            buildNumberText = "" + run.getNumber();
        }
        String runName = parentFullName + "-" + buildNumberText;
        String name = KubernetesNames.convertToKubernetesName(runName, false);

        boolean create = false;
        PipelineActivity activity = client.withName(name).get();
        if (activity == null) {
            activity = new PipelineActivity();
            activity.setMetadata(new ObjectMetaBuilder().withName(name).build());
            create = true;
        }
        PipelineActivitySpec spec = activity.getSpec();
        if (spec == null) {
            spec = new PipelineActivitySpec();
            activity.setSpec(spec);
        }
        String oldYaml = create ? "" : toYamlOrRandom(spec);
        String status = getStatus(run);
        spec.setStatus(status);
        if (isBlank(spec.getStartedTimestamp())) {
            spec.setStartedTimestamp(startTime);
        }
        if (isBlank(spec.getCompletedTimestamp()) && Statuses.isCompleted(status)) {
            spec.setCompletedTimestamp(completionTime);
        }
        if (isBlank(spec.getPipeline())) {
            spec.setPipeline(parentFullName);
        }
        if (isBlank(spec.getBuild())) {
            spec.setBuild(buildNumberText);
        }

        String jenkinsURL = jenkinsURL(kubeClent, namespace);
        if (!isBlank(jenkinsURL)) {
            if (isBlank(spec.getBuildUrl())) {
                spec.setBuildUrl(createBuildUrl(jenkinsURL, parentFullName, buildNumberText));
            }
            if (isBlank(spec.getBuildLogsUrl())) {
                spec.setBuildLogsUrl(createBuildLogsUrl(jenkinsURL, parentFullName, buildNumberText));
            }
        }

        if (isBlank(spec.getGitUrl())) {
            String gitUrl = findGitURL(run);
            if (!isBlank(gitUrl)) {
                spec.setGitUrl(gitUrl);
            }
        }

        List<StageNodeExt> stages = wfRunExt.getStages();
        if (stages != null) {
            int i = 0;
            for (StageNodeExt stage : stages) {
                String stageStatus = getStageStatus(stage.getStatus());
                StageActivityStep stageStep = getOrCreateStage(spec, i++);
                if (stageStep != null) {
                    stageStep.setStatus(stageStatus);
                    if (status.equals(Statuses.SUCCEEDED)) {
                        switch (stageStatus) {
                            case Statuses.RUNNING:
                            case Statuses.PENDING:
                                spec.setStatus(Statuses.RUNNING);
                        }
                    }
                    String stageName = getStageName(stage);
                    stageStep.setName(stageName);
                    if (isBlank(stageStep.getStartedTimestamp())) {
                        stageStep.setStartedTimestamp(formatTimestamp(stage.getStartTimeMillis()));
                    }
                    if (isBlank(stageStep.getCompletedTimestamp()) && Statuses.isCompleted(stageStatus)) {
                        stageStep.setCompletedTimestamp(formatTimestamp(stage.getStartTimeMillis() + stage.getDurationMillis()));
                    }
                }
            }
        }

        String newYaml = create ? "" : toYamlOrRandom(spec);
        if (create || !oldYaml.equals(newYaml)) {
            client.createOrReplace(activity);
            logger.log(INFO, (create ? "Created" : "Updated") + "  pipeline activity " + name);
        }
    }

    /**
     * Returns true if the plugin is running inside Servlerless Jenkins where we don't have a static master
     * serving up a UI
     *
     * @return true if inside a UI-less servleress jenkins
     */
    private boolean isServerlessJenkins() {
        return Strings.notEmpty(EnvironmentVariableExpander.getenv("PROW_JOB_ID"));
    }

    private String findGitURL(Run run) {
        if (run instanceof WorkflowRun) {
            WorkflowRun workflowRun = (WorkflowRun) run;
            WorkflowJob job = workflowRun.getParent();
            if (job != null) {
                FlowDefinition definition = job.getDefinition();
                if (definition instanceof CpsScmFlowDefinition) {
                    CpsScmFlowDefinition cpsScmFlowDefinition = (CpsScmFlowDefinition) definition;
                    SCM scm = cpsScmFlowDefinition.getScm();
                    String url = getGitUrl(scm);
                    if (!isBlank(url)) {
                        return url;
                    }
                }
                Collection<? extends SCM> scms = job.getSCMs();
                if (scms != null) {
                    for (SCM scm : scms) {
                        String url = getGitUrl(scm);
                        if (!isBlank(url)) {
                            return url;
                        }
                    }
                }
            }
        }
        return null;
    }

    private String getGitUrl(SCM scm) {
        if (scm instanceof GitSCM) {
            GitSCM gitSCM = (GitSCM) scm;
            List<UserRemoteConfig> userRemoteConfigs = gitSCM.getUserRemoteConfigs();
            if (userRemoteConfigs != null) {
                for (UserRemoteConfig userRemoteConfig : userRemoteConfigs) {
                    if (userRemoteConfig != null) {
                        String url = userRemoteConfig.getUrl();
                        if (!isBlank(url)) {
                            return url;
                        }
                    }
                }
            }
        }
        return null;
    }

    protected String jenkinsURL(KubernetesClient kubeClient, String namespace) {
        if (isServerlessJenkins()) {
            return "";
        }
        if (this.jenkinsURL == null) {
            try {
                Service service = kubeClient.services().inNamespace(namespace).withName("jenkins").get();
                if (service != null) {
                    ObjectMeta metadata = service.getMetadata();
                    if (metadata != null) {
                        Map<String, String> annotations = metadata.getAnnotations();
                        if (annotations != null) {
                            for (String exposeUrlAnnotation : exposeUrlAnnotations) {
                                this.jenkinsURL = annotations.get(exposeUrlAnnotation);
                                if (this.jenkinsURL != null) {
                                    return this.jenkinsURL;
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                logger.log(WARNING, "Could not find Jenkins service in namespace " + namespace + ": " + e, e);
            }
        }
        return this.jenkinsURL;
    }

    protected String getStageName(StageNodeExt stage) {
        String name = stage.getName();
        if ("Declarative: Checkout SCM".equals(name)) {
            return "Checkout Source";
        }
        if ("Declarative: Post Actions".equals(name)) {
            return "Clean up";
        }
        String prefix = "Declarative: ";
        if (name.startsWith(prefix)) {
            return name.substring(prefix.length());
        }
        return name;
    }

    protected String toYamlOrRandom(Object data) {
        try {
            return toYaml(data);
        } catch (IOException e) {
            logger.log(WARNING, "Could not marshal Object " + data + " to YAML: " + e, e);
            return UUID.randomUUID().toString();
        }
    }

    private StageActivityStep getOrCreateStage(PipelineActivitySpec spec, int index) {
        int i = 0;
        List<PipelineActivityStep> steps = spec.getSteps();
        for (PipelineActivityStep step : steps) {
            StageActivityStep stage = step.getStage();
            if (stage != null) {
                if (i++ == index) {
                    return stage;
                }
            }
        }
        StageActivityStep answer = null;
        while (i <= index) {
            PipelineActivityStep step = new PipelineActivityStep();
            step.setKind("stage");
            StageActivityStep stage = new StageActivityStep();
            step.setStage(stage);
            steps.add(step);
            answer = stage;
            i++;
        }
        spec.setSteps(steps);
        return answer;
    }

    private String getStageStatus(StatusExt status) {
        if (status == null) {
            return "";
        }
        switch (status) {
            case ABORTED:
                return Statuses.ABORTED;
            case NOT_EXECUTED:
                return Statuses.NOT_EXECUTED;
            case SUCCESS:
                return Statuses.SUCCEEDED;
            case IN_PROGRESS:
                return Statuses.PENDING;
            case PAUSED_PENDING_INPUT:
                return Statuses.WAITING_FOR_APPROVAL;
            case FAILED:
                return Statuses.FAILED;
            case UNSTABLE:
                return Statuses.UNSTABLE;
            default:
                return "";
        }
    }

    private String getStatus(Run run) {
        if (run != null && !run.hasntStartedYet()) {
            if (run.isBuilding()) {
                return Statuses.RUNNING;
            } else {
                Result result = run.getResult();
                if (result != null) {
                    if (result.equals(Result.SUCCESS)) {
                        return Statuses.SUCCEEDED;
                    } else if (result.equals(Result.ABORTED)) {
                        return Statuses.ABORTED;
                    } else if (result.equals(Result.FAILURE)) {
                        return Statuses.FAILED;
                    } else if (result.equals(Result.UNSTABLE)) {
                        return Statuses.FAILED;
                    } else {
                        return Statuses.PENDING;
                    }
                }
            }
        }
        return "";
    }


    private long getStartTime(Run run) {
        return run.getStartTimeInMillis();
    }

    private long getDuration(Run run) {
        return run.getDuration();
    }

    /**
     * Returns true if we should poll the status of this run
     *
     * @param run the Run to test against
     * @return true if the should poll the status of this build run
     */
    protected boolean shouldPollRun(Run run) {
        return run instanceof WorkflowRun;
    }
}
