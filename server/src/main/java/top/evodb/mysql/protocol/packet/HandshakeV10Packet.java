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

package top.evodb.mysql.protocol.packet;

import top.evodb.buffer.ProtocolBuffer;
import top.evodb.mysql.Constants;
import top.evodb.mysql.protocol.CapabilityFlags;
import top.evodb.util.BitUtil;
import top.evodb.util.RandomUtil;

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
        authPluginName = "mysql_native_password";
        serverVersion = Constants.VERSION;
        protocolVersion = 0x0a;
    }

    @Override
    public ProtocolBuffer write() {
        protocolBuffer.writeIndex(startIndex + PACKET_OFFSET);
        protocolBuffer.writeByte(sequenceId);

        protocolBuffer.writeByte(protocolVersion);
        protocolBuffer.writeNULString(serverVersion);
        protocolBuffer.writeFixInt(4, connectionId);
        protocolBuffer.writeBytes(authPluginDataPart1);
        protocolBuffer.writeByte((byte) 0);
        protocolBuffer.writeFixInt(2, capabilityFlags & 0xFFFF);
        //TODO charset
        protocolBuffer.writeByte((byte) 8);
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
        int packetLen = protocolBuffer.writeIndex() - PACKET_OFFSET - startIndex;
        protocolBuffer.putFixInt(startIndex, 3, packetLen - 1);
        return protocolBuffer;
    }

    @Override
    public void read() {

    }
}
