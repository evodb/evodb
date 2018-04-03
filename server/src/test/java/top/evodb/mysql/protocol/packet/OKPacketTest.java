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

import top.evodb.buffer.AdjustableProtocolBufferAllocator;
import top.evodb.buffer.ProtocolBufferAllocator;
import top.evodb.exception.MysqlPacketFactoryException;
import top.evodb.mysql.protocol.CapabilityFlags;
import top.evodb.mysql.protocol.ServerStatus;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author evodb
 */
public class OKPacketTest {
    private static final int CHUNK_SIZE = 15;
    private ProtocolBufferAllocator allocator = new AdjustableProtocolBufferAllocator(CHUNK_SIZE);
    private MysqlPacketFactory factory = new MysqlPacketFactory(allocator);

    @Test
    public void testWrite() throws MysqlPacketFactoryException {
        int capablityFlags = 0;
        capablityFlags |= CapabilityFlags.CLIENT_PROTOCOL_41;
        OKPacket okPacket = factory.getMysqlPacket(MysqlPacket.OK_PACKET);
        okPacket.info = "test";
        okPacket.capabilityFlags = capablityFlags;

        okPacket.write();
    }

    @Test
    public void testWriteWithClientTranscations() throws MysqlPacketFactoryException {
        int capablityFlags = 0;
        capablityFlags |= CapabilityFlags.CLIENT_TRANSACTIONS;
        OKPacket okPacket = factory.getMysqlPacket(MysqlPacket.OK_PACKET);
        okPacket.info = "test";
        okPacket.capabilityFlags = capablityFlags;

        okPacket.write();
    }

    @Test
    public void testWriteWithSessionTrackAndServerStateChange() throws MysqlPacketFactoryException {
        int capablityFlags = 0;
        capablityFlags |= CapabilityFlags.CLIENT_SESSION_TRACK;
        OKPacket okPacket = factory.getMysqlPacket(MysqlPacket.OK_PACKET);
        okPacket.info = "test";
        okPacket.capabilityFlags = capablityFlags;
        okPacket.statusFlag = ServerStatus.SERVER_SESSION_STATE_CHANGED;
        okPacket.sessionStateChanges = "session state change";
        okPacket.write();
    }

    @Test
    public void testRead() throws MysqlPacketFactoryException {
        int capablityFlags = 0;
        capablityFlags |= CapabilityFlags.CLIENT_PROTOCOL_41;
        capablityFlags |= CapabilityFlags.CLIENT_SESSION_TRACK;

        OKPacket okPacket = factory.getMysqlPacket(MysqlPacket.OK_PACKET);
        okPacket.setSequenceId((byte) 120);
        okPacket.capabilityFlags = capablityFlags;
        okPacket.affectedRows = 101;
        okPacket.lastInsertId = 10083;
        okPacket.statusFlag = ServerStatus.SERVER_SESSION_STATE_CHANGED;
        okPacket.warnings = 200;
        okPacket.info = "test";
        okPacket.sessionStateChanges = "session state change";
        okPacket.write();

        okPacket.read();

        assertEquals(120, okPacket.sequenceId);
        assertEquals(capablityFlags, okPacket.capabilityFlags);
        assertEquals(101, okPacket.affectedRows);
        assertEquals(10083, okPacket.lastInsertId);
        assertEquals(ServerStatus.SERVER_SESSION_STATE_CHANGED, okPacket.statusFlag);
        assertEquals(200, okPacket.warnings);
        assertEquals("test", okPacket.info);
        assertEquals("session state change", okPacket.sessionStateChanges);
    }

    @Test
    public void testReadWithClientTransactions() throws MysqlPacketFactoryException {
        int capablityFlags = 0;
        capablityFlags |= CapabilityFlags.CLIENT_TRANSACTIONS;

        OKPacket okPacket = factory.getMysqlPacket(MysqlPacket.OK_PACKET);
        okPacket.setSequenceId((byte) 120);
        okPacket.capabilityFlags = capablityFlags;
        okPacket.affectedRows = 101;
        okPacket.lastInsertId = 10083;
        okPacket.statusFlag = 0;
        okPacket.warnings = 200;
        okPacket.info = "test";
        okPacket.sessionStateChanges = "session state change";
        okPacket.write();

        okPacket.read();

        assertEquals(120, okPacket.sequenceId);
        assertEquals(capablityFlags, okPacket.capabilityFlags);
        assertEquals(101, okPacket.affectedRows);
        assertEquals(10083, okPacket.lastInsertId);
        assertEquals(0, okPacket.statusFlag);
        assertEquals(200, okPacket.warnings);
        assertEquals("test", okPacket.info);
        assertEquals("session state change", okPacket.sessionStateChanges);
    }
}
