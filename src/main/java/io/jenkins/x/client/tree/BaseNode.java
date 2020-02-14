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
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 */
public abstract class BaseNode<K, T extends BaseNode> implements TreeItem {
    public List<T> getChildren() {
        return Collections.EMPTY_LIST;
    }

    public boolean isEmpty() {
        return getChildren().isEmpty();
    }

    public List<TreeItem> getChildrenItems() {
        List<TreeItem> answer = new ArrayList<>();
        answer.addAll(this.getChildren());
        return answer;
    }

    @Override
    public String getIconPath() {
        return "";
    }

    @Override
    public String getTooltip() {
        return "";
    }

    @Override
    public Object[] getPath() {
        LinkedList<TreeItem> list = new LinkedList<>();
        TreeItem node = this;
        while (node != null) {
            list.addFirst(node);
            node = node.getParent();
        }
        return list.toArray();
    }
}
