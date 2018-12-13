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

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

/**
 */
public class ExpandVarsTest {
    protected String jenkinsURL = "http://jenkins.jx.104.155.72.204.nip.io";
    protected String pipeline = "jstrachan/mynode10/master";
    protected String run = "9";

    @Test
    public void testBuildLogsURL() throws Exception {
    	Map<String,String> systemProperties = new HashMap<>();
    	systemProperties.put("BUILD_ID", "123");
    	systemProperties.put("OTHER", "A");
    			
    	assertEquals("123", BuildSyncRunListener.expandVars("123", systemProperties));
    	assertEquals("123", BuildSyncRunListener.expandVars("$BUILD_ID", systemProperties));
    	assertEquals("123-A", BuildSyncRunListener.expandVars("$BUILD_ID-$OTHER", systemProperties));
    }
    
    
   
}
