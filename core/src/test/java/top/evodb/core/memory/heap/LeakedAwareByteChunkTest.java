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

import static org.junit.Assert.assertEquals;


import org.junit.Test;
import top.evodb.core.memory.BuddyAllocator;
import top.evodb.core.util.MemoryLeakDetector;

/**
 * @author ynfeng
 */
public class LeakedAwareByteChunkTest {

    @Test
    public void testEequals() {
        BuddyAllocator<ByteChunk> buddyAllocator = new ByteChunkAllocator(32);
        ((ByteChunkAllocator) buddyAllocator).getMemoryLeakDetector().setDetectLevel(MemoryLeakDetector.DetectLevel.HIGH);
        LeakedAwareByteChunk leakedAwareByteChunk = (LeakedAwareByteChunk) buddyAllocator.alloc(10);
        byte[] bytes = {1, 2, 3, 4, 5, 6, 7, 8, 9, 0};
        leakedAwareByteChunk.append(bytes, 0, bytes.length);
        ByteChunk unwarp = leakedAwareByteChunk.unwarp();
        assertEquals(unwarp, leakedAwareByteChunk);
        assertEquals(leakedAwareByteChunk, unwarp);
        assertEquals(15, leakedAwareByteChunk.getEnd());
    }

    @Test
    public void testOffset() {
        BuddyAllocator<ByteChunk> buddyAllocator = new ByteChunkAllocator(32);
        ((ByteChunkAllocator) buddyAllocator).getMemoryLeakDetector().setDetectLevel(MemoryLeakDetector.DetectLevel.HIGH);
        LeakedAwareByteChunk leakedAwareByteChunk = (LeakedAwareByteChunk) buddyAllocator.alloc(10);
        leakedAwareByteChunk.setOffset(-1);
        assertEquals(0, leakedAwareByteChunk.getOffset());
        leakedAwareByteChunk.setOffset(11);
        assertEquals(9, leakedAwareByteChunk.getOffset());
        leakedAwareByteChunk.setOffset(7);
        assertEquals(7, leakedAwareByteChunk.getOffset());
    }
}
