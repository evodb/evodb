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

import org.junit.Assert;
import org.junit.Test;
import top.evodb.core.memory.direct.AdjustableProtocolBufferAllocator;
import top.evodb.core.memory.direct.ProtocolBufferAllocator;
import top.evodb.core.protocol.MysqlPacket;
import top.evodb.server.exception.MysqlPacketFactoryException;
import top.evodb.server.mysql.CapabilityFlags;
import top.evodb.server.mysql.ServerStatus;

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
        capablityFlags |= CapabilityFlags.PROTOCOL_41;
        OKPacket okPacket = factory.getMysqlPacket(MysqlPacket.OK_PACKET);
        okPacket.info = "test";
        okPacket.capabilityFlags = capablityFlags;

        okPacket.write();
    }

    @Test
    public void testWriteWithClientTranscations() throws MysqlPacketFactoryException {
        int capablityFlags = 0;
        capablityFlags |= CapabilityFlags.TRANSACTIONS;
        OKPacket okPacket = factory.getMysqlPacket(MysqlPacket.OK_PACKET);
        okPacket.info = "test";
        okPacket.capabilityFlags = capablityFlags;

        okPacket.write();
    }

    @Test
    public void testWriteWithSessionTrackAndServerStateChange() throws MysqlPacketFactoryException {
        int capablityFlags = 0;
        capablityFlags |= CapabilityFlags.SESSION_TRACK;
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
        capablityFlags |= CapabilityFlags.PROTOCOL_41;
        capablityFlags |= CapabilityFlags.SESSION_TRACK;

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

        Assert.assertEquals(120, okPacket.sequenceId);
        Assert.assertEquals(capablityFlags, okPacket.capabilityFlags);
        Assert.assertEquals(101, okPacket.affectedRows);
        Assert.assertEquals(10083, okPacket.lastInsertId);
        Assert.assertEquals(ServerStatus.SERVER_SESSION_STATE_CHANGED, okPacket.statusFlag);
        Assert.assertEquals(200, okPacket.warnings);
        Assert.assertEquals("test", okPacket.info);
        Assert.assertEquals("session state change", okPacket.sessionStateChanges);
    }

    @Test
    public void testReadWithClientTransactions() throws MysqlPacketFactoryException {
        int capablityFlags = 0;
        capablityFlags |= CapabilityFlags.TRANSACTIONS;

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

        Assert.assertEquals(120, okPacket.sequenceId);
        Assert.assertEquals(capablityFlags, okPacket.capabilityFlags);
        Assert.assertEquals(101, okPacket.affectedRows);
        Assert.assertEquals(10083, okPacket.lastInsertId);
        Assert.assertEquals(0, okPacket.statusFlag);
        Assert.assertEquals(200, okPacket.warnings);
        Assert.assertEquals("test", okPacket.info);
        Assert.assertEquals("session state change", okPacket.sessionStateChanges);
    }
}
