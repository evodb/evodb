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
import top.evodb.server.ServerContext;
import top.evodb.server.buffer.ProtocolBuffer;
import top.evodb.server.exception.MysqlPacketFactoryException;
import top.evodb.server.handler.Handler;
import top.evodb.server.mysql.AbstractMysqlConnection;
import top.evodb.server.mysql.Constants;
import top.evodb.server.mysql.ErrorCode;
import top.evodb.server.mysql.protocol.ServerStatus;
import top.evodb.server.mysql.protocol.packet.HandshakeV10Packet;

/**
 * @author evodb
 */
public class ClientConnectHandler implements Handler {
    public static final ClientConnectHandler INSTANCE = new ClientConnectHandler();
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientConnectHandler.class);

    private ClientConnectHandler() {
    }

    @Override
    public boolean handle(AbstractMysqlConnection mysqlConnection) {
        try {
            HandshakeV10Packet handshakeV10Packet = mysqlConnection.getMysqlPacketFactory().getMysqlPacket(HandshakeV10Packet.class);
            handshakeV10Packet.capabilityFlags = Constants.SERVER_CAPABILITY;
            handshakeV10Packet.statusFlag = ServerStatus.SERVER_STATUS_AUTOCOMMIT;
            handshakeV10Packet.connectionId = ServerContext.getContext().newConnectId();
            handshakeV10Packet.characterSet = ServerContext.getContext().getCharset().charsetIndex;
            handshakeV10Packet.serverVersion = ServerContext.getContext().getVersion().getServerVersion();
            handshakeV10Packet.protocolVersion = ServerContext.getContext().getVersion().getProtocolVersion();
            ProtocolBuffer buffer = handshakeV10Packet.write();
            byte[] authPluginData = new byte[20];
            System.arraycopy(handshakeV10Packet.authPluginDataPart1, 0, authPluginData, 0, 8);
            System.arraycopy(handshakeV10Packet.authPluginDataPart2, 0, authPluginData, 8, 12);
            mysqlConnection.setAttribute(AbstractMysqlConnection.ATTR_AUTH_PLUGIN_DATA, authPluginData);
            mysqlConnection.asyncWrite(buffer);
            mysqlConnection.offerHandler(ClientAuthResponseHandler.INSTANCE);
        } catch (MysqlPacketFactoryException e) {
            LOGGER.warn(mysqlConnection.getName() + " Create packet error.", e);
            mysqlConnection.close(ErrorCode.ER_HANDSHAKE_ERROR, "Handshake error.");
        } catch (IOException e) {
            LOGGER.warn(mysqlConnection.getName() + " Network error.", e);
            mysqlConnection.close(ErrorCode.ER_HANDSHAKE_ERROR, "Handshake error.");
        }
        return true;
    }
}
