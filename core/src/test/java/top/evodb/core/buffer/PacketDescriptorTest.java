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

package top.evodb.core.buffer;

import static org.junit.Assert.assertEquals;


import org.junit.Test;

/**
 * @author evodb
 */
public class PacketDescriptorTest {

    private static final int CHUNK_SIZE = 15;
    private ProtocolBufferAllocator allocator = new AdjustableProtocolBufferAllocator(CHUNK_SIZE);


    @Test
    public void testPacketDescriptorWithFullAndShortPacket() {
        ProtocolBuffer protocolBuffer = allocator.allocate();
        /* packet 1 */
        protocolBuffer.writeFixInt(3, 7);
        protocolBuffer.writeByte((byte) 0);
        protocolBuffer.writeByte((byte) 0x00);
        protocolBuffer.writeFixString("hello");

        /* packet 2 */
        protocolBuffer.writeFixInt(3, 10);
        protocolBuffer.writeByte((byte) 0);
        protocolBuffer.writeByte((byte) 0x02);
        protocolBuffer.writeFixString("hello2");

        PacketIterator packetIterator = protocolBuffer.packetIterator();
        long packetDescriptor = packetIterator.nextPacket();

        assertEquals(7, PacketDescriptor.getPacketLen(packetDescriptor));
        assertEquals(0, PacketDescriptor.getCommandType(packetDescriptor));
        assertEquals(PacketDescriptor.PacketType.FULL, PacketDescriptor.getPacketType(packetDescriptor));
        assertEquals(0, PacketDescriptor.getPacketStartPos(packetDescriptor));

        packetDescriptor = packetIterator.nextPacket();
        assertEquals(10, PacketDescriptor.getPacketLen(packetDescriptor));
        assertEquals(2, PacketDescriptor.getCommandType(packetDescriptor));
        assertEquals(PacketDescriptor.PacketType.HALF, PacketDescriptor.getPacketType(packetDescriptor));
        assertEquals(10, PacketDescriptor.getPacketStartPos(packetDescriptor));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetPacketTypeWithWrongDescriptor() {
        PacketDescriptor.getPacketType(Long.MAX_VALUE);
    }
}
