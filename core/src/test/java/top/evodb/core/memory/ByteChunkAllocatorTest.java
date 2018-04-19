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

import org.junit.Test;
import top.evodb.core.memory.heap.ByteChunk;
import top.evodb.core.memory.heap.ByteChunkAllocator;


import static org.junit.Assert.*;

/**
 * @author evodb
 */
public class ByteChunkAllocatorTest {

    @Test
    public void testAllocateWidth16Bytes() {
        BuddyAllocator<ByteChunk> buddyAllocator = new ByteChunkAllocator(12);
        assertEquals(16, buddyAllocator.alloc(16).getLength());
        assertNull(buddyAllocator.alloc(1));
    }

    @Test
    public void testAllocateWidth9Bytes() {
        BuddyAllocator<ByteChunk> buddyAllocator = new ByteChunkAllocator(16);
        assertEquals(16, buddyAllocator.alloc(9).getLength());
        assertNull(buddyAllocator.alloc(1));
    }

    @Test
    public void testAllocateWidth3Bytes() {
        BuddyAllocator<ByteChunk> buddyAllocator = new ByteChunkAllocator(16);
        assertEquals(4, buddyAllocator.alloc(3).getLength());
        assertEquals(1, buddyAllocator.alloc(1).getLength());
    }

    @Test
    public void testAllocateWidth5Bytes() {
        BuddyAllocator<ByteChunk> buddyAllocator = new ByteChunkAllocator(16);
        assertEquals(8, buddyAllocator.alloc(5).getLength());
        assertEquals(1, buddyAllocator.alloc(1).getLength());
    }

    @Test
    public void testAllocateWidth6Bytes() {
        BuddyAllocator<ByteChunk> buddyAllocator = new ByteChunkAllocator(16);
        assertEquals(8, buddyAllocator.alloc(6).getLength());
        assertEquals(1, buddyAllocator.alloc(1).getLength());
    }

    @Test
    public void testAllocateWidth7Bytes() {
        BuddyAllocator<ByteChunk> buddyAllocator = new ByteChunkAllocator(16);
        assertEquals(8, buddyAllocator.alloc(7).getLength());
        assertEquals(1, buddyAllocator.alloc(1).getLength());
    }

    @Test
    public void testAllocateWidth10Bytes() {
        BuddyAllocator<ByteChunk> buddyAllocator = new ByteChunkAllocator(16);
        assertEquals(16, buddyAllocator.alloc(10).getLength());
        assertNull(buddyAllocator.alloc(1));
    }

    @Test
    public void testAllocateWidth8Bytes() {
        BuddyAllocator<ByteChunk> buddyAllocator = new ByteChunkAllocator(16);
        assertEquals(8, buddyAllocator.alloc(8).getLength());
        assertEquals(8, buddyAllocator.alloc(8).getLength());
        assertNull(buddyAllocator.alloc(8));
    }

    @Test
    public void testAllocateWidth4Bytes() {
        BuddyAllocator<ByteChunk> buddyAllocator = new ByteChunkAllocator(16);
        assertEquals(4, buddyAllocator.alloc(4).getLength());
        assertEquals(4, buddyAllocator.alloc(4).getLength());
        assertEquals(4, buddyAllocator.alloc(4).getLength());
        assertEquals(4, buddyAllocator.alloc(4).getLength());
        assertNull(buddyAllocator.alloc(4));
    }

    @Test
    public void testAllocateWidth1Byte() {
        BuddyAllocator<ByteChunk> buddyAllocator = new ByteChunkAllocator(16);
        assertEquals(1, buddyAllocator.alloc(1).getLength());
        assertEquals(1, buddyAllocator.alloc(1).getLength());
        assertEquals(1, buddyAllocator.alloc(1).getLength());
        assertEquals(1, buddyAllocator.alloc(1).getLength());

        assertEquals(1, buddyAllocator.alloc(1).getLength());
        assertEquals(1, buddyAllocator.alloc(1).getLength());
        assertEquals(1, buddyAllocator.alloc(1).getLength());
        assertEquals(1, buddyAllocator.alloc(1).getLength());

        assertEquals(1, buddyAllocator.alloc(1).getLength());
        assertEquals(1, buddyAllocator.alloc(1).getLength());
        assertEquals(1, buddyAllocator.alloc(1).getLength());
        assertEquals(1, buddyAllocator.alloc(1).getLength());

        assertEquals(1, buddyAllocator.alloc(1).getLength());
        assertEquals(1, buddyAllocator.alloc(1).getLength());
        assertEquals(1, buddyAllocator.alloc(1).getLength());
        assertEquals(1, buddyAllocator.alloc(1).getLength());

        assertNull(buddyAllocator.alloc(1));
    }

    @Test
    public void testAllocate() {
        BuddyAllocator<ByteChunk> buddyAllocator = new ByteChunkAllocator(16);
        ByteChunk byteChunk = buddyAllocator.alloc(8);
        assertEquals(0, byteChunk.getStart());
        assertEquals(7, byteChunk.getEnd());

        byteChunk = buddyAllocator.alloc(8);
        assertEquals(8, byteChunk.getStart());
        assertEquals(15, byteChunk.getEnd());
    }

    @Test
    public void testFree() {
        BuddyAllocator<ByteChunk> buddyAllocator = new ByteChunkAllocator(16);
        ByteChunk byteChunk1 = buddyAllocator.alloc(4);
        ByteChunk byteChunk2 = buddyAllocator.alloc(4);
        ByteChunk byteChunk3 = buddyAllocator.alloc(4);
        ByteChunk byteChunk4 = buddyAllocator.alloc(4);

        byteChunk1.recycle();
        byteChunk2.recycle();
        byteChunk3.recycle();
        byteChunk4.recycle();

        assertEquals(true, byteChunk1.isRecyled());
        assertEquals(true, byteChunk2.isRecyled());
        assertEquals(true, byteChunk3.isRecyled());
        assertEquals(true, byteChunk4.isRecyled());
    }

    @Test
    public void testAllocFromCache() {
        BuddyAllocator<ByteChunk> buddyAllocator = new ByteChunkAllocator(16);
        ByteChunk byteChunk = buddyAllocator.alloc(16);
        byteChunk.recycle();
        ByteChunk byteChunk1 = buddyAllocator.alloc(16);
        assertTrue(byteChunk == byteChunk1);
    }


    @Test(expected = IllegalStateException.class)
    public void testUserRecyleByteChunk() {
        BuddyAllocator<ByteChunk> buddyAllocator = new ByteChunkAllocator(16);
        ByteChunk byteChunk = buddyAllocator.alloc(16);
        byteChunk.recycle();

        byteChunk.setOffset(11);
    }

}
