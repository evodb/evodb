/*
 * Copyright 2017-2018 The Evodb Project
 *
 * The Evodb Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 */

package top.evodb.mysql.protocol.packet;


import top.evodb.buffer.ProtocolBuffer;
import top.evodb.mysql.protocol.CapabilityFlags;
import top.evodb.mysql.protocol.ServerStatus;
import top.evodb.util.BitUtil;

/**
 * @author ynfeng
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
    }

    @Override
    public ProtocolBuffer writeToBuffer() {
        protocolBuffer.writeIndex(startIndex + PACKET_OFFSET);
        protocolBuffer.writeByte(sequenceId);
        protocolBuffer.writeByte(OK_PACKET);
        protocolBuffer.writeLenencInt(affectedRows);
        protocolBuffer.writeLenencInt(lastInsertId);
        if (BitUtil.checkBit(capabilityFlags, CapabilityFlags.CLIENT_PROTOCOL_41)) {
            protocolBuffer.writeFixInt(2, statusFlag);
            protocolBuffer.writeFixInt(2, warnings);
        } else if (BitUtil.checkBit(capabilityFlags, CapabilityFlags.CLIENT_TRANSACTIONS)) {
            protocolBuffer.writeFixInt(2, statusFlag);
        }
        if (BitUtil.checkBit(capabilityFlags, CapabilityFlags.CLIENT_SESSION_TRACK)) {
            protocolBuffer.writeLenencString(info);
            if (BitUtil.checkBit(statusFlag, ServerStatus.SERVER_SESSION_STATE_CHANGED)) {
                protocolBuffer.writeLenencString(sessionStateChanges);
            }
        } else {
            protocolBuffer.writeNULString(info);
        }
        protocolBuffer.putFixInt(startIndex, 3, protocolBuffer.writeIndex() - startIndex);
        return protocolBuffer;
    }

    @Override
    public void parseFromBuffer() {
        protocolBuffer.readIndex(startIndex);
        payloadLength = (int) protocolBuffer.readFixInt(PACKET_OFFSET);
        sequenceId = protocolBuffer.readByte();
        cmd = protocolBuffer.readByte();
        affectedRows = protocolBuffer.readLenencInt();
        lastInsertId = protocolBuffer.readLenencInt();
        if (BitUtil.checkBit(capabilityFlags, CapabilityFlags.CLIENT_PROTOCOL_41)) {
            statusFlag = (short) protocolBuffer.readFixInt(2);
            warnings = (short) protocolBuffer.readFixInt(2);
        } else if (BitUtil.checkBit(capabilityFlags, CapabilityFlags.CLIENT_TRANSACTIONS)) {
            statusFlag = (short) protocolBuffer.readFixInt(2);
        }
        if (BitUtil.checkBit(capabilityFlags, CapabilityFlags.CLIENT_SESSION_TRACK)) {
            info = protocolBuffer.readLenencString();
            if (BitUtil.checkBit(statusFlag, ServerStatus.SERVER_SESSION_STATE_CHANGED)) {
                sessionStateChanges = protocolBuffer.readLenencString();
            }
        } else {
            protocolBuffer.writeNULString(info);
        }
    }

}