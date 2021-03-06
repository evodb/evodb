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
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.evodb.core.memory.heap.ByteChunk;
import top.evodb.core.memory.protocol.PacketDescriptor;
import top.evodb.core.memory.protocol.ProtocolBuffer;
import top.evodb.core.protocol.MysqlPacket;
import top.evodb.core.util.StringCache;
import top.evodb.server.ServerContext;
import top.evodb.server.exception.MysqlPacketFactoryException;
import top.evodb.server.handler.Handler;
import top.evodb.server.mysql.AbstractMysqlConnection;
import top.evodb.server.mysql.Charset;
import top.evodb.server.mysql.Constants;
import top.evodb.server.mysql.ErrorCode;
import top.evodb.server.mysql.ServerStatus;
import top.evodb.server.protocol.HandshakeResponse41Packet;
import top.evodb.server.protocol.OKPacket;
import top.evodb.server.util.PacketUtil;
import top.evodb.server.util.SecurityUtil;

/**
 * @author evodb
 */
public class ClientAuthResponseHandler implements Handler {
    public static final ClientAuthResponseHandler INSTANCE = new ClientAuthResponseHandler();
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientAuthResponseHandler.class);
    private final StringCache stringCache = ServerContext.getContext().getStringCache();

    private ClientAuthResponseHandler() {
    }

    @Override
    public boolean handle(AbstractMysqlConnection mysqlConnection) {
        byte lastPacketId = 2;
        HandshakeResponse41Packet handshakeResponse41Packet = null;
        OKPacket okPacket = null;
        try {
            ProtocolBuffer protocolBuffer = mysqlConnection.read();
            if (PacketUtil.getPacketType(protocolBuffer, protocolBuffer.readIndex()) == PacketDescriptor.PacketType.FULL) {
                //TODO load charset
                Charset charset = new Charset();
                handshakeResponse41Packet = mysqlConnection.getMysqlPacketFactory().getMysqlPacket(HandshakeResponse41Packet.class, protocolBuffer);
                handshakeResponse41Packet.read();
                lastPacketId = (byte) (handshakeResponse41Packet.getSequenceId() + 1);
                mysqlConnection.setMaxPacketSize(handshakeResponse41Packet.maxPacketSize);
                mysqlConnection.setCapability(handshakeResponse41Packet.capability);
                charset.charsetIndex = handshakeResponse41Packet.characterSet;
                mysqlConnection.setCharset(charset);
                mysqlConnection.setAttribute(AbstractMysqlConnection.ATTR_PRE_PACKET_ID, lastPacketId);

                if (!Constants.AUTH_PLUGIN_NAME.equals(handshakeResponse41Packet.authPluginName.toString())) {
                    closeConnection(mysqlConnection, ErrorCode.ER_ACCESS_DENIED_ERROR, "Auth plugin not found.");
                    return true;
                }

                if (!auth(handshakeResponse41Packet, (ByteChunk) mysqlConnection.getAttribute(AbstractMysqlConnection.ATTR_AUTH_PLUGIN_DATA))) {
                    closeConnection(mysqlConnection, ErrorCode.ER_ACCESS_DENIED_ERROR, "Access denied for user '" + handshakeResponse41Packet.username + '\'');
                    return true;
                } else {
                    okPacket = mysqlConnection.getMysqlPacketFactory().getMysqlPacket(MysqlPacket.OK_PACKET);
                    okPacket.capabilityFlags = mysqlConnection.getCapability();
                    okPacket.statusFlag = ServerStatus.SERVER_STATUS_AUTOCOMMIT;
                    okPacket.setSequenceId(lastPacketId);
                    ProtocolBuffer packetBuffer = okPacket.write();
                    mysqlConnection.asyncWrite(packetBuffer);
                    mysqlConnection.offerHandler(ClientIdelHandler.INSTANCE);
                }
                mysqlConnection.removeAttributes(AbstractMysqlConnection.ATTR_AUTH_PLUGIN_DATA);
                return true;
            } else {
                return false;
            }
        } catch (IOException e) {
            LOGGER.warn(mysqlConnection.getName() + " Network error.", e);
            closeConnection(mysqlConnection, ErrorCode.ER_ACCESS_DENIED_ERROR, "IO error.");
        } catch (MysqlPacketFactoryException e) {
            LOGGER.warn(mysqlConnection.getName() + " Create packet error.", e);
            closeConnection(mysqlConnection, ErrorCode.ER_ACCESS_DENIED_ERROR, "Protocol error.");
        } catch (Exception e) {
            LOGGER.warn(mysqlConnection.getName() + "Unexcept error.", e);
            closeConnection(mysqlConnection, ErrorCode.ER_ACCESS_DENIED_ERROR, "Protocol error.");
        } finally {
            if (handshakeResponse41Packet != null) {
                handshakeResponse41Packet.destory();
            }
            if (okPacket != null) {
                okPacket.destory();
            }
        }
        return true;
    }

    private void closeConnection(AbstractMysqlConnection mysqlConnection, short errorCode, String message) {
        mysqlConnection.close(errorCode, message);
    }

    private boolean auth(HandshakeResponse41Packet handshakeResponse41Packet, ByteChunk authPluginData) {
        //TODO real user config
        String username = handshakeResponse41Packet.username.toString();
        byte[] authResponse = handshakeResponse41Packet.authResponse.getByteArray();
        boolean authSuccess = true;
        try {
            byte[] authData = SecurityUtil.scramble411("123456".getBytes(), authPluginData.getByteArray());
            authSuccess = Arrays.equals(authData, authResponse);
        } catch (NoSuchAlgorithmException e) {
            LOGGER.warn("auth error.", e);
        } finally {
            authPluginData.recycle();
        }
        return authSuccess;

    }
}
