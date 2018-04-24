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

package top.evodb.core.memory.protocol;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;


import org.junit.Test;
import top.evodb.core.memory.heap.ByteChunk;
import top.evodb.core.memory.heap.ByteChunkAllocator;

/**
 * @author evodb
 */
public class SimplePacketIteratorTest {
    private ByteChunkAllocator byteChunkAllocator = new ByteChunkAllocator(1024 * 1024);
    private static final int CHUNK_SIZE = 15;
    private ProtocolBufferAllocator allocator = new AdjustableProtocolBufferAllocator(CHUNK_SIZE,byteChunkAllocator);

    @Test
    public void testGetDefaultPacketIterator() {
        ProtocolBuffer protocolBuffer = allocator.allocate();
        PacketIterator packetIterator = protocolBuffer.packetIterator();
        assertNotNull(packetIterator);
    }

    @Test
    public void testGetNamedPacketIterator() {
        ProtocolBuffer protocolBuffer = allocator.allocate();
        PacketIterator packetIterator = protocolBuffer.packetIterator("other");
        assertNotNull(packetIterator);
    }

    @Test
    public void testGetPacketIteratorWithClear() {
        ProtocolBuffer protocolBuffer = allocator.allocate();
        protocolBuffer.packetIterator("other");
        protocolBuffer.packetIterator();
        protocolBuffer.clear();
    }

    @Test
    public void testHasPacketWithFullPacket() {
        ByteChunk byteChunk = byteChunkAllocator.alloc(9);
        byteChunk.append("123456789".getBytes(),0,9);
        ProtocolBuffer protocolBuffer = allocator.allocate();
        protocolBuffer.writeFixInt(3, 10);
        protocolBuffer.writeByte((byte) 0);
        protocolBuffer.writeFixString(byteChunk);

        PacketIterator packetIterator = protocolBuffer.packetIterator();
        assertTrue(packetIterator.hasPacket());
        byteChunk.recycle();
    }

    @Test
    public void testHasPacketWithHalfPacket() {
        ByteChunk byteChunk = byteChunkAllocator.alloc(1);
        byteChunk.append("t".getBytes(),0,1);

        ProtocolBuffer protocolBuffer = allocator.allocate();
        protocolBuffer.writeFixInt(3, 14);
        protocolBuffer.writeByte((byte) 0);
        protocolBuffer.writeByte((byte) 0x00);
        protocolBuffer.writeFixString(byteChunk);
        PacketIterator packetIterator = protocolBuffer.packetIterator();
        assertTrue(packetIterator.hasPacket());
        byteChunk.recycle();
    }

    @Test
    public void testHasPacketWithNoPacket() {
        ProtocolBuffer protocolBuffer = allocator.allocate();
        protocolBuffer.writeFixInt(3, 14);
        protocolBuffer.writeByte((byte) 0);

        PacketIterator packetIterator = protocolBuffer.packetIterator();
        assertFalse(packetIterator.hasPacket());
    }

    @Test
    public void testNextPacketWithFullPacket() {
        ByteChunk byteChunk = byteChunkAllocator.alloc(5);
        byteChunk.append("t".getBytes(),0,1);


        ProtocolBuffer protocolBuffer = allocator.allocate();
        /* packet 1 */
        protocolBuffer.writeFixInt(3, 7);
        protocolBuffer.writeByte((byte) 0);
        protocolBuffer.writeByte((byte) 0x00);
        protocolBuffer.writeFixString(byteChunk);

        /* packet 2 */
        protocolBuffer.writeFixInt(3, 8);
        protocolBuffer.writeByte((byte) 0);
        protocolBuffer.writeByte((byte) 0x00);
        protocolBuffer.writeFixString(byteChunk);

        PacketIterator packetIterator = protocolBuffer.packetIterator();
        assertTrue(packetIterator.hasPacket());

        long packetDescriptor = packetIterator.nextPacket();
        assertFalse(PacketDescriptor.NONE == packetDescriptor);

        assertTrue(packetIterator.hasPacket());
        packetDescriptor = packetIterator.nextPacket();
        assertFalse(PacketDescriptor.NONE == packetDescriptor);

        assertFalse(packetIterator.hasPacket());
        packetDescriptor = packetIterator.nextPacket();
        assertTrue(PacketDescriptor.NONE == packetDescriptor);

        packetIterator.reset();

        byteChunk.recycle();
    }

    @Test
    public void testNextPacketWithFullPacketAndHalfPacket() {
        ByteChunk byteChunk = byteChunkAllocator.alloc(5);
        byteChunk.append("hello".getBytes(),0,9);

        ProtocolBuffer protocolBuffer = allocator.allocate();
        /* packet 1 */
        protocolBuffer.writeFixInt(3, 7);
        protocolBuffer.writeByte((byte) 0);
        protocolBuffer.writeByte((byte) 0x00);
        protocolBuffer.writeFixString(byteChunk);

        /* packet 2,half */
        protocolBuffer.writeFixInt(3, 10);
        protocolBuffer.writeByte((byte) 0);
        protocolBuffer.writeByte((byte) 0x00);
        protocolBuffer.writeFixString(byteChunk);

        PacketIterator packetIterator = protocolBuffer.packetIterator();
        assertTrue(packetIterator.hasPacket());

        long packetDescriptor = packetIterator.nextPacket();
        assertFalse(PacketDescriptor.NONE == packetDescriptor);

        assertTrue(packetIterator.hasPacket());
        packetDescriptor = packetIterator.nextPacket();
        assertFalse(PacketDescriptor.NONE == packetDescriptor);

        assertFalse(packetIterator.hasPacket());
        packetDescriptor = packetIterator.nextPacket();
        assertTrue(PacketDescriptor.NONE == packetDescriptor);

        packetIterator.reset();
    }
}

