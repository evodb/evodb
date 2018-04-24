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
import top.evodb.core.memory.AllocatorOutOfMemoryException;
import top.evodb.core.memory.BuddyAllocator;
import top.evodb.core.util.MemoryLeak;
import top.evodb.core.util.MemoryLeakDetector;

/**
 * Thread safe
 *
 * @author evodb
 */
public class ByteChunkAllocator extends BuddyAllocator<ByteChunk> {
    private final byte[] buf;
    private int offset;
    private final TreeMap<Integer, LinkedList> objCache;
    private final MemoryLeakDetector memoryLeakDetector;
    private int detectCount;
    private boolean allowOverAlloc;

    public ByteChunkAllocator(int size) {
        super(size);
        buf = new byte[super.size];
        objCache = new TreeMap<>();
        memoryLeakDetector = new MemoryLeakDetector();
    }

    @Override
    protected void doFree(ByteChunk byteChunk) {
        if (byteChunk.getAllocator() == this) {
            synchronized (objCache) {
                LinkedList linkedList = objCache.computeIfAbsent(byteChunk.getRawLength(), k -> new LinkedList());
                linkedList.offer(byteChunk);
            }
        }
    }

    @Override
    protected ByteChunk doAlloc(int nodeIndex, int nodeSize, int reqSize) {
        synchronized (objCache) {
//            LinkedList linkedList = objCache.computeIfAbsent(nodeSize, k -> new LinkedList());
            AbstractChunk chunk = null;//(AbstractChunk) linkedList.poll();
            if (nodeSize == 0) {
                if (isAllowOverAlloc()) {
                    chunk = new ByteChunk(this, buf, offset, offset + nodeSize - 1, offset + reqSize, nodeIndex);
                } else {
                    throw new AllocatorOutOfMemoryException();
                }
            }
            if (chunk == null) {
                if (memoryLeakDetector.getDetectLevel() != MemoryLeakDetector.DetectLevel.DISABLE) {
                    if (memoryLeakDetector.getDetectLevel() == MemoryLeakDetector.DetectLevel.HIGH) {
                        chunk = warp(nodeSize, nodeIndex, reqSize);
                    } else if (memoryLeakDetector.getDetectLevel() == MemoryLeakDetector.DetectLevel.MIDDLE) {
                        detectCount++;
                        if (detectCount == Integer.MAX_VALUE) {
                            detectCount = 0;
                        }
                        if (detectCount % memoryLeakDetector.getDetectRate() == 0) {
                            chunk = warp(nodeSize, nodeIndex, reqSize);
                        } else {
                            chunk = new ByteChunk(this, buf, offset, offset + nodeSize - 1, offset + reqSize, nodeIndex);
                        }
                    }
                } else {
                    chunk = new ByteChunk(this, buf, offset, offset + nodeSize - 1, offset + reqSize, nodeIndex);
                }
                offset += nodeSize;
            } else {
                if (chunk instanceof LeakedAwareByteChunk) {
                    ((LeakedAwareByteChunk) chunk).getMemoryLeak().generateTraceInfo(4);
                }
                chunk.reuse(nodeIndex);
            }
            return (ByteChunk) chunk;
        }
    }


    public boolean isAllowOverAlloc() {
        return allowOverAlloc;
    }

    public void setAllowOverAlloc(boolean allowOverAlloc) {
        this.allowOverAlloc = allowOverAlloc;
    }

    private LeakedAwareByteChunk warp(int nodeSize, int nodeIndex, int reqSize) {
        ByteChunk byteChunk = new ByteChunk(this, buf, offset, offset + nodeSize - 1, offset + reqSize, nodeIndex);
        MemoryLeak memoryLeak = memoryLeakDetector.open(byteChunk);
        memoryLeak.generateTraceInfo(4);
        return new LeakedAwareByteChunk(byteChunk, memoryLeak);
    }

    public MemoryLeakDetector getMemoryLeakDetector() {
        return memoryLeakDetector;
    }
}
