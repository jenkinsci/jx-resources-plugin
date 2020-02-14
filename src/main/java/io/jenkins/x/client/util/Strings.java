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
package io.jenkins.x.client.util;

/**
 */
public class Strings {

    public static String getOrBlank(String value) {
        return getOrDefault(value, "");
    }

    public static String getOrDefault(String value, String defaultValue) {
        return value != null ? value : defaultValue;
    }

    /**
     * Returns true if the string is not null and not empty
     */
    public static boolean notEmpty(String text) {
        return text != null && !text.isEmpty();
    }


    /**
     * Returns true if the string is null or empty
     */
    public static boolean empty(String text) {
        return text == null || text.isEmpty();
    }

    /**
     * Capitalise the given text
     */
    public static String capitalise(String text) {
        if (empty(text)) {
            return "";
        }
        return text.substring(0, 1).toUpperCase() + text.substring(1);
    }
}
