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
import top.evodb.core.memory.heap.ByteChunk;
import top.evodb.core.memory.heap.ByteChunkAllocator;
import top.evodb.core.memory.protocol.AdjustableProtocolBufferAllocator;
import top.evodb.core.memory.protocol.ProtocolBufferAllocator;
import top.evodb.core.protocol.MysqlPacket;
import top.evodb.server.ServerContext;
import top.evodb.server.exception.MysqlPacketFactoryException;
import top.evodb.server.mysql.CapabilityFlags;
import top.evodb.server.mysql.Constants;

/**
 * @author evodb
 */
public class HandshakeResponse41PacketTest {
    private static final int CHUNK_SIZE = 15;
    private ByteChunkAllocator byteChunkAllocator = ServerContext.getContext().getByteChunkAllocator();
    private ProtocolBufferAllocator allocator = new AdjustableProtocolBufferAllocator(CHUNK_SIZE, byteChunkAllocator);
    private MysqlPacketFactory factory = new MysqlPacketFactory(allocator);

    @Test
    public void testWrite() throws MysqlPacketFactoryException {
        ByteChunk root = byteChunkAllocator.alloc(4);
        root.append("root");
        ByteChunk authResponse = byteChunkAllocator.alloc(3);
        authResponse.append(new byte[]{1,2,3},0,3);
        ByteChunk db = byteChunkAllocator.alloc(2);
        db.append("db");
        ByteChunk authPluginName = byteChunkAllocator.alloc(Constants.AUTH_PLUGIN_NAME.length());
        authPluginName.append(Constants.AUTH_PLUGIN_NAME);

        HandshakeResponse41Packet handshakeResponse41Packet = factory.getMysqlPacket(HandshakeResponse41Packet.class);
        handshakeResponse41Packet.capability = CapabilityFlags.CONNECT_WITH_DB;
        handshakeResponse41Packet.maxPacketSize = MysqlPacket.LARGE_PACKET_SIZE;
        handshakeResponse41Packet.characterSet = 8;
        handshakeResponse41Packet.username = root;
        handshakeResponse41Packet.authResponse = authResponse;
        handshakeResponse41Packet.database = db;
        handshakeResponse41Packet.authPluginName = authPluginName;
        handshakeResponse41Packet.write();
    }

    @Test
    public void testWriteWithSecureConnection() throws MysqlPacketFactoryException {
        ByteChunk root = byteChunkAllocator.alloc(4);
        root.append("root");
        ByteChunk authResponse = byteChunkAllocator.alloc(3);
        authResponse.append(new byte[]{1,2,3},0,3);
        ByteChunk db = byteChunkAllocator.alloc(2);
        db.append("db");
        ByteChunk authPluginName = byteChunkAllocator.alloc(Constants.AUTH_PLUGIN_NAME.length());
        authPluginName.append(Constants.AUTH_PLUGIN_NAME);


        HandshakeResponse41Packet handshakeResponse41Packet = factory.getMysqlPacket(HandshakeResponse41Packet.class);
        handshakeResponse41Packet.capability = CapabilityFlags.SECURE_CONNECTION |
            CapabilityFlags.PLUGIN_AUTH;
        handshakeResponse41Packet.maxPacketSize = MysqlPacket.LARGE_PACKET_SIZE;
        handshakeResponse41Packet.characterSet = 8;
        handshakeResponse41Packet.username = root;
        handshakeResponse41Packet.authResponse = authResponse;
        handshakeResponse41Packet.database = db;
        handshakeResponse41Packet.authPluginName = authPluginName;
        handshakeResponse41Packet.write();
    }

    @Test
    public void testWriteWithPluginAuthLenencClientData() throws MysqlPacketFactoryException {
        ByteChunk root = byteChunkAllocator.alloc(4);
        root.append("root");
        ByteChunk authResponse = byteChunkAllocator.alloc(3);
        authResponse.append(new byte[]{1,2,3},0,3);
        ByteChunk db = byteChunkAllocator.alloc(2);
        db.append("db");
        ByteChunk authPluginName = byteChunkAllocator.alloc(Constants.AUTH_PLUGIN_NAME.length());
        authPluginName.append(Constants.AUTH_PLUGIN_NAME);

        HandshakeResponse41Packet handshakeResponse41Packet = factory.getMysqlPacket(HandshakeResponse41Packet.class);
        handshakeResponse41Packet.capability = CapabilityFlags.SECURE_CONNECTION |
            CapabilityFlags.PLUGIN_AUTH_LENENC_CLIENT_DATA |
            CapabilityFlags.PLUGIN_AUTH;
        handshakeResponse41Packet.maxPacketSize = MysqlPacket.LARGE_PACKET_SIZE;
        handshakeResponse41Packet.characterSet = 8;
        handshakeResponse41Packet.username = root;
        handshakeResponse41Packet.authResponse = authResponse;
        handshakeResponse41Packet.database = db;
        handshakeResponse41Packet.authPluginName = authPluginName;
        handshakeResponse41Packet.write();
    }

