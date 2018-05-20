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

/**
 */
public class URLHelpers {

    /**
     * Joins all the given strings, ignoring nulls so that they form a URL with / between the paths without a // if the previous path ends with / and the next path starts with / unless a path item is blank
     *
     * @param strings the strings to join
     * @return the strings concatenated together with / while avoiding a double // between non blank strings.
     */
    public static String pathJoin(String... strings) {
        StringBuilder buffer = new StringBuilder();
        for (String string : strings) {
            if (string == null) {
                continue;
            }
            if (buffer.length() > 0) {
              boolean bufferEndsWithSeparator = buffer.toString().endsWith("/");
              boolean stringStartsWithSeparator = string.startsWith("/");
              if (bufferEndsWithSeparator) {
                if (stringStartsWithSeparator) {
                  string = string.substring(1);
                }
              } else {
                if (!stringStartsWithSeparator) {
                  buffer.append("/");
                }
              }
            }
            buffer.append(string);
        }
        return buffer.toString();
    }
}
