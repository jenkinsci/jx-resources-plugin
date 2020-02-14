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
package io.jenkins.x.client.tree;

/**
 * @author suren
 */
public abstract class TreeModelListenerAdapter implements TreeModelListener {
    public static final String E_ADDED = "added";
    public static final String E_UPDATED = "updated";
    public static final String E_DELETED = "deleted";

    /**
     * Combine of all events.
     * @param treeItem tree item
     * @param event event
     */
    public abstract void event(TreeItem treeItem, String event);

    @Override
    public void itemAdded(TreeItem treeItem) {
        event(treeItem, E_ADDED);
    }

    @Override
    public void itemUpdated(TreeItem treeItem) {
        event(treeItem, E_UPDATED);
    }

    @Override
    public void itemDeleted(TreeItem treeItem) {
        event(treeItem, E_DELETED);
    }
}
