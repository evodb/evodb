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

import top.evodb.server.buffer.ProtocolBuffer;
import top.evodb.server.mysql.protocol.CapabilityFlags;
import top.evodb.server.util.BitUtil;

/**
 * @author evodb
 */
public class HandshakeResponse41 extends AbstractMysqlPacket {
    public int capability;
    public int maxPacketSize;
    public byte characterSet;
    public String username;
    public byte[] authResponse;
    public String database;
    public String authPluginName;

    public HandshakeResponse41(ProtocolBuffer protocolBuffer, Integer startIndex, Integer endIndex) {
        super(protocolBuffer, startIndex, endIndex);
    }

    @Override
    public ProtocolBuffer write() {
        return null;
    }

    @Override
    public void read() {
        protocolBuffer.readIndex(startIndex);
        payloadLength = (int) protocolBuffer.readFixInt(3);
        sequenceId = protocolBuffer.readByte();
        capability = (int) protocolBuffer.readFixInt(4);
        maxPacketSize = (int) protocolBuffer.readFixInt(4);
        characterSet = protocolBuffer.readByte();
        protocolBuffer.readIndex(protocolBuffer.readIndex() + 23);
        username = protocolBuffer.readNULString();
        if (BitUtil.checkBit(capability, CapabilityFlags.PLUGIN_AUTH_LENENC_CLIENT_DATA)) {
            int lengthOfAuthResponse = (int) protocolBuffer.readLenencInt();
            authResponse = protocolBuffer.readBytes(lengthOfAuthResponse);
        } else if (BitUtil.checkBit(capability, CapabilityFlags.SECURE_CONNECTION)) {
            byte lengthOfAuthResonse = protocolBuffer.readByte();
            authResponse = protocolBuffer.readBytes(lengthOfAuthResonse);
        } else {
            authResponse = protocolBuffer.readNULString().getBytes();
        }
        if (BitUtil.checkBit(capability, CapabilityFlags.CONNECT_WITH_DB)) {
            database = protocolBuffer.readNULString();
        }
        if (BitUtil.checkBit(capability, CapabilityFlags.PLUGIN_AUTH)) {
            authPluginName = protocolBuffer.readNULString();
        }
    }
}