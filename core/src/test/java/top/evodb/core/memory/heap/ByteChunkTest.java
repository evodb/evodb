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
import static org.junit.Assert.assertTrue;


import org.junit.Test;

/**
 * @author evodb
 */
public class ByteChunkTest {

    @Test
    public void testAppend() {
        byte[] bytes = new byte[10];
        ByteChunk byteChunk = new ByteChunk(null, bytes, 0, bytes.length, 0, 0);
        byteChunk.append("1234567890".getBytes(), 0, 10);
        assertEquals(10, byteChunk.getEnd());
        assertEquals(0, byteChunk.getStart());
        assertEquals(11, byteChunk.getLength());
    }

    @Test
    public void testEquals() {
        byte[] bytes = new byte[10];
        ByteChunk byteChunk = new ByteChunk(null, bytes, 0, bytes.length, 0, 0);
        byteChunk.append("1234567890".getBytes(), 0, 10);

        byte[] otherBytes = new byte[10];
        ByteChunk otherByteChunk = new ByteChunk(null, otherBytes, 0, otherBytes.length, 0, 0);
        otherByteChunk.append("1234567890".getBytes(), 0, 10);
        assertTrue(otherByteChunk.equals(byteChunk));
    }

    @Test
    public void testEqualsWithDifferentLength() {
        byte[] bytes = new byte[10];
        ByteChunk byteChunk = new ByteChunk(null, bytes, 0, bytes.length, 0, 0);
        byteChunk.append("1234567890".getBytes(), 0, 10);

        byte[] otherBytes = new byte[11];
        ByteChunk otherByteChunk = new ByteChunk(null, otherBytes, 0, otherBytes.length, 0, 0);
        otherByteChunk.append("12345678901".getBytes(), 0, 11);
        assertFalse(otherByteChunk.equals(byteChunk));
    }

    @Test
    public void testEqualsWithDifferentContent() {
        byte[] bytes = new byte[10];
        ByteChunk byteChunk = new ByteChunk(null, bytes, 0, bytes.length, 0, 0);
        byteChunk.append("1234567890".getBytes(), 0, 10);

        byte[] otherBytes = new byte[10];
        ByteChunk otherByteChunk = new ByteChunk(null, otherBytes, 0, otherBytes.length, 0, 0);
        otherByteChunk.append("1234567891".getBytes(), 0, 10);
        assertFalse(otherByteChunk.equals(byteChunk));
    }

    @Test
    public void testEqualsWithDifferentSameInstance() {
        byte[] bytes = new byte[10];
        ByteChunk byteChunk = new ByteChunk(null, bytes, 0, bytes.length, 0, 0);
        byteChunk.append("1234567890".getBytes(), 0, 10);
        assertTrue(byteChunk.equals(byteChunk));
    }

    @Test
    public void testEqualsWithOtherType() {
        byte[] bytes = new byte[10];
        ByteChunk byteChunk = new ByteChunk(null, bytes, 0, bytes.length, 0, 0);
        byteChunk.append("1234567890".getBytes(), 0, 10);
        assertFalse(byteChunk.equals("1234567890"));
    }

    @Test
    public void testSetOffset() {
        byte[] bytes = new byte[10];
        ByteChunk byteChunk = new ByteChunk(null, bytes, 0, bytes.length, 0, 0);
        byteChunk.append("1234567890".getBytes(), 0, 10);
        byteChunk.setOffset(5);
        assertEquals(5, byteChunk.getOffset());
    }

    @Test
    public void testSetOffsetWithStartBounds() {
        byte[] bytes = new byte[10];
        ByteChunk byteChunk = new ByteChunk(null, bytes, 0, bytes.length, 0, 0);
        byteChunk.append("1234567890".getBytes(), 0, 10);
        byteChunk.setOffset(-1);
        assertEquals(0, byteChunk.getOffset());
    }

    @Test
    public void testSetOffsetWithEndBounds() {
        byte[] bytes = new byte[10];
        ByteChunk byteChunk = new ByteChunk(null, bytes, 0, bytes.length, 0, 0);
        byteChunk.append("1234567890".getBytes(), 0, 10);
        byteChunk.setOffset(byteChunk.getEnd() + 1);
        assertEquals(10, byteChunk.getOffset());
    }

    @Test
    public void testReuse() {
        byte[] bytes = new byte[10];
        ByteChunk byteChunk = new ByteChunk(null, bytes, 0, bytes.length, 0, 0);
        byteChunk.append("1234567890".getBytes(), 0, 10);
        byteChunk.reuse(0);
        assertEquals(0, byteChunk.getStart());
        assertEquals(10, byteChunk.getEnd());
        assertEquals(0, byteChunk.getOffset());
    }
}
