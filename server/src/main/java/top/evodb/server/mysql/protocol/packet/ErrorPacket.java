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

import top.evodb.core.buffer.ProtocolBuffer;
import top.evodb.core.mysql.protocol.packet.MysqlPacket;
import top.evodb.server.mysql.ErrorCode;
import top.evodb.server.mysql.protocol.CapabilityFlags;
import top.evodb.server.util.BitUtil;

/**
 * @author evodb
 */
public class ErrorPacket extends AbstractMysqlPacket {
    public short errorCode;
    public int capabilities;
    public String sqlState;
    public String message;

    public ErrorPacket(ProtocolBuffer protocolBuffer, Integer startIndex, Integer endIndex) {
        super(protocolBuffer, startIndex, endIndex);
        cmd = MysqlPacket.ERR_PACKET;
    }

    @Override
    public ProtocolBuffer write() {
        protocolBuffer.writeIndex(startIndex + PACKET_OFFSET);
        protocolBuffer.writeByte(sequenceId);

        protocolBuffer.writeByte(cmd);
        protocolBuffer.writeFixInt(2, errorCode);
        if (BitUtil.checkBit(capabilities, CapabilityFlags.PROTOCOL_41)) {
            protocolBuffer.writeFixString(ErrorCode.getSqlState(errorCode));
        }
        protocolBuffer.writeFixString(message);
        int packetLen = protocolBuffer.writeIndex() - PACKET_OFFSET - startIndex;
        protocolBuffer.putFixInt(startIndex, 3, packetLen - 1);
        return protocolBuffer;
    }

    @Override
    public void read() {
        protocolBuffer.writeIndex(startIndex + PACKET_OFFSET);
        payloadLength = (int) protocolBuffer.readFixInt(3);
        sequenceId = protocolBuffer.readByte();

        cmd = protocolBuffer.readByte();
        errorCode = (short) protocolBuffer.readFixInt(2);
        if (BitUtil.checkBit(capabilities, CapabilityFlags.PROTOCOL_41)) {
            sqlState = protocolBuffer.readFixString(6);
        }
        message = protocolBuffer.readFixString(payloadLength - protocolBuffer.readIndex() - startIndex + MysqlPacket.PACKET_PAYLOAD_OFFSET);
    }
}
