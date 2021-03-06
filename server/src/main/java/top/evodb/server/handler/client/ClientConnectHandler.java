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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.evodb.core.memory.heap.ByteChunk;
import top.evodb.core.memory.heap.ByteChunkAllocator;
import top.evodb.core.memory.protocol.ProtocolBuffer;
import top.evodb.server.ServerContext;
import top.evodb.server.exception.MysqlPacketFactoryException;
import top.evodb.server.handler.Handler;
import top.evodb.server.mysql.AbstractMysqlConnection;
import top.evodb.server.mysql.Constants;
import top.evodb.server.mysql.ErrorCode;
import top.evodb.server.mysql.ServerStatus;
import top.evodb.server.protocol.HandshakeV10Packet;

/**
 * @author evodb
 */
public class ClientConnectHandler implements Handler {
    public static final ClientConnectHandler INSTANCE = new ClientConnectHandler();
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientConnectHandler.class);
    private ByteChunkAllocator byteChunkAllocator = ServerContext.getContext().getByteChunkAllocator();

    private ClientConnectHandler() {
    }

    @Override
    public boolean handle(AbstractMysqlConnection mysqlConnection) {
        HandshakeV10Packet handshakeV10Packet = null;
        try {
            ByteChunk serverVersion = byteChunkAllocator.alloc(ServerContext.getContext().getVersion().getServerVersion().length());
            serverVersion.append(ServerContext.getContext().getVersion().getServerVersion());

            handshakeV10Packet = mysqlConnection.getMysqlPacketFactory().getMysqlPacket(HandshakeV10Packet.class);
            handshakeV10Packet.capabilityFlags = Constants.SERVER_CAPABILITY;
            handshakeV10Packet.statusFlag = ServerStatus.SERVER_STATUS_AUTOCOMMIT;
            handshakeV10Packet.connectionId = ServerContext.getContext().newConnectId();
            handshakeV10Packet.characterSet = ServerContext.getContext().getCharset().charsetIndex;
            handshakeV10Packet.serverVersion = serverVersion;
            handshakeV10Packet.protocolVersion = ServerContext.getContext().getVersion().getProtocolVersion();
            ProtocolBuffer buffer = handshakeV10Packet.write();
            ByteChunk authPluginData = byteChunkAllocator.alloc(20);

            authPluginData.append(handshakeV10Packet.authPluginDataPart1);
            authPluginData.setOffset(authPluginData.getOffset() + handshakeV10Packet.authPluginDataPart1.getLength());

            authPluginData.append(handshakeV10Packet.authPluginDataPart2);

            mysqlConnection.setAttribute(AbstractMysqlConnection.ATTR_AUTH_PLUGIN_DATA, authPluginData);
            mysqlConnection.asyncWrite(buffer);
            mysqlConnection.offerHandler(ClientAuthResponseHandler.INSTANCE);
        } catch (MysqlPacketFactoryException e) {
            LOGGER.warn(mysqlConnection.getName() + " Create packet error.", e);
            mysqlConnection.close(ErrorCode.ER_HANDSHAKE_ERROR, "Handshake error.");
        } catch (IOException e) {
            LOGGER.warn(mysqlConnection.getName() + " Network error.", e);
            mysqlConnection.close(ErrorCode.ER_HANDSHAKE_ERROR, "Handshake error.");
        } finally {
            if (handshakeV10Packet != null) {
                handshakeV10Packet.destory();
            }
        }
        return true;
    }
}
