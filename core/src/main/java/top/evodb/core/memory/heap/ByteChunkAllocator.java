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

package top.evodb.core.memory.heap;

import java.util.LinkedList;
import java.util.TreeMap;
import top.evodb.core.memory.BuddyAllocator;

/**
 * Thread safe
 *
 * @author evodb
 */
public class ByteChunkAllocator extends BuddyAllocator<ByteChunk> {
    private final byte[] buf;
    private int offset;
    private final TreeMap<Integer, LinkedList> objCache;

    public ByteChunkAllocator(int size) {
        super(size);
        buf = new byte[size];
        objCache = new TreeMap<>();
    }

    @Override
    protected void doFree(ByteChunk byteChunk) {
        if (byteChunk.getAllocator() == this) {
            synchronized (objCache) {
                LinkedList linkedList = objCache.computeIfAbsent(byteChunk.getLength(), k -> new LinkedList());
                linkedList.offer(byteChunk);
            }
        }
    }

    @Override
    protected ByteChunk doAlloc(int size) {
        if (size == 0) {
            return null;
        }
        synchronized (objCache) {
            LinkedList linkedList = objCache.computeIfAbsent(size, k -> new LinkedList());
            ByteChunk byteChunk = (ByteChunk) linkedList.poll();
            if (byteChunk == null) {
                byteChunk = new ByteChunk(this, buf, offset, offset + size - 1);
                offset += size;
            } else {
                byteChunk.reuse();
            }
            return byteChunk;
        }
    }
}
