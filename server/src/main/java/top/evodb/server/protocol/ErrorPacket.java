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

import top.evodb.core.memory.heap.ByteChunk;
import top.evodb.core.memory.heap.ByteChunkAllocator;
import top.evodb.core.memory.protocol.AbstractProtocolBuffer;
import top.evodb.core.memory.protocol.ProtocolBuffer;
import top.evodb.core.protocol.MysqlPacket;
import top.evodb.server.mysql.CapabilityFlags;
import top.evodb.server.mysql.ErrorCode;
import top.evodb.server.util.BitUtil;

/**
 * @author evodb
 */
public class ErrorPacket extends AbstractMysqlPacket {
    public short errorCode;
    public int capabilities;
    public ByteChunk sqlState;
    public ByteChunk message;
    private ByteChunkAllocator byteChunkAllocator;

    public ErrorPacket(ProtocolBuffer protocolBuffer, Integer startIndex, Integer endIndex) {
        super(protocolBuffer, startIndex, endIndex);
        cmd = MysqlPacket.ERR_PACKET;
        byteChunkAllocator = ((AbstractProtocolBuffer) protocolBuffer).getByteChunkAllocator();
    }

    @Override
    public void destory() {

    }

    @Override
    public ProtocolBuffer write() {
        protocolBuffer.writeIndex(startIndex + MysqlPacket.PACKET_OFFSET);
        protocolBuffer.writeByte(sequenceId);

        protocolBuffer.writeByte(cmd);
        protocolBuffer.writeFixInt(2, errorCode);
        if (BitUtil.checkBit(capabilities, CapabilityFlags.PROTOCOL_41)) {
            String sqlState = ErrorCode.getSqlState(errorCode);
            ByteChunk byteChunk = byteChunkAllocator.alloc(sqlState.length());
            byteChunk.append(sqlState);
            protocolBuffer.writeFixString(byteChunk);
            byteChunk.recycle();
        }
        protocolBuffer.writeFixString(message);
        int packetLen = protocolBuffer.writeIndex() - MysqlPacket.PACKET_OFFSET - startIndex;
        protocolBuffer.putFixInt(startIndex, 3, packetLen - 1);
        return protocolBuffer;
    }

    @Override
    public void read() {
        protocolBuffer.writeIndex(startIndex + MysqlPacket.PACKET_OFFSET);
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
