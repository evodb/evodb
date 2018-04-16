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

import top.evodb.core.memory.direct.ProtocolBuffer;
import top.evodb.core.protocol.MysqlPacket;
import top.evodb.server.mysql.Constants;
import top.evodb.server.mysql.CapabilityFlags;
import top.evodb.server.util.BitUtil;
import top.evodb.server.util.RandomUtil;

/**
 * @author evodb
 */
public class HandshakeV10Packet extends AbstractMysqlPacket {
    public byte protocolVersion;
    public String serverVersion;
    public int connectionId;
    public byte[] authPluginDataPart1;
    public byte[] authPluginDataPart2;
    public int capabilityFlags;
    public byte characterSet;
    public short statusFlag;
    public String authPluginName;

    public HandshakeV10Packet(ProtocolBuffer protocolBuffer, Integer startIndex, Integer endIndex) {
        super(protocolBuffer, startIndex, endIndex);
        authPluginDataPart1 = RandomUtil.randomBytes(8);
        authPluginDataPart2 = RandomUtil.randomBytes(12);
        capabilityFlags = Constants.SERVER_CAPABILITY;
        authPluginName = Constants.AUTH_PLUGIN_NAME;
    }

    @Override
    public ProtocolBuffer write() {
        protocolBuffer.writeIndex(startIndex + MysqlPacket.PACKET_OFFSET);
        protocolBuffer.writeByte(sequenceId);
        protocolBuffer.writeByte(protocolVersion);
        protocolBuffer.writeNULString(serverVersion);
        protocolBuffer.writeFixInt(4, connectionId);
        protocolBuffer.writeBytes(authPluginDataPart1);
        protocolBuffer.writeByte((byte) 0);
        protocolBuffer.writeFixInt(2, capabilityFlags & 0xFFFF);
        protocolBuffer.writeByte(characterSet);
        protocolBuffer.writeFixInt(2, statusFlag);
        protocolBuffer.writeFixInt(2, capabilityFlags >> 16 & 0xFFFF);
        if (BitUtil.checkBit(capabilityFlags, CapabilityFlags.PLUGIN_AUTH)) {
            protocolBuffer.writeByte((byte) 21);
        }
        protocolBuffer.writeFixInt(5, 0);
        protocolBuffer.writeFixInt(5, 0);
        if (BitUtil.checkBit(capabilityFlags, CapabilityFlags.SECURE_CONNECTION)) {
            protocolBuffer.writeBytes(authPluginDataPart2);
            protocolBuffer.writeByte((byte) 0);
        }
        if (BitUtil.checkBit(capabilityFlags, CapabilityFlags.PLUGIN_AUTH)) {
            protocolBuffer.writeNULString(authPluginName);
        }
        int packetLen = protocolBuffer.writeIndex() - MysqlPacket.PACKET_OFFSET - startIndex;
        protocolBuffer.putFixInt(startIndex, 3, packetLen - 1);
        return protocolBuffer;
    }

    @Override
    public void read() {
        protocolBuffer.writeIndex(startIndex);
        payloadLength = (int) protocolBuffer.readFixInt(3);
        sequenceId = protocolBuffer.readByte();
        protocolVersion = protocolBuffer.readByte();
        serverVersion = protocolBuffer.readNULString();
        connectionId = (int) protocolBuffer.readFixInt(4);
        authPluginDataPart1 = new byte[8];
        protocolBuffer.readBytes(authPluginDataPart1);
        protocolBuffer.readByte();


        capabilityFlags = (int) protocolBuffer.readFixInt(2) & 0xFFFF;
        characterSet = protocolBuffer.readByte();
        statusFlag = (short) protocolBuffer.readFixInt(2);
        capabilityFlags |= protocolBuffer.readFixInt(2) << 16;
        if (BitUtil.checkBit(capabilityFlags, CapabilityFlags.PLUGIN_AUTH)) {
            protocolBuffer.readByte();
        }
        protocolBuffer.readFixInt(5);
        protocolBuffer.readFixInt(5);
        if (BitUtil.checkBit(capabilityFlags, CapabilityFlags.SECURE_CONNECTION)) {
            authPluginDataPart2 = new byte[12];
            protocolBuffer.readBytes(authPluginDataPart2);
            protocolBuffer.readByte();
        }
        if (BitUtil.checkBit(capabilityFlags, CapabilityFlags.PLUGIN_AUTH)) {
            authPluginName = protocolBuffer.readNULString();
        }
    }
}
