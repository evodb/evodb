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
import top.evodb.server.mysql.protocol.ServerStatus;
import top.evodb.server.util.BitUtil;

/**
 * @author evodb
 */
public class OKPacket extends AbstractMysqlPacket {

    public long affectedRows;
    public long lastInsertId;
    public short statusFlag;
    public short warnings;
    public String info;
    public String sessionStateChanges;
    public int capabilityFlags;

    public OKPacket(ProtocolBuffer protocolBuffer, Integer startIndex, Integer endIndex) {
        super(protocolBuffer, startIndex, endIndex);
        info = "";
    }

    @Override
    public ProtocolBuffer write() {
        protocolBuffer.writeIndex(startIndex + PACKET_OFFSET);
        protocolBuffer.writeByte(sequenceId);
        protocolBuffer.writeByte(OK_PACKET);
        protocolBuffer.writeLenencInt(affectedRows);
        protocolBuffer.writeLenencInt(lastInsertId);
        if (BitUtil.checkBit(capabilityFlags, CapabilityFlags.PROTOCOL_41)) {
            protocolBuffer.writeFixInt(2, statusFlag);
            protocolBuffer.writeFixInt(2, warnings);
        } else if (BitUtil.checkBit(capabilityFlags, CapabilityFlags.TRANSACTIONS)) {
            protocolBuffer.writeFixInt(2, statusFlag);
        }
        if (BitUtil.checkBit(capabilityFlags, CapabilityFlags.SESSION_TRACK)) {
            protocolBuffer.writeLenencString(info);
            if (BitUtil.checkBit(statusFlag, ServerStatus.SERVER_SESSION_STATE_CHANGED)) {
                protocolBuffer.writeLenencString(sessionStateChanges);
            }
        } else {
            protocolBuffer.writeNULString(info);
        }
        int packetLen = protocolBuffer.writeIndex() - PACKET_OFFSET - startIndex;
        protocolBuffer.putFixInt(startIndex, 3, packetLen - 1);
        return protocolBuffer;
    }

    @Override
    public void read() {
        protocolBuffer.readIndex(startIndex);
        payloadLength = (int) protocolBuffer.readFixInt(PACKET_OFFSET);
        sequenceId = protocolBuffer.readByte();
        cmd = protocolBuffer.readByte();
        affectedRows = protocolBuffer.readLenencInt();
        lastInsertId = protocolBuffer.readLenencInt();
        if (BitUtil.checkBit(capabilityFlags, CapabilityFlags.PROTOCOL_41)) {
            statusFlag = (short) protocolBuffer.readFixInt(2);
            warnings = (short) protocolBuffer.readFixInt(2);
        } else if (BitUtil.checkBit(capabilityFlags, CapabilityFlags.TRANSACTIONS)) {
            statusFlag = (short) protocolBuffer.readFixInt(2);
        }
        if (BitUtil.checkBit(capabilityFlags, CapabilityFlags.SESSION_TRACK)) {
            info = protocolBuffer.readLenencString();
            if (BitUtil.checkBit(statusFlag, ServerStatus.SERVER_SESSION_STATE_CHANGED)) {
                sessionStateChanges = protocolBuffer.readLenencString();
            }
        } else {
            info = protocolBuffer.readNULString();
        }
    }

}
