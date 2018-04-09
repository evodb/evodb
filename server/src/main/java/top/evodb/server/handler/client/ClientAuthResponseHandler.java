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
import top.evodb.server.buffer.AbstractProtocolBuffer;
import top.evodb.server.buffer.PacketDescriptor;
import top.evodb.server.exception.MysqlPacketFactoryException;
import top.evodb.server.handler.Handler;
import top.evodb.server.mysql.AbstractMysqlConnection;
import top.evodb.server.mysql.Charset;
import top.evodb.server.mysql.Constants;
import top.evodb.server.mysql.ErrorCode;
import top.evodb.server.mysql.protocol.packet.HandshakeResponse41;

/**
 * @author evodb
 */
public class ClientAuthResponseHandler implements Handler {
    public static final ClientAuthResponseHandler INSTANCE = new ClientAuthResponseHandler();
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientAuthResponseHandler.class);

    private ClientAuthResponseHandler() {
    }

    @Override
    public boolean handle(AbstractMysqlConnection mysqlConnection) {
        byte lastPacketId = 2;
        try {
            AbstractProtocolBuffer protocolBuffer = (AbstractProtocolBuffer) mysqlConnection.read();
            if (protocolBuffer.getPacketType() == PacketDescriptor.PacketType.FULL) {
                //TODO load charset
                Charset charset = new Charset();
                HandshakeResponse41 handshakeResponse41 = mysqlConnection.getMysqlPacketFactory().getMysqlPacket(HandshakeResponse41.class, protocolBuffer);
                handshakeResponse41.read();
                lastPacketId = (byte) (handshakeResponse41.getSequenceId() + 1);
                mysqlConnection.setMaxPacketSize(handshakeResponse41.maxPacketSize);
                mysqlConnection.setCapability(handshakeResponse41.capability);
                charset.charsetIndex = handshakeResponse41.characterSet;
                mysqlConnection.setCharset(charset);
                if (!Constants.AUTH_PLUGIN_NAME.equals(handshakeResponse41.authPluginName)) {
                    closeConnection(mysqlConnection, ErrorCode.ER_ACCESS_DENIED_ERROR, lastPacketId, "Auth plugin not found.");
                }
                if (!auth(handshakeResponse41)) {
                    closeConnection(mysqlConnection, ErrorCode.ER_ACCESS_DENIED_ERROR, lastPacketId, "Access denied for user '" + handshakeResponse41.username + '\'');
                }
                return true;
            } else {
                return false;
            }
        } catch (IOException e) {
            LOGGER.warn(mysqlConnection.getName() + " Network error.", e);
            closeConnection(mysqlConnection, ErrorCode.ER_ACCESS_DENIED_ERROR, lastPacketId, "IO error.");
        } catch (MysqlPacketFactoryException e) {
            LOGGER.warn(mysqlConnection.getName() + " Create packet error.", e);
            closeConnection(mysqlConnection, ErrorCode.ER_ACCESS_DENIED_ERROR, lastPacketId, "Protocol error.");
        }
        return true;
    }

    private void closeConnection(AbstractMysqlConnection mysqlConnection, short errorCode, byte lastPacketId, String message) {
        mysqlConnection.setAttribute(AbstractMysqlConnection.ATTR_PRE_PACKET_ID, lastPacketId);
        mysqlConnection.close(errorCode, message);
    }

    private boolean auth(HandshakeResponse41 handshakeResponse41) {
        return false;
    }
}
