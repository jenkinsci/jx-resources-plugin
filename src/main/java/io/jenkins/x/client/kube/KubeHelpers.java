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

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.ObjectMeta;

import static io.jenkins.x.client.util.Strings.notEmpty;

/**
 */
public class KubeHelpers {

    /**
     * Returns true if the first parameter is newer than the second
     */
    public static boolean isNewer(HasMetadata newer, HasMetadata older) {
        long n1 = parseResourceVersion(newer);
        long n2 = parseResourceVersion(older);
        return n1 >= n2;
    }


    /**
     * Returns the numeric resource version of the resource
     */
    public static long parseResourceVersion(HasMetadata obj) {
        ObjectMeta metadata = obj.getMetadata();
        if (metadata != null) {
            String resourceVersion = metadata.getResourceVersion();
            if (notEmpty(resourceVersion)) {
                try {
                    return Long.parseLong(resourceVersion);
                } catch (NumberFormatException e) {
                    // ignore
                }
            }
        }
        return 0;
    }
}