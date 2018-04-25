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

package top.evodb.server.handler.client;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import top.evodb.core.memory.heap.ByteChunk;
import top.evodb.core.memory.heap.ByteChunkAllocator;
import top.evodb.core.memory.protocol.AdjustableProtocolBufferAllocator;
import top.evodb.core.memory.protocol.ProtocolBuffer;
import top.evodb.core.memory.protocol.ProtocolBufferAllocator;
import top.evodb.core.protocol.MysqlPacket;
import top.evodb.server.PortRandomUtil;
import top.evodb.server.ServerContext;
import top.evodb.server.exception.MysqlPacketFactoryException;
import top.evodb.server.mysql.Constants;
import top.evodb.server.network.Acceptor;
import top.evodb.server.network.Reactor;
import top.evodb.server.protocol.HandshakeResponse41Packet;
import top.evodb.server.protocol.HandshakeV10Packet;
import top.evodb.server.protocol.MysqlPacketFactory;
import top.evodb.server.util.SecurityUtil;

/**
 * @author evodb
 */
public class ClientAuthResponseHandlerTest {
    private static final int CHUNK_SIZE = 15;
    private Reactor reactor;
    private Acceptor acceptor;
    private ByteChunkAllocator byteChunkAllocator = ServerContext.getContext().getByteChunkAllocator();
    private ProtocolBufferAllocator allocator = new AdjustableProtocolBufferAllocator(CHUNK_SIZE, byteChunkAllocator);
    private MysqlPacketFactory factory = new MysqlPacketFactory(allocator);
    private int port;

    @SuppressWarnings("Duplicates")
    @Before
    public void setUp() throws IOException {
        port = PortRandomUtil.getPort();
        reactor = Reactor.newInstance();
        acceptor = Acceptor.newInstance("127.0.0.1", port, reactor);
        acceptor.start();
        reactor.start();
    }

    @After
    public void shutdown() {
        acceptor.shutdown();
        reactor.shutdown();
    }

    @Test
    public void testHandle() throws IOException, MysqlPacketFactoryException, NoSuchAlgorithmException {
        auth("123456", Constants.AUTH_PLUGIN_NAME);
    }

    @Test
    public void testHandleWithWrongPassword()
        throws IOException, MysqlPacketFactoryException, NoSuchAlgorithmException {
        auth("1234567", Constants.AUTH_PLUGIN_NAME);
    }

    @Test
    public void testHandleWithWrongAuthPluginName()
        throws IOException, MysqlPacketFactoryException, NoSuchAlgorithmException {
        auth("1234567", "some thing");
    }

    private void auth(String password, String authPluginName)
        throws IOException, MysqlPacketFactoryException, NoSuchAlgorithmException {
        Socket socket = new Socket();
        socket.connect(new InetSocketAddress("127.0.0.1", port));
        InputStream in = socket.getInputStream();
        byte[] buffer = new byte[1024];
        int readed = in.read(buffer);
        byte[] readBytes = new byte[readed];
        System.arraycopy(buffer, 0, readBytes, 0, readed);

        ByteChunk byteChunk = byteChunkAllocator.alloc(readed);
        byteChunk.append(readBytes, 0, readed);

        ProtocolBuffer protocolBuffer = allocator.allocate();
        protocolBuffer.writeBytes(byteChunk);

        HandshakeV10Packet handshakeV10Packet = factory.getMysqlPacket(HandshakeV10Packet.class, protocolBuffer);
        handshakeV10Packet.read();
        allocator.recyle(protocolBuffer);

        ByteChunk username = byteChunkAllocator.alloc(4);
        username.append("root");
        ByteChunk authPluginNameByteChunk = byteChunkAllocator.alloc(authPluginName.length());
        authPluginNameByteChunk.append(authPluginName);

        HandshakeResponse41Packet handshakeResponse41Packet = factory.getMysqlPacket(HandshakeResponse41Packet.class);
        handshakeResponse41Packet.setSequenceId((byte) 1);
        handshakeResponse41Packet.capability = Constants.CLIENT_CAPABILITY;
        handshakeResponse41Packet.maxPacketSize = MysqlPacket.LARGE_PACKET_SIZE;
        handshakeResponse41Packet.characterSet = 8;
        handshakeResponse41Packet.username = username;
        handshakeResponse41Packet.authPluginName = authPluginNameByteChunk;

        ByteChunk challenge = byteChunkAllocator.alloc(20);
        challenge.append(handshakeV10Packet.authPluginDataPart1);
        challenge.setOffset(challenge.getOffset() + handshakeV10Packet.authPluginDataPart1.getLength());
        challenge.append(handshakeV10Packet.authPluginDataPart2);

        byte[] authData = SecurityUtil.scramble411(password.getBytes(), challenge.getByteArray());
        ByteChunk authResonseByteChunk = byteChunkAllocator.alloc(authData.length);
        authResonseByteChunk.append(authData, 0, authData.length);
        handshakeResponse41Packet.authResponse = authResonseByteChunk;

        protocolBuffer = handshakeResponse41Packet.write();

        byte[] reponseBytes = new byte[protocolBuffer.readableBytes()];

        byteChunk = byteChunkAllocator.alloc(reponseBytes.length);
        protocolBuffer.readBytes(byteChunk);
        byte[] out = byteChunk.getByteArray();
        socket.getOutputStream().write(out);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }
        in.close();
        socket.close();
    }

}
