/*
 * Copyright 2017-2018 The Evodb Project
 *
 * The Evolution Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 */

package top.evodb.buffer;

import org.junit.Test;
import top.evodb.exception.MysqlPacketFactoryException;
import top.evodb.mysql.protocol.ServerStatus;
import top.evodb.mysql.protocol.packet.MysqlPacket;
import top.evodb.mysql.protocol.packet.MysqlPacketFactory;
import top.evodb.mysql.protocol.packet.OKPacket;

import static org.junit.Assert.*;

/**
 * @author evodb
 */
public class SimplePacketIteratorTest {

    private static final int CHUNK_SIZE = 15;
    private ProtocolBufferAllocator allocator = new AdjustableProtocolBufferAllocator(CHUNK_SIZE);
    private MysqlPacketFactory factory = new MysqlPacketFactory(allocator);

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
    public void testHasPacketWithFullPacket() throws MysqlPacketFactoryException {
        OKPacket okPacket = factory.getMysqlPacket(MysqlPacket.OK_PACKET);
        okPacket.setSequenceId((byte) 120);
        okPacket.affectedRows = 101;
        okPacket.lastInsertId = 10083;
        okPacket.statusFlag = ServerStatus.SERVER_SESSION_STATE_CHANGED;
        okPacket.warnings = 200;
        okPacket.info = "test";
        okPacket.sessionStateChanges = "session state change";
        ProtocolBuffer protocolBuffer = okPacket.write();

        PacketIterator packetIterator = protocolBuffer.packetIterator();
        assertTrue(packetIterator.hasPacket());
    }

    @Test
    public void testHasPacketWithHalfPacket() throws MysqlPacketFactoryException {
        ProtocolBuffer protocolBuffer = allocator.allocate();
        protocolBuffer.writeFixInt(3, 14);
        protocolBuffer.writeByte((byte) 0);
        protocolBuffer.writeByte((byte) 0x00);
        protocolBuffer.writeFixString("t");
        PacketIterator packetIterator = protocolBuffer.packetIterator();
        assertTrue(packetIterator.hasPacket());
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
    public void testNextPacketWithFullPacket() throws MysqlPacketFactoryException {
        ProtocolBuffer protocolBuffer = allocator.allocate();
        /* packet 1 */
        protocolBuffer.writeFixInt(3, 7);
        protocolBuffer.writeByte((byte) 0);
        protocolBuffer.writeByte((byte) 0x00);
        protocolBuffer.writeFixString("hello");

        /* packet 2 */
        protocolBuffer.writeFixInt(3, 8);
        protocolBuffer.writeByte((byte) 0);
        protocolBuffer.writeByte((byte) 0x00);
        protocolBuffer.writeFixString("hello2");

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

    @Test
    public void testNextPacketWithFullPacketAndHalfPacket() throws MysqlPacketFactoryException {
        ProtocolBuffer protocolBuffer = allocator.allocate();
        /* packet 1 */
        protocolBuffer.writeFixInt(3, 7);
        protocolBuffer.writeByte((byte) 0);
        protocolBuffer.writeByte((byte) 0x00);
        protocolBuffer.writeFixString("hello");

        /* packet 2,half */
        protocolBuffer.writeFixInt(3, 10);
        protocolBuffer.writeByte((byte) 0);
        protocolBuffer.writeByte((byte) 0x00);
        protocolBuffer.writeFixString("hello2");

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

