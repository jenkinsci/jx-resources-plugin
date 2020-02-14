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

import java.net.MalformedURLException;
import java.net.URL;

/**
 *
 */
public class GitURLParser {
    protected static String gitPrefix = "git@";

    public static GitURLInfo parse(String text) {
        // lets turn git://host/path URLs into a real URLs java can parse
        if (text.startsWith("git:")) {
            text = "http:" + text.substring(4);
        }
        try {
            URL url = new URL(text);
            return parseURL(url);
        } catch (MalformedURLException e) {
            // handle git@ kinds of URIs
            if (text.startsWith(gitPrefix)) {
                String t = trimPrefix(text, gitPrefix);
                t = trimPrefix(t, "/");
                t = trimPrefix(t, "/");
                t = trimSuffix(t, "/");
                t = trimSuffix(t, ".git");


                String[] arr = t.split(":|/");
                if (arr.length >= 3) {
                    return new GitURLInfo("git", arr[0], arr[1], arr[arr.length - 1], "");
                }
            }
        }
        return null;
    }

    public static GitURLInfo parseURL(URL url) {
        String path = url.getPath();

        // This is necessary for Bitbucket Server in some cases.
        String trimPath = trimPrefix(path, "/scm");

        // This is necessary for Bitbucket Server, EG: /projects/ORG/repos/NAME/pull-requests/1/overview
        trimPath = trimPath.replaceAll("/pull-requests/[0-9]+(/overview)?", "");

        // This is necessary for Bitbucket Server in other cases
        trimPath = trimPath.replace("/projects/", "/");
        trimPath = trimPath.replace("/repos/", "/");
        trimPath.replaceAll("/pull.*/[0-9]+$", "");

        // Remove leading and trailing slashes so that splitting on "/" won't result
        // in empty strings at the beginning & end of the array.
        trimPath = trimPrefix(trimPath, "/");
        trimPath = trimSuffix(trimPath, "/");
        trimPath = trimSuffix(trimPath, ".git");

        String[] arr = trimPath.split("/");
        String project = "";
        String owner = "";
        String name = "";
        if (arr.length >= 2) {
            // We're assuming the beginning of the path is of the form /<org>/<repo> or /<org>/<subgroup>/.../<repo>
            owner = arr[0];
            project = arr[0];
            name = arr[arr.length - 1];
        } else if (arr.length == 1) {
            // We're assuming the beginning of the path is of the form /<org>/<repo>
            owner = arr[0];
            project = arr[0];
        }
        return new GitURLInfo(url.getProtocol(), url.getHost(), owner, name, project);

    }

    protected static String trimPrefix(String text, String prefix) {
        if (text.startsWith(prefix)) {
            return text.substring(prefix.length());
        }
        return text;
    }

    protected static String trimSuffix(String text, String prefix) {
        if (text.endsWith(prefix)) {
            return text.substring(0, text.length() - prefix.length());
        }
        return text;
    }

}