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

import java.util.ArrayList;
import java.util.List;

/**
 */
public class TreeModelListeners implements TreeModelListener {
    private List<TreeModelListener> listeners = new ArrayList<>();

    public void addListener(TreeModelListener listener) {
        listeners.add(listener);
    }

    public void removeListener(TreeModelListener listener) {
        listeners.add(listener);
    }

    @Override
    public void itemAdded(TreeItem treeItem) {
        for (TreeModelListener listener : listeners) {
            listener.itemAdded(treeItem);
        }
    }

    @Override
    public void itemUpdated(TreeItem treeItem) {
        for (TreeModelListener listener : listeners) {
            listener.itemUpdated(treeItem);
        }
    }

    @Override
    public void itemDeleted(TreeItem treeItem) {
        for (TreeModelListener listener : listeners) {
            listener.itemDeleted(treeItem);
        }
    }
}
