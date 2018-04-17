/*
 * Copyright 2017-2018 The Evodb Project
 *
 *  The Evodb Project licenses this file to you under the Apache License,
 *  version 2.0 (the "License"); you may not use this file except in compliance
 *  with the License. You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *  License for the specific language governing permissions and limitations
 *  under the License.
 */

package top.evodb.core.memory;

import top.evodb.core.util.MathUtil;

/**
 * @author evodb
 */
public class BuddyAllocator {
    private final int[] treeMap;

    public BuddyAllocator(int size) {
        int depth = MathUtil.log2(size);
        int treeArrayLen = 1 << depth + 1;
        treeMap = new int[treeArrayLen - 1];
        int nodeSize = size << 1;
        for (int i = 0; i < treeMap.length; i++) {
            if (MathUtil.isPowerOf2(i + 1)) {
                nodeSize >>= 1;
            }
            treeMap[i] = nodeSize;
        }
    }

    private int left(int idx) {
        return (idx << 1) + 1;
    }

    private int right(int idx) {
        return (idx << 1) + 2;
    }

    private int parent(int idx) {
        return idx - 1 >> 1;
    }
}
