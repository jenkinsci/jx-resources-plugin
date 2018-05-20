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
package org.jenkinsci.plugins.jx.resources.kube;

/**
 */
public class Statuses {
    public static final String PENDING = "Pending";
    public static final String RUNNING = "Running";
    public static final String SUCCEEDED = "Succeeded";
    public static final String FAILED = "Failed";
    public static final String ABORTED = "Aborted";
    public static final String ERROR = "Error";
    public static final String WAITING_FOR_APPROVAL = "WaitingForApproval";
    public static final String NOT_EXECUTED = "NotExecuted";
    public static final String UNSTABLE = "Unstable";

    /**
     * Returns true if the pipeline has completed running
     *
     * @return whether its completed or not
     * @param status the status string
     */
    public static boolean isCompleted(String status) {
        switch (status) {
            case ABORTED:
            case SUCCEEDED:
            case FAILED:
                return true;
            default:
                return false;
        }
    }
}
