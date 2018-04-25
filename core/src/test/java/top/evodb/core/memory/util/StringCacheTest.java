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

package top.evodb.core.memory.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


import org.junit.Test;
import top.evodb.core.memory.heap.ByteChunk;
import top.evodb.core.memory.heap.ByteChunkAllocator;
import top.evodb.core.util.StringCache;

/**
 * @author ynfeng
 */
public class StringCacheTest {

    @Test
    public void testGetString() {
        StringCache stringCache = StringCache.newInstance(20000);

        ByteChunkAllocator buddyAllocator = new ByteChunkAllocator(16);
        ByteChunk byteChunk = buddyAllocator.alloc(4);
        byteChunk.append("test".getBytes(), 0, 4);

        String str = stringCache.getString(byteChunk);
        assertEquals("test", str);

        ByteChunk byteChunk1 = buddyAllocator.alloc(4);
        byteChunk1.append("test".getBytes(), 0, 4);
        String str1 = stringCache.getString(byteChunk1);
        assertTrue(str1 == str);
    }

    @Test
    public void testLRU() {
        StringCache stringCache = StringCache.newInstance(2);

        ByteChunkAllocator buddyAllocator = new ByteChunkAllocator(16);

        ByteChunk byteChunk = buddyAllocator.alloc(4);
        byteChunk.append("1".getBytes(), 0, 1);

        ByteChunk byteChunk1 = buddyAllocator.alloc(4);
        byteChunk1.append("2".getBytes(), 0, 1);

        ByteChunk byteChunk2 = buddyAllocator.alloc(4);
        byteChunk2.append("3".getBytes(), 0, 1);

        String str1 = stringCache.getString(byteChunk);
        String str2 = stringCache.getString(byteChunk1);
        String str3 = stringCache.getString(byteChunk2);

        stringCache.getString(byteChunk);
        stringCache.getString(byteChunk);
        stringCache.getString(byteChunk2);
        String str4 = stringCache.getString(byteChunk1);


        assertEquals(2, stringCache.getCacheSize());
        assertTrue(str2 != str4);
    }

    @Test
    public void testGetStringWithLageStr() {
        StringCache stringCache = StringCache.newInstance(2);
        stringCache.setMaxStringSize(1);
        ByteChunkAllocator buddyAllocator = new ByteChunkAllocator(16);
        ByteChunk byteChunk = buddyAllocator.alloc(2);
        byteChunk.append("12".getBytes(), 0, 2);
        stringCache.getString(byteChunk);
    }
}
