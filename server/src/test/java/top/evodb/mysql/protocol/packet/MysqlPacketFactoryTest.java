/*
 * Copyright 2017-2018 The Evodb Project
 *
 * The Evodb Project licenses this file to you under the Apache License,
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

package top.evodb.mysql.protocol.packet;

import org.junit.Test;
import top.evodb.buffer.AdjustableProtocolBufferAllocator;
import top.evodb.buffer.ProtocolBuffer;
import top.evodb.buffer.ProtocolBufferAllocator;
import top.evodb.exception.MysqlPacketFactoryException;
import top.evodb.mysql.protocol.CapabilityFlags;
import top.evodb.mysql.protocol.ServerStatus;


import static org.junit.Assert.*;

/**
 * @author evodb
 */
public class MysqlPacketFactoryTest {

    private static final int CHUNK_SIZE = 15;
    private ProtocolBufferAllocator allocator = new AdjustableProtocolBufferAllocator(CHUNK_SIZE);
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
    public void testGetMysqlPacketWithProtocolBuffer() throws MysqlPacketFactoryException {
        int capablityFlags = 0;
        capablityFlags |= CapabilityFlags.CLIENT_SESSION_TRACK;
        OKPacket okPacket = factory.getMysqlPacket(MysqlPacket.OK_PACKET);
        okPacket.info = "test";
        okPacket.capabilityFlags = capablityFlags;
        okPacket.statusFlag = ServerStatus.SERVER_SESSION_STATE_CHANGED;
        okPacket.sessionStateChanges = "session state change";
        ProtocolBuffer protocolBuffer = okPacket.write();

        okPacket = factory.getMysqlPacket(protocolBuffer, 0);
        okPacket.capabilityFlags = capablityFlags;
        okPacket.read();
        assertEquals("test", okPacket.info);

        assertEquals(MysqlPacket.OK_PACKET, okPacket.getCmd());
        assertEquals(0, okPacket.getSequenceId());
        assertEquals(33, okPacket.getPayloadLength());
        assertNotNull(okPacket.getPayload());
    }

}
