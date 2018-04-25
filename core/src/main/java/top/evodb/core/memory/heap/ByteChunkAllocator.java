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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger LOGGER = LoggerFactory.getLogger(ByteChunkAllocator.class);
    private final byte[] buf;
    private final LinkedList objCache;
    private final MemoryLeakDetector memoryLeakDetector;
    private int detectCount;
    private boolean allowOverAlloc = true;
    private int maxObjCacheSize = 20000;
    private int cacheSize = 0;

    public ByteChunkAllocator(int size) {
        super(size);
        buf = new byte[super.size];
        objCache = new LinkedList();
        memoryLeakDetector = new MemoryLeakDetector();
    }

    @Override
    protected void doFree(ByteChunk byteChunk) {
        if (byteChunk.getAllocator() == this) {
            synchronized (objCache) {
                cacheSize++;
                if (cacheSize <= maxObjCacheSize) {
                    objCache.offer(byteChunk);
                }
            }
        }
    }

    @Override
    protected ByteChunk doAlloc(int nodeIndex, int nodeSize, int reqSize) {
        synchronized (objCache) {
            AbstractChunk chunk = (AbstractChunk) objCache.poll();
            if (nodeSize == 0) {
                if (isAllowOverAlloc()) {
                    LOGGER.error("Heap memory is out,please check!!!", new AllocatorOutOfMemoryException());
                    printTree();
                    byte[] bytes = new byte[reqSize];
                    chunk = new ByteChunk(this, bytes, 0, bytes.length - 1, bytes.length, -1);
                    return (ByteChunk) chunk;
                } else {
                    throw new AllocatorOutOfMemoryException();
                }
            }
            int offset = (nodeIndex + 1) * nodeSize - size;
            if (chunk == null) {
                if (memoryLeakDetector.getDetectLevel() != MemoryLeakDetector.DetectLevel.DISABLE) {
                    if (memoryLeakDetector.getDetectLevel() == MemoryLeakDetector.DetectLevel.HIGH) {
                        chunk = warp(nodeSize, nodeIndex, reqSize, offset);
                    } else if (memoryLeakDetector.getDetectLevel() == MemoryLeakDetector.DetectLevel.MIDDLE) {
                        detectCount++;
                        if (detectCount == Integer.MAX_VALUE) {
                            detectCount = 0;
                        }
                        if (detectCount % memoryLeakDetector.getDetectRate() == 0) {
                            chunk = warp(nodeSize, nodeIndex, reqSize, offset);
                        } else {
                            chunk = new ByteChunk(this, buf, offset, offset + nodeSize - 1, offset + reqSize, nodeIndex);
                        }
                    }
                } else {
                    chunk = new ByteChunk(this, buf, offset, offset + nodeSize - 1, offset + reqSize, nodeIndex);
                }
            } else {
                if (chunk instanceof LeakedAwareByteChunk) {
                    ((LeakedAwareByteChunk) chunk).getMemoryLeak().generateTraceInfo(4);
                }
                chunk.reuse(offset, offset + nodeSize - 1, offset + reqSize, nodeIndex);
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

    private LeakedAwareByteChunk warp(int nodeSize, int nodeIndex, int reqSize, int offset) {
        ByteChunk byteChunk = new ByteChunk(this, buf, offset, offset + nodeSize - 1, offset + reqSize, nodeIndex);
        MemoryLeak memoryLeak = memoryLeakDetector.open(byteChunk);
        memoryLeak.generateTraceInfo(4);
        return new LeakedAwareByteChunk(byteChunk, memoryLeak);
    }

    public int getMaxObjCacheSize() {
        return maxObjCacheSize;
    }

    public void setMaxObjCacheSize(int maxObjCacheSize) {
        this.maxObjCacheSize = maxObjCacheSize;
    }

    public MemoryLeakDetector getMemoryLeakDetector() {
        return memoryLeakDetector;
    }
}
