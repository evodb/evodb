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
import static org.junit.Assert.assertFalse;

import org.junit.Test;
import top.evodb.core.memory.BuddyAllocator;

/**
 * @author evodb
 */
public class ByteChunkTest {

    @Test
    public void testOffset() {
        BuddyAllocator<ByteChunk> buddyAllocator = new ByteChunkAllocator(16);
        ByteChunk byteChunk = buddyAllocator.alloc(10);
        byteChunk.setOffset(-1);
        assertEquals(0, byteChunk.getOffset());
        byteChunk.setOffset(11);
        assertEquals(9, byteChunk.getOffset());
        byteChunk.setOffset(7);
        assertEquals(7, byteChunk.getOffset());
    }

    @Test
    public void testAppend() {
        BuddyAllocator<ByteChunk> buddyAllocator = new ByteChunkAllocator(32);
        ByteChunk byteChunk = buddyAllocator.alloc(10);
        byte[] bytes = {1, 2, 3, 4, 5, 6, 7, 8, 9, 0};
        byteChunk.append(bytes, 0, bytes.length);

    }

    @Test
    public void testEquals() {
        BuddyAllocator<ByteChunk> buddyAllocator = new ByteChunkAllocator(32);
        ByteChunk byteChunk = buddyAllocator.alloc(10);
        byte[] bytes = {1, 2, 3, 4, 5, 6, 7, 8, 9, 0};
        byteChunk.append(bytes, 0, bytes.length);

        ByteChunk byteChunk1 = buddyAllocator.alloc(10);
        byteChunk1.append(bytes, 0, bytes.length);

        assertEquals(byteChunk, byteChunk1);
    }

    @Test
    public void testEqualsWith11Bytes() {
        BuddyAllocator<ByteChunk> buddyAllocator = new ByteChunkAllocator(32);
        ByteChunk byteChunk = buddyAllocator.alloc(10);
        byte[] bytes = {1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 1};
        byteChunk.append(bytes, 0, bytes.length);

        ByteChunk byteChunk1 = buddyAllocator.alloc(10);
        byteChunk1.append(bytes, 0, bytes.length);

        assertEquals(byteChunk, byteChunk1);
    }

    @Test
    public void testEqualsWithDifferentData() {
        BuddyAllocator<ByteChunk> buddyAllocator = new ByteChunkAllocator(32);
        ByteChunk byteChunk = buddyAllocator.alloc(11);
        byte[] bytes = {1, 2, 3, 4, 5, 6, 7, 8, 9, 1, 2};
        byteChunk.append(bytes, 0, bytes.length);

        byte[] bytes1 = {1, 2, 3, 4, 5, 6, 7, 8, 9, 1, 3};
        ByteChunk byteChunk1 = buddyAllocator.alloc(11);
        byteChunk1.append(bytes1, 0, bytes1.length);

        assertFalse(byteChunk.equals(byteChunk1));
        assertFalse(byteChunk.equals(bytes1));
    }

    @Test
    public void testEqualsWithDifferentLength() {
        BuddyAllocator<ByteChunk> buddyAllocator = new ByteChunkAllocator(32);
        ByteChunk byteChunk = buddyAllocator.alloc(11);
        byte[] bytes = {1, 2, 3, 4, 5, 6, 7, 8, 9, 1, 2};
        byteChunk.append(bytes, 0, bytes.length);

        byte[] bytes1 = {1, 2, 3, 4, 5, 6, 7, 8, 9, 1};
        ByteChunk byteChunk1 = buddyAllocator.alloc(10);
        byteChunk1.append(bytes1, 0, bytes1.length);

        assertFalse(byteChunk.equals(byteChunk1));
    }
}
