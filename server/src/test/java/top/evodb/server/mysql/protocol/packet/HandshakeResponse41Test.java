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

package top.evodb.server.mysql.protocol.packet;

import static org.junit.Assert.assertEquals;


import org.junit.Test;
import top.evodb.server.buffer.AdjustableProtocolBufferAllocator;
import top.evodb.server.buffer.ProtocolBufferAllocator;
import top.evodb.server.exception.MysqlPacketFactoryException;
import top.evodb.server.mysql.Constants;
import top.evodb.server.mysql.protocol.CapabilityFlags;

/**
 * @author ynfeng
 */
public class HandshakeResponse41Test {
    private static final int CHUNK_SIZE = 15;
    private ProtocolBufferAllocator allocator = new AdjustableProtocolBufferAllocator(CHUNK_SIZE);
    private MysqlPacketFactory factory = new MysqlPacketFactory(allocator);

    @Test
    public void testWrite() throws MysqlPacketFactoryException {
        HandshakeResponse41 handshakeResponse41 = factory.getMysqlPacket(HandshakeResponse41.class);
        handshakeResponse41.capability = CapabilityFlags.CONNECT_WITH_DB;
        handshakeResponse41.maxPacketSize = MysqlPacket.LARGE_PACKET_SIZE;
        handshakeResponse41.characterSet = 8;
        handshakeResponse41.username = "root";
        handshakeResponse41.authResponse = new byte[] {0, 0, 0};
        handshakeResponse41.database = "db";
        handshakeResponse41.authPluginName = Constants.AUTH_PLUGIN_NAME;
        handshakeResponse41.write();
    }

    @Test
    public void testWriteWithSecureConnection() throws MysqlPacketFactoryException {
        HandshakeResponse41 handshakeResponse41 = factory.getMysqlPacket(HandshakeResponse41.class);
        handshakeResponse41.capability = CapabilityFlags.SECURE_CONNECTION |
            CapabilityFlags.PLUGIN_AUTH;
        handshakeResponse41.maxPacketSize = MysqlPacket.LARGE_PACKET_SIZE;
        handshakeResponse41.characterSet = 8;
        handshakeResponse41.username = "root";
        handshakeResponse41.authResponse = new byte[] {0, 0, 0};
        handshakeResponse41.database = "db";
        handshakeResponse41.authPluginName = Constants.AUTH_PLUGIN_NAME;
        handshakeResponse41.write();
    }

    @Test
    public void testWriteWithPluginAuthLenencClientData() throws MysqlPacketFactoryException {
        HandshakeResponse41 handshakeResponse41 = factory.getMysqlPacket(HandshakeResponse41.class);
        handshakeResponse41.capability = CapabilityFlags.SECURE_CONNECTION |
            CapabilityFlags.PLUGIN_AUTH_LENENC_CLIENT_DATA |
            CapabilityFlags.PLUGIN_AUTH;
        handshakeResponse41.maxPacketSize = MysqlPacket.LARGE_PACKET_SIZE;
        handshakeResponse41.characterSet = 8;
        handshakeResponse41.username = "root";
        handshakeResponse41.authResponse = new byte[] {0, 0, 0};
        handshakeResponse41.database = "db";
        handshakeResponse41.authPluginName = Constants.AUTH_PLUGIN_NAME;
        handshakeResponse41.write();
    }

    @Test
    public void testRead() throws MysqlPacketFactoryException {
        int capability = CapabilityFlags.CONNECT_WITH_DB;

        HandshakeResponse41 handshakeResponse41 = factory.getMysqlPacket(HandshakeResponse41.class);
        handshakeResponse41.capability = CapabilityFlags.CONNECT_WITH_DB;
        handshakeResponse41.maxPacketSize = MysqlPacket.LARGE_PACKET_SIZE;
        handshakeResponse41.characterSet = 8;
        handshakeResponse41.username = "root";
        handshakeResponse41.authResponse = new byte[] {0, 0, 0};
        handshakeResponse41.database = "db";
        handshakeResponse41.authPluginName = Constants.AUTH_PLUGIN_NAME;
        handshakeResponse41.write();

        handshakeResponse41.read();
        assertEquals(capability, handshakeResponse41.capability);
        assertEquals(MysqlPacket.LARGE_PACKET_SIZE, handshakeResponse41.maxPacketSize);
        assertEquals(8, handshakeResponse41.characterSet);
        assertEquals("root", handshakeResponse41.username);
        for (int i = 0; i < handshakeResponse41.authResponse.length; i++) {
            assertEquals(0, handshakeResponse41.authResponse[i]);
        }
        assertEquals(Constants.AUTH_PLUGIN_NAME, handshakeResponse41.authPluginName);
    }

    @Test
    public void testReadWithSecureConnection() throws MysqlPacketFactoryException {
        int capability = CapabilityFlags.SECURE_CONNECTION |
            CapabilityFlags.PLUGIN_AUTH;

        HandshakeResponse41 handshakeResponse41 = factory.getMysqlPacket(HandshakeResponse41.class);
        handshakeResponse41.capability = capability;
        handshakeResponse41.maxPacketSize = MysqlPacket.LARGE_PACKET_SIZE;
        handshakeResponse41.characterSet = 8;
        handshakeResponse41.username = "root";
        handshakeResponse41.authResponse = new byte[] {0, 0, 0};
        handshakeResponse41.database = "db";
        handshakeResponse41.authPluginName = Constants.AUTH_PLUGIN_NAME;
        handshakeResponse41.write();

        handshakeResponse41.read();
        assertEquals(capability, handshakeResponse41.capability);
        assertEquals(MysqlPacket.LARGE_PACKET_SIZE, handshakeResponse41.maxPacketSize);
        assertEquals(8, handshakeResponse41.characterSet);
        assertEquals("root", handshakeResponse41.username);
        for (int i = 0; i < handshakeResponse41.authResponse.length; i++) {
            assertEquals(0, handshakeResponse41.authResponse[i]);
        }
        assertEquals(Constants.AUTH_PLUGIN_NAME, handshakeResponse41.authPluginName);
    }

    @Test
    public void testReadWithPluginAuthLenencClientData() throws MysqlPacketFactoryException {
        int capability = CapabilityFlags.SECURE_CONNECTION |
            CapabilityFlags.PLUGIN_AUTH_LENENC_CLIENT_DATA |
            CapabilityFlags.PLUGIN_AUTH;
        HandshakeResponse41 handshakeResponse41 = factory.getMysqlPacket(HandshakeResponse41.class);
        handshakeResponse41.capability = capability;
        handshakeResponse41.maxPacketSize = MysqlPacket.LARGE_PACKET_SIZE;
        handshakeResponse41.characterSet = 8;
        handshakeResponse41.username = "root";
        handshakeResponse41.authResponse = new byte[] {0, 0, 0};
        handshakeResponse41.database = "db";
        handshakeResponse41.authPluginName = Constants.AUTH_PLUGIN_NAME;
        handshakeResponse41.write();

        handshakeResponse41.read();
        assertEquals(capability, handshakeResponse41.capability);
        assertEquals(MysqlPacket.LARGE_PACKET_SIZE, handshakeResponse41.maxPacketSize);
        assertEquals(8, handshakeResponse41.characterSet);
        assertEquals("root", handshakeResponse41.username);
        for (int i = 0; i < handshakeResponse41.authResponse.length; i++) {
            assertEquals(0, handshakeResponse41.authResponse[i]);
        }
        assertEquals(Constants.AUTH_PLUGIN_NAME, handshakeResponse41.authPluginName);
    }
}
