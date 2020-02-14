/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.jenkins.x.client.kube;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.util.Objects;

/**
 * An ordered key of PipelineActivity objects
 */
public class PipelineKey implements Comparable<PipelineKey> {
    private static final transient Logger LOG = LoggerFactory.getLogger(PipelineKey.class);

    private final String pipeline;
    private final int build;

    public PipelineKey(String pipeline, int build) {
        this.pipeline = pipeline;
        this.build = build;
    }

    public static PipelineKey createKey(@NotNull PipelineActivity activity) {
        PipelineActivitySpec spec = activity.getSpec();
        if (spec == null) {
            LOG.warn("No spec for pipeline " + activity);
            return null;
        }
        String pipeline = spec.getPipeline();
        if (pipeline == null) {
            LOG.warn("No pipeline name for pipeline " + activity + " spec " + spec);
            return null;
        }
        String build = spec.getBuild();
        if (build == null) {
            LOG.warn("No build for pipeline " + activity + " spec " + spec);
            return null;
        }
        try {
            int buildNumber = Integer.parseInt(build);
            return new PipelineKey(pipeline, buildNumber);
        } catch (NumberFormatException e) {
            LOG.warn("Pipeline " + pipeline + " has build number: " + build + " which could not be parsed to a number: " + e, e);
            return null;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PipelineKey that = (PipelineKey) o;
        return build == that.build &&
                Objects.equals(pipeline, that.pipeline);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pipeline, build);
    }

    @Override
    public int compareTo(PipelineKey that) {
        int answer = this.pipeline.compareTo(that.pipeline);
        if (answer == 0) {
            // lets sort newest builds first
            answer = that.build - this.build;
        }
        return answer;
    }

    public String getPipeline() {
        return pipeline;
    }

    public int getBuild() {
        return build;
    }

    @Override
    public String toString() {
        return "PipelineKey{" + pipeline + " #" + build + "}";
    }

}
