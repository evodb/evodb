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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


import org.junit.Test;
import top.evodb.core.memory.heap.ByteChunk;
import top.evodb.core.memory.heap.ByteChunkAllocator;
import top.evodb.core.util.MemoryLeakDetector;

/**
 * @author evodb
 */
public class MemoryLeakDetectorTest {

    @Test
    public void memoryLeakDetectorTestWithHighLevel() {
        ByteChunkAllocator buddyAllocator = new ByteChunkAllocator(12);
        buddyAllocator.getMemoryLeakDetector().setDetectLevel(MemoryLeakDetector.DetectLevel.HIGH);
        ByteChunk byteChunk = buddyAllocator.alloc(4);
        byteChunk = null;
        System.gc();
        try {
            Thread.sleep(1000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        buddyAllocator.alloc(4);
        assertTrue(buddyAllocator.getMemoryLeakDetector().isMemoryLeakOccurred());
    }

    @Test
    public void memoryLeakDetectorTestWithDisableLevel() {
        ByteChunkAllocator buddyAllocator = new ByteChunkAllocator(12);
        buddyAllocator.getMemoryLeakDetector().setDetectLevel(MemoryLeakDetector.DetectLevel.DISABLE);
        ByteChunk byteChunk = buddyAllocator.alloc(4);
        byteChunk = null;
        System.gc();
        try {
            Thread.sleep(1000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        buddyAllocator.alloc(4);
        assertFalse(buddyAllocator.getMemoryLeakDetector().isMemoryLeakOccurred());
    }

    @Test
    public void memoryLeakDetectorTestWithMiddleLevel() {
        ByteChunkAllocator buddyAllocator = new ByteChunkAllocator(12);
        buddyAllocator.getMemoryLeakDetector().setDetectLevel(MemoryLeakDetector.DetectLevel.MIDDLE);
        buddyAllocator.getMemoryLeakDetector().setDetectRate(1);
        ByteChunk byteChunk = buddyAllocator.alloc(4);
        byteChunk = null;
        System.gc();
        try {
            Thread.sleep(1000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        buddyAllocator.alloc(4);
        assertTrue(buddyAllocator.getMemoryLeakDetector().isMemoryLeakOccurred());
    }

    @Test
    public void memoryLeakDetectorTestWithMiddleLevelAndRate2() {
        ByteChunkAllocator buddyAllocator = new ByteChunkAllocator(12);
        buddyAllocator.getMemoryLeakDetector().setDetectLevel(MemoryLeakDetector.DetectLevel.MIDDLE);
        buddyAllocator.getMemoryLeakDetector().setDetectRate(2);
        ByteChunk byteChunk = buddyAllocator.alloc(4);
        byteChunk = null;
        System.gc();
        try {
            Thread.sleep(1000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        buddyAllocator.alloc(4);
        assertFalse(buddyAllocator.getMemoryLeakDetector().isMemoryLeakOccurred());
    }

    @Test
    public void memoryLeakDetectorTestWithCache() {
        ByteChunkAllocator buddyAllocator = new ByteChunkAllocator(12);
        buddyAllocator.getMemoryLeakDetector().setDetectLevel(MemoryLeakDetector.DetectLevel.HIGH);
        ByteChunk byteChunk = buddyAllocator.alloc(4);
        byteChunk.recycle();

        ByteChunk other = buddyAllocator.alloc(4);
        assertFalse(buddyAllocator.getMemoryLeakDetector().isMemoryLeakOccurred());
        assertTrue(other == byteChunk);
    }
}
