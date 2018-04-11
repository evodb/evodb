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
import org.omg.Messaging.SYNC_WITH_TRANSPORT;
import top.evodb.server.buffer.AdjustableProtocolBufferAllocator;
import top.evodb.server.buffer.ProtocolBuffer;
import top.evodb.server.buffer.ProtocolBufferAllocator;
import top.evodb.server.exception.MysqlPacketFactoryException;
import top.evodb.server.mysql.Constants;
import top.evodb.server.mysql.protocol.packet.HandshakeResponse41Packet;
import top.evodb.server.mysql.protocol.packet.HandshakeV10Packet;
import top.evodb.server.mysql.protocol.packet.MysqlPacket;
import top.evodb.server.mysql.protocol.packet.MysqlPacketFactory;
import top.evodb.server.network.Acceptor;
import top.evodb.server.network.Reactor;
import top.evodb.server.util.SecurityUtil;

/**
 * @author evodb
 */
public class ClientAuthResponseHandlerTest {
    private static final int CHUNK_SIZE = 15;
    private Reactor reactor;
    private Acceptor acceptor;
    private ProtocolBufferAllocator allocator = new AdjustableProtocolBufferAllocator(CHUNK_SIZE);
    private MysqlPacketFactory factory = new MysqlPacketFactory(allocator);

    @Before
    public void setUp() throws IOException {
        reactor = Reactor.newInstance();
        acceptor = Acceptor.newInstance("127.0.0.1", 8888, reactor);
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
        Socket socket = new Socket();
        socket.connect(new InetSocketAddress("127.0.0.1", 8888));
        InputStream in = socket.getInputStream();
        byte[] buffer = new byte[1024];
        int readed = in.read(buffer);
        byte[] readBytes = new byte[readed];
        System.arraycopy(buffer, 0, readBytes, 0, readed);
        ProtocolBuffer protocolBuffer = allocator.allocate();
        protocolBuffer.writeBytes(readBytes);
        HandshakeV10Packet handshakeV10Packet = factory.getMysqlPacket(HandshakeV10Packet.class, protocolBuffer);
        handshakeV10Packet.read();
        allocator.recyle(protocolBuffer);

        HandshakeResponse41Packet handshakeResponse41Packet = factory.getMysqlPacket(HandshakeResponse41Packet.class);
        handshakeResponse41Packet.setSequenceId((byte) 1);
        handshakeResponse41Packet.capability = Constants.CLIENT_CAPABILITY;
        handshakeResponse41Packet.maxPacketSize = MysqlPacket.LARGE_PACKET_SIZE;
        handshakeResponse41Packet.characterSet = 8;
        handshakeResponse41Packet.username = "root";
        handshakeResponse41Packet.authPluginName = Constants.AUTH_PLUGIN_NAME;

        byte[] challenge = new byte[20];
        System.arraycopy(handshakeV10Packet.authPluginDataPart1, 0, challenge, 0, 8);
        System.arraycopy(handshakeV10Packet.authPluginDataPart2, 0, challenge, 8, 12);
        handshakeResponse41Packet.authResponse = SecurityUtil.scramble411("123456".getBytes(), challenge);
        protocolBuffer = handshakeResponse41Packet.write();

        byte[] reponseBytes = protocolBuffer.readBytes(protocolBuffer.readableBytes());
        socket.getOutputStream().write(reponseBytes);


        in.close();
        socket.close();
    }

}