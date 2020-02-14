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
package io.jenkins.x.client;

import static io.jenkins.x.client.util.Strings.notEmpty;

/**
 */
public class Pipelines {
    public static boolean isPullRequestBranch(String branchName) {
        return branchName != null && branchName.toUpperCase().startsWith("PR-");
    }

    public static Integer getBuildNumber(String build) {
        if (notEmpty(build)) {
            try {
                return Integer.parseInt(build);
            } catch (NumberFormatException e) {
                // ignore
            }
        }
        return null;
    }

    /**
     * Returns the Pull Request name from the given URL prefixed with space or an empty string if there is no URL
     */
    public static String getPullRequestName(String prUrl) {
        if (notEmpty(prUrl)) {
            int idx = prUrl.lastIndexOf("/");
            if (idx > 0) {
                return " #" + prUrl.substring(idx + 1);
            }
        }
        return "";
    }
}
