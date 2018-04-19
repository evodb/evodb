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

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import top.evodb.core.memory.direct.AdjustableProtocolBufferAllocator;
import top.evodb.core.memory.direct.ProtocolBufferAllocator;
import top.evodb.server.ServerContext;
import top.evodb.server.exception.MysqlPacketFactoryException;
import top.evodb.server.mysql.Constants;
import top.evodb.server.mysql.ServerStatus;

/**
 * @author evodb
 */
public class HandshakeV10PacketTest {
    private static final int CHUNK_SIZE = 15;
    private ProtocolBufferAllocator allocator = new AdjustableProtocolBufferAllocator(CHUNK_SIZE);
    private MysqlPacketFactory factory = new MysqlPacketFactory(allocator);

    @Test
    public void testWrite() throws MysqlPacketFactoryException {
        HandshakeV10Packet handshakeV10Packet = factory.getMysqlPacket(HandshakeV10Packet.class);
        handshakeV10Packet.capabilityFlags = Constants.SERVER_CAPABILITY;
        handshakeV10Packet.statusFlag = ServerStatus.SERVER_STATUS_AUTOCOMMIT;
        handshakeV10Packet.connectionId = ServerContext.getContext().newConnectId();
        handshakeV10Packet.characterSet = ServerContext.getContext().getCharset().charsetIndex;
        handshakeV10Packet.serverVersion = ServerContext.getContext().getVersion().getServerVersion();
        handshakeV10Packet.protocolVersion = ServerContext.getContext().getVersion().getProtocolVersion();
        handshakeV10Packet.write();
    }

    @Test
    public void testRead() throws MysqlPacketFactoryException {
        HandshakeV10Packet handshakeV10Packet = factory.getMysqlPacket(HandshakeV10Packet.class);
        int connectionId = ServerContext.getContext().newConnectId();
        handshakeV10Packet.capabilityFlags = Constants.SERVER_CAPABILITY;
        handshakeV10Packet.statusFlag = ServerStatus.SERVER_STATUS_AUTOCOMMIT;
        handshakeV10Packet.connectionId = connectionId;
        handshakeV10Packet.characterSet = ServerContext.getContext().getCharset().charsetIndex;
        handshakeV10Packet.serverVersion = ServerContext.getContext().getVersion().getServerVersion();
        handshakeV10Packet.protocolVersion = ServerContext.getContext().getVersion().getProtocolVersion();
        handshakeV10Packet.write();

        handshakeV10Packet.read();
        assertEquals(Constants.SERVER_CAPABILITY, handshakeV10Packet.capabilityFlags);
        assertEquals(ServerStatus.SERVER_STATUS_AUTOCOMMIT, handshakeV10Packet.statusFlag);
        assertEquals(connectionId, handshakeV10Packet.connectionId);
        assertEquals(Constants.AUTH_PLUGIN_NAME, handshakeV10Packet.authPluginName);
        assertEquals(ServerContext.getContext().getCharset().charsetIndex, handshakeV10Packet.characterSet);
        assertEquals(ServerContext.getContext().getVersion().getServerVersion(), handshakeV10Packet.serverVersion);
        assertEquals(ServerContext.getContext().getVersion().getProtocolVersion(), handshakeV10Packet.protocolVersion);

    }
}
