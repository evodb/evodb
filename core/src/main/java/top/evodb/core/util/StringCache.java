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

package top.evodb.core.util;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import top.evodb.core.memory.heap.ByteChunk;

/**
 * LRU
 *
 * @author evodb
 */
public class StringCache {
    private int maxStringSize = 128;
    private int maxCacheElementSize = 20000;

    private final AtomicInteger hit = new AtomicInteger(0);
    private final AtomicInteger accessCount = new AtomicInteger(0);
    private final LinkedHashMap cache;

    public static StringCache newInstance(int maxCacheElementSize) {
        return new StringCache(maxCacheElementSize);
    }

    private StringCache(int maxCacheElementSize) {
        this.maxCacheElementSize = maxCacheElementSize;
        cache = new LinkedHashMap(maxCacheElementSize, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry eldest) {
                return size() > maxCacheElementSize;
            }
        };
    }

    public String getString(ByteChunk byteChunk) {
        if (byteChunk.getLength() <= maxStringSize) {
            accessCount.incrementAndGet();
            synchronized (cache) {
                ByteChunkEntry entry = (ByteChunkEntry) cache.get(byteChunk);
                if (entry != null) {
                    hit.incrementAndGet();
                    cache.put(byteChunk, entry);
                    return entry.value;
                } else {
                    try {
                        ByteChunk clone = (ByteChunk) byteChunk.clone();
                        ByteChunkEntry byteChunkEntry = new ByteChunkEntry(byteChunk, byteChunk.toString());
                        cache.put(clone, byteChunkEntry);
                        return byteChunkEntry.value;
                    } catch (CloneNotSupportedException e) {
                        //Can't happen.
                    }
                }
            }
        }
        return byteChunk.toString();
    }

    private class ByteChunkEntry {
        private ByteChunk byteChunk;
        private String value;

        public ByteChunkEntry(ByteChunk byteChunk, String value) {
            this.byteChunk = byteChunk;
            this.value = value;
        }

        @Override
        public int hashCode() {
            return value.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof ByteChunkEntry) {
                return value.equals(((ByteChunkEntry) obj).value);
            }
            return false;
        }
    }

    public int getCacheSize() {
        return cache.size();
    }

    public int getMaxStringSize() {
        return maxStringSize;
    }

    public void setMaxStringSize(int maxStringSize) {
        this.maxStringSize = maxStringSize;
    }

    public int getMaxCacheElementSize() {
        return maxCacheElementSize;
    }
}
