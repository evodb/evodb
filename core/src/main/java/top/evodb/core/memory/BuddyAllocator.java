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

import java.util.Arrays;
import top.evodb.core.util.MathUtil;

/**
 * @author evodb
 */
public class BuddyAllocator {
    private final int[] tree;
    private final int size;

    public BuddyAllocator(int size) {
        this.size = size;
        int depth = MathUtil.log2(size);
        int treeArrayLen = (1 << depth + 1) - 1;
        tree = new int[treeArrayLen];
        int nodeSize = size << 1;
        for (int i = 0; i < tree.length; i++) {
            if (MathUtil.isPowerOf2(i + 1)) {
                nodeSize >>= 1;
            }
            tree[i] = nodeSize;
        }
        System.out.println(Arrays.toString(tree));
    }

    protected int alloc(int size) {
        int index = 0;
        size = fixSize(size);
        for (int nodeSize = tree[index]; nodeSize != size; nodeSize = tree[index]) {
            if (nodeSize > size) {
                index = left(index);
            } else if (nodeSize < size) {
                index = right(index);
            }
        }
        int foundNodeSize = tree[index];
        tree[index] = 0;
        while (index != 0) {
            index = parent(index);
            tree[index] = Math.max(tree[left(index)], tree[right(index)]);
        }
        System.out.println(Arrays.toString(tree));
        return foundNodeSize;
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

    private int fixSize(int size) {
        int shift = Integer.SIZE - 1 - Integer.numberOfLeadingZeros(size);
        return 1 << shift;
    }
}
