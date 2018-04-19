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

import top.evodb.core.memory.heap.AbstractChunk;
import top.evodb.core.util.MathUtil;

/**
 * @author evodb
 */
public abstract class BuddyAllocator<T extends AbstractChunk> {
    private final int[] tree;
    private final int size;

    public BuddyAllocator(int size) {
        if (!MathUtil.isPowerOf2(size)) {
            size = fixSize(size);
        }
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
    }

    public T alloc(int size) {
        int index = 0;
        if (!MathUtil.isPowerOf2(size)) {
            size = fixSize(size);
        }
        int nodeSize;
        for (nodeSize = tree[index]; nodeSize != size && nodeSize != 0; nodeSize = tree[index]) {
            if (tree[left(index)] >= size) {
                index = left(index);
            } else {
                index = right(index);
            }
        }
        tree[index] = 0;
        while (index != 0) {
            index = parent(index);
            tree[index] = Math.max(tree[left(index)], tree[right(index)]);
        }
        return doAlloc(nodeSize);
    }

    public void free(T t) {

    }

    protected abstract void doFree(T t);

    protected abstract T doAlloc(int size);

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
        int shift = Integer.SIZE - Integer.numberOfLeadingZeros(size);
        return 1 << shift;
    }
}
