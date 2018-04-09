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

/**
 * Mysql protocol packet.
 *
 * @author evodb
 */
public interface MysqlPacket extends Packet {

    int PACKET_START = 0;
    int PACKET_OFFSET = 3;
    int PACKET_PAYLOAD_OFFSET = 4;
    int PACKET_CMD_OFFSET = 5;

    byte OK_PACKET = 0x00;
    byte ERR_PACKET = (byte) 0xff;

    /**
     * Get the command in mysql packet.
     *
     * @return command of packet
     */
    byte getCmd();

    /**
     * Set the sequence id of packet.
     *
     * @param sequenceId sequenceId
     */
    void setSequenceId(byte sequenceId);

    /**
     * Write packet data to buffer.
     *
     * @return ProtocolBuffer
     */
    ProtocolBuffer write();

    /**
     * Parse data from protocol buffer.
     */
    void read();
}
