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

package top.evodb.server.protocol;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;


import org.junit.Assert;
import org.junit.Test;
import top.evodb.core.memory.heap.ByteChunk;
import top.evodb.core.memory.heap.ByteChunkAllocator;
import top.evodb.core.memory.protocol.AdjustableProtocolBufferAllocator;
import top.evodb.core.memory.protocol.ProtocolBuffer;
import top.evodb.core.memory.protocol.ProtocolBufferAllocator;
import top.evodb.core.protocol.MysqlPacket;
import top.evodb.server.exception.MysqlPacketFactoryException;
import top.evodb.server.mysql.CapabilityFlags;
import top.evodb.server.mysql.ServerStatus;

/**
 * @author evodb
 */
public class MysqlPacketFactoryTest {

    private static final int CHUNK_SIZE = 15;
    private ByteChunkAllocator byteChunkAllocator = new ByteChunkAllocator(1024 * 1024);
    private ProtocolBufferAllocator allocator = new AdjustableProtocolBufferAllocator(CHUNK_SIZE, byteChunkAllocator);
    private MysqlPacketFactory factory = new MysqlPacketFactory(allocator);

    @Test
    public void testGetMysqlPacket() throws MysqlPacketFactoryException {
        MysqlPacket mysqlPacket = factory.getMysqlPacket(MysqlPacket.OK_PACKET);
        assertTrue(mysqlPacket instanceof OKPacket);
    }

    @Test(expected = MysqlPacketFactoryException.class)
    public void testGetMysqlPacketWithWrongType() throws MysqlPacketFactoryException {
        factory.getMysqlPacket((byte) 0xFB);
    }

    @Test
    public void testGetMysqlPacketWithClassAndProtocolBuffer() throws MysqlPacketFactoryException {
        factory.getMysqlPacket(HandshakeResponse41Packet.class, allocator.allocate());
    }

    @Test
    public void testGetMysqlPacketWithProtocolBuffer() throws MysqlPacketFactoryException {
        ByteChunk test = byteChunkAllocator.alloc(4);
        test.append("test");
        ByteChunk sessionState = byteChunkAllocator.alloc("session state change".length());
        sessionState.append("session state change");

        int capablityFlags = 0;
        capablityFlags |= CapabilityFlags.SESSION_TRACK;
        OKPacket okPacket = factory.getMysqlPacket(MysqlPacket.OK_PACKET);
        okPacket.setInfo(test);
        okPacket.capabilityFlags = capablityFlags;
        okPacket.statusFlag = ServerStatus.SERVER_SESSION_STATE_CHANGED;
        okPacket.sessionStateChanges = sessionState;
        ProtocolBuffer protocolBuffer = okPacket.write();

        okPacket = factory.getMysqlPacket(protocolBuffer, 0);
        okPacket.capabilityFlags = capablityFlags;
        okPacket.read();
        Assert.assertEquals("test", okPacket.getInfo().toString());

        Assert.assertEquals(MysqlPacket.OK_PACKET, okPacket.getCmd());
        Assert.assertEquals(0, okPacket.getSequenceId());
        Assert.assertEquals(29, okPacket.getPayloadLength());
        assertNotNull(okPacket.getPayload());
    }

}
