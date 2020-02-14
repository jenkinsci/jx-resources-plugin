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
package org.jenkinsci.plugins.jx.resources;

import java.util.Objects;

/**
 * Represents the git information from a URL
 */
public class GitURLInfo {
    private final String scheme;
    private final String host;
    private final String owner;
    private final String repository;
    private final String project;

    public GitURLInfo(String scheme, String host, String owner, String repository, String project) {
        this.scheme = scheme;
        this.host = host;
        this.owner = owner;
        this.repository = repository;
        this.project = project;
    }

    public String getHost() {
        return host;
    }

    public String getOwner() {
        return owner;
    }

    public String getRepository() {
        return repository;
    }

    public String getScheme() {
        return scheme;
    }

    public String getProject() {
        return project;
    }

    @Override
    public String toString() {
        return "GitURLInfo{" +
                "scheme='" + scheme + '\'' +
                ", host='" + host + '\'' +
                ", owner='" + owner + '\'' +
                ", repository='" + repository + '\'' +
                ", project='" + project + '\'' +
                '}';
    }
}