    @Test
    public void testRead() throws MysqlPacketFactoryException {
        int capability = CapabilityFlags.CONNECT_WITH_DB;

        ByteChunk root = byteChunkAllocator.alloc(4);
        root.append("root");
        ByteChunk authResponse = byteChunkAllocator.alloc(3);
        authResponse.append(new byte[]{1,2,3},0,3);
        ByteChunk db = byteChunkAllocator.alloc(2);
        db.append("db");
        ByteChunk authPluginName = byteChunkAllocator.alloc(Constants.AUTH_PLUGIN_NAME.length());
        authPluginName.append(Constants.AUTH_PLUGIN_NAME);

        HandshakeResponse41Packet handshakeResponse41Packet = factory.getMysqlPacket(HandshakeResponse41Packet.class);
        handshakeResponse41Packet.capability = CapabilityFlags.CONNECT_WITH_DB;
        handshakeResponse41Packet.maxPacketSize = MysqlPacket.LARGE_PACKET_SIZE;
        handshakeResponse41Packet.characterSet = 8;
        handshakeResponse41Packet.username = root;
        handshakeResponse41Packet.authResponse = authResponse;
        handshakeResponse41Packet.database = db;
        handshakeResponse41Packet.authPluginName = authPluginName;
        handshakeResponse41Packet.write();

        handshakeResponse41Packet.read();
        assertEquals(capability, handshakeResponse41Packet.capability);
        assertEquals(MysqlPacket.LARGE_PACKET_SIZE, handshakeResponse41Packet.maxPacketSize);
        assertEquals(8, handshakeResponse41Packet.characterSet);
        assertEquals("root", handshakeResponse41Packet.username.toString());
        for (int i = 0; i < handshakeResponse41Packet.authResponse.getLength(); i++) {
            byte[] bytes = handshakeResponse41Packet.authResponse.getByteArray();
            assertEquals(i + 1, bytes[i]);
        }
        assertEquals(Constants.AUTH_PLUGIN_NAME, handshakeResponse41Packet.authPluginName.toString());
    }

    @Test
    public void testReadWithSecureConnection() throws MysqlPacketFactoryException {
        int capability = CapabilityFlags.SECURE_CONNECTION |
            CapabilityFlags.PLUGIN_AUTH;

        ByteChunk root = byteChunkAllocator.alloc(4);
        root.append("root");
        ByteChunk authResponse = byteChunkAllocator.alloc(3);
        authResponse.append(new byte[]{1,2,3},0,3);
        ByteChunk db = byteChunkAllocator.alloc(2);
        db.append("db");
        ByteChunk authPluginName = byteChunkAllocator.alloc(Constants.AUTH_PLUGIN_NAME.length());
        authPluginName.append(Constants.AUTH_PLUGIN_NAME);

        HandshakeResponse41Packet handshakeResponse41Packet = factory.getMysqlPacket(HandshakeResponse41Packet.class);
        handshakeResponse41Packet.capability = capability;
        handshakeResponse41Packet.maxPacketSize = MysqlPacket.LARGE_PACKET_SIZE;
        handshakeResponse41Packet.characterSet = 8;
        handshakeResponse41Packet.username = root;
        handshakeResponse41Packet.authResponse = authResponse;
        handshakeResponse41Packet.database = db;
        handshakeResponse41Packet.authPluginName = authPluginName;
        handshakeResponse41Packet.write();

        handshakeResponse41Packet.read();
        assertEquals(capability, handshakeResponse41Packet.capability);
        assertEquals(MysqlPacket.LARGE_PACKET_SIZE, handshakeResponse41Packet.maxPacketSize);
        assertEquals(8, handshakeResponse41Packet.characterSet);
        assertEquals("root", handshakeResponse41Packet.username.toString());
        for (int i = 0; i < handshakeResponse41Packet.authResponse.getLength(); i++) {
            byte[] bytes = handshakeResponse41Packet.authResponse.getByteArray();
            assertEquals(i + 1, bytes[i]);
        }
        assertEquals(Constants.AUTH_PLUGIN_NAME, handshakeResponse41Packet.authPluginName.toString());
    }

    @Test
    public void testReadWithPluginAuthLenencClientData() throws MysqlPacketFactoryException {
        int capability = CapabilityFlags.SECURE_CONNECTION |
            CapabilityFlags.PLUGIN_AUTH_LENENC_CLIENT_DATA |
            CapabilityFlags.PLUGIN_AUTH;

        ByteChunk root = byteChunkAllocator.alloc(4);
        root.append("root");
        ByteChunk authResponse = byteChunkAllocator.alloc(3);
        authResponse.append(new byte[]{1,2,3},0,3);
        ByteChunk db = byteChunkAllocator.alloc(2);
        db.append("db");
        ByteChunk authPluginName = byteChunkAllocator.alloc(Constants.AUTH_PLUGIN_NAME.length());
        authPluginName.append(Constants.AUTH_PLUGIN_NAME);

        HandshakeResponse41Packet handshakeResponse41Packet = factory.getMysqlPacket(HandshakeResponse41Packet.class);
        handshakeResponse41Packet.capability = capability;
        handshakeResponse41Packet.maxPacketSize = MysqlPacket.LARGE_PACKET_SIZE;
        handshakeResponse41Packet.characterSet = 8;
        handshakeResponse41Packet.username = root;
        handshakeResponse41Packet.authResponse = authResponse;
        handshakeResponse41Packet.database = db;
        handshakeResponse41Packet.authPluginName = authPluginName;
        handshakeResponse41Packet.write();

        handshakeResponse41Packet.read();
        assertEquals(capability, handshakeResponse41Packet.capability);
        assertEquals(MysqlPacket.LARGE_PACKET_SIZE, handshakeResponse41Packet.maxPacketSize);
        assertEquals(8, handshakeResponse41Packet.characterSet);
        assertEquals("root", handshakeResponse41Packet.username.toString());
        for (int i = 0; i < handshakeResponse41Packet.authResponse.getLength(); i++) {
            byte[] bytes = handshakeResponse41Packet.authResponse.getByteArray();
            assertEquals(i + 1, bytes[i]);
        }
        assertEquals(Constants.AUTH_PLUGIN_NAME, handshakeResponse41Packet.authPluginName.toString());
    }
}
