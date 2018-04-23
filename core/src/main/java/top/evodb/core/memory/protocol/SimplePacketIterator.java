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

package top.evodb.core.memory.protocol;


import top.evodb.core.protocol.MysqlPacket;

/**
 * @author evodb
 */
public class SimplePacketIterator implements PacketIterator {

    private ProtocolBuffer protocolBuffer;
    private int iterateIndex;

    public SimplePacketIterator(ProtocolBuffer protocolBuffer) {
        this.protocolBuffer = protocolBuffer;
    }

    @Override
    public boolean hasPacket() {
        if (iterateIndex + MysqlPacket.PACKET_CMD_OFFSET < protocolBuffer.writeIndex()) {
            return true;
        }
        return false;
    }

    @Override
    public long nextPacket() {
        if (hasPacket()) {
            long packetDesriptor = 0;
            int packetLength = (int) protocolBuffer
                .getFixInt(iterateIndex, MysqlPacket.PACKET_OFFSET);
            byte cmd = protocolBuffer.getByte(iterateIndex + MysqlPacket.PACKET_PAYLOAD_OFFSET);
            PacketDescriptor.PacketType packetType = getPacketType(packetLength);
            packetDesriptor = PacketDescriptor.setPacketLen(packetDesriptor, packetLength);
            packetDesriptor = PacketDescriptor.setPacketStartPos(packetDesriptor, iterateIndex);
            packetDesriptor = PacketDescriptor.setPacketType(packetDesriptor, packetType);
            packetDesriptor = PacketDescriptor.setCommandType(packetDesriptor, cmd);
            iterateIndex += packetLength + MysqlPacket.PACKET_OFFSET;
            return packetDesriptor;
        } else {
            return PacketDescriptor.NONE;
        }
    }

    private PacketDescriptor.PacketType getPacketType(long packetLength) {
        if (iterateIndex + packetLength - 1 + MysqlPacket.PACKET_PAYLOAD_OFFSET > protocolBuffer
            .writeIndex()) {
            return PacketDescriptor.PacketType.HALF;
        } else {
            return PacketDescriptor.PacketType.FULL;
        }
    }

    @Override
    public void reset() {
        iterateIndex = 0;
    }
}
