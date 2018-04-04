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

package top.evodb.mysql.handler.client;

import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.evodb.buffer.ProtocolBuffer;
import top.evodb.exception.MysqlPacketFactoryException;
import top.evodb.mysql.AbstractMysqlConnection;
import top.evodb.mysql.Constants;
import top.evodb.mysql.handler.Handler;
import top.evodb.mysql.protocol.ServerStatus;
import top.evodb.mysql.protocol.packet.HandshakeV10Packet;
import top.evodb.mysql.protocol.packet.MysqlPacket;

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
            HandshakeV10Packet handshakeV10Packet = mysqlConnection.getMysqlPacketFactory().getMysqlPacket(MysqlPacket.HANDSHAKE_PACKET_V10);
            handshakeV10Packet.capabilityFlags = Constants.SERVER_CAPABILITY;
            handshakeV10Packet.statusFlag = ServerStatus.SERVER_STATUS_AUTOCOMMIT;
            ProtocolBuffer buffer = handshakeV10Packet.write();
            mysqlConnection.asyncWrite(buffer);
        } catch (MysqlPacketFactoryException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }
}
