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
 * distributed under the License is distributed on an "AS IS" BASIS);
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jenkinsci.plugins.jx.resources;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 *
 */
public class GitURLParserTest {
    @Test
    public void testGitParserURL() throws Exception {
        assertParseGitURL("git://host.xz/org/repo", "host.xz", "org", "repo");
        assertParseGitURL("git://host.xz/org/repo.git", "host.xz", "org", "repo");
        assertParseGitURL("git://host.xz/org/repo.git/", "host.xz", "org", "repo");
        assertParseGitURL("git://github.com/jstrachan/npm-pipeline-test-project.git", "github.com", "jstrachan", "npm-pipeline-test-project");
        assertParseGitURL("https://github.com/fabric8io/foo.git", "github.com", "fabric8io", "foo");
        assertParseGitURL("https://github.com/fabric8io/foo", "github.com", "fabric8io", "foo");
        assertParseGitURL("git@github.com:jstrachan/npm-pipeline-test-project.git", "github.com", "jstrachan", "npm-pipeline-test-project");
        assertParseGitURL("git@github.com:bar/foo.git", "github.com", "bar", "foo");
        assertParseGitURL("git@github.com:bar/foo", "github.com", "bar", "foo");
        assertParseGitURL("git@github.com:bar/overview", "github.com", "bar", "overview");
        assertParseGitURL("git@gitlab.com:bar/subgroup/foo", "gitlab.com", "bar", "foo");
        assertParseGitURL("https://gitlab.com/bar/subgroup/foo", "gitlab.com", "bar", "foo");
        assertParseGitURL("https://gitlab.com/bar/subgroup/overview", "gitlab.com", "bar", "overview");
        assertParseGitURL("http://test-user@auth.example.com/scm/bar/foo.git", "auth.example.com", "bar", "foo");
        assertParseGitURL("https://bitbucketserver.com/projects/myproject/repos/foo/pull-requests/1/overview", "bitbucketserver.com", "myproject", "foo");
        assertParseGitURL("https://bitbucketserver.com/projects/myproject/repos/foo/pull-requests/1", "bitbucketserver.com", "myproject", "foo");
    }

    private void assertParseGitURL(String url, String expectedHost, String expectedOwner, String expectedRepo) {
        GitURLInfo info = GitURLParser.parse(url);
        assertNotNull("no GitURLInfo for " + url, info);
        assertEquals("host for " + url, expectedHost, info.getHost());
        assertEquals("owner for " + url, expectedOwner, info.getOwner());
        assertEquals("repository for " + url, expectedRepo, info.getRepository());
    }
}
